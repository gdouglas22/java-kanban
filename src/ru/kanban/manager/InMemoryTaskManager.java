package ru.kanban.manager;

import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int idCounter = 0;

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, SubTask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;

    protected final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public Task createTask(String title, String description) {
        if (title == null || description == null) return null;
        Task task = new Task(idCounter++, title, description);
        validateNoOverlap(task);
        tasks.put(task.getId(), task);
        indexForPriority(task);
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        if (title == null || description == null) return null;
        Epic epic = new Epic(idCounter++, title, description);
        epics.put(epic.getId(), epic);
        epic.recalcTimeFields(subtasks);
        return epic;
    }

    @Override
    public SubTask createSubTask(String title, String description, int epicId) {
        if (title == null || description == null) return null;
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + epicId + " не существует");
        }
        SubTask sub = new SubTask(idCounter++, title, description, epicId);
        validateNoOverlap(sub);
        subtasks.put(sub.getId(), sub);
        epic.addSubTaskId(sub.getId());
        indexForPriority(sub);
        updateEpicStatus(epic);
        return sub;
    }

    @Override
    public void addTask(Task task) throws ManagerSaveException {
        if (task == null) return;
        validateNoOverlap(task);
        tasks.put(task.getId(), task);
        indexForPriority(task);
    }

    @Override
    public void addSubTask(SubTask subtask) {
        if (subtask == null) return;
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует");
        }
        validateNoOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubTaskId(subtask.getId());
        indexForPriority(subtask);
        updateEpicStatus(epic);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic != null) {
            epics.put(epic.getId(), epic);
            epic.recalcTimeFields(subtasks);
            updateEpicStatus(epic);
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask task = subtasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<SubTask> getSubTasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return List.of();
        }
        return epic.getSubTaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void updateTask(Task updated) {
        if (updated == null) return;
        Task old = tasks.get(updated.getId());
        if (old == null) return;

        deindexForPriority(old);
        validateNoOverlap(updated);
        tasks.put(updated.getId(), updated);
        indexForPriority(updated);
    }

    @Override
    public void updateSubTask(SubTask updated) {
        if (updated == null) return;
        SubTask old = subtasks.get(updated.getId());
        if (old == null) return;

        deindexForPriority(old);
        validateNoOverlap(updated);
        subtasks.put(updated.getId(), updated);
        indexForPriority(updated);

        Epic epic = epics.get(updated.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void removeTask(int id) {
        Task t = tasks.remove(id);
        if (t != null) {
            deindexForPriority(t);
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubTask(int id) {
        SubTask st = subtasks.remove(id);
        if (st != null) {
            deindexForPriority(st);
            Epic epic = epics.get(st.getEpicId());
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;
        new ArrayList<>(epic.getSubTaskIds()).stream()
                .map(subtasks::remove)
                .filter(Objects::nonNull)
                .peek(this::deindexForPriority)
                .map(Task::getId)
                .forEach(historyManager::remove);
        historyManager.remove(id);
    }

    @Override
    public void clearAll() {
        tasks.values().forEach(this::deindexForPriority);
        subtasks.values().forEach(this::deindexForPriority);
        tasks.clear();
        epics.clear();
        subtasks.clear();
        historyManager.getHistory().forEach(t -> historyManager.remove(t.getId()));
        idCounter = 0;
    }

    private void updateEpicStatus(Epic epic) {
        List<SubTask> subs = new ArrayList<>();
        List<Integer> ids = epic.getSubTaskIds();
        for (Integer id : ids) {
            SubTask s = subtasks.get(id);
            if (s != null) {
                subs.add(s);
            }
        }
        epic.recalcTimeFields(subtasks);
        epic.updateStatus(subs);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void indexForPriority(Task t) {
        if (t.getStartTime() != null) {
            prioritizedTasks.add(t);
        }
    }

    protected void deindexForPriority(Task t) {
        if (t.getStartTime() != null) {
            prioritizedTasks.remove(t);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean overlaps(Task a, Task b) {
        if (a == null || b == null) return false;
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private boolean overlapsAny(Task candidate, int ignoreId) {
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != ignoreId)
                .anyMatch(t -> overlaps(candidate, t));
    }

    void validateNoOverlap(Task t) {
        if (overlapsAny(t, t.getId())) {
            throw new IllegalArgumentException("Временной конфликт задачи id=" + t.getId());
        }
    }
}

