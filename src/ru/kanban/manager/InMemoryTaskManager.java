package ru.kanban.manager;

import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter = 0;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public Task createTask(String title, String description) {
        if (title == null || description == null) return null;
        Task task = new Task(idCounter++, title, description);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        if (title == null || description == null) return null;
        Epic epic = new Epic(idCounter++, title, description);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(String title, String description, int epicId) {
        if (title == null || description == null) return null;
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + epicId + " не существует");
        }
        SubTask subTask = new SubTask(idCounter++, title, description, epicId);
        subtasks.put(subTask.getId(), subTask);
        epic.addSubTaskId(subTask.getId());
        updateEpicStatus(epic);
        return subTask;
    }

    @Override
    public void addTask(Task task) {
        if (task != null) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic != null) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void addSubTask(SubTask subtask) {
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic == null) {
                throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует");
            }
            subtasks.put(subtask.getId(), subtask);
            epic.addSubTaskId(subtask.getId());
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
        if (epic == null) return new ArrayList<>();
        List<SubTask> result = new ArrayList<>();
        for (Integer id : epic.getSubTaskIds()) {
            SubTask sub = subtasks.get(id);
            if (sub != null) {
                result.add(sub);
            }
        }
        return result;
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (updatedTask != null) {
            if (tasks.containsKey(updatedTask.getId())) {
                tasks.put(updatedTask.getId(), updatedTask);
            }
        }
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        if (updatedSubTask != null) {
            if (subtasks.containsKey(updatedSubTask.getId())) {
                subtasks.put(updatedSubTask.getId(), updatedSubTask);
                Epic epic = epics.get(updatedSubTask.getEpicId());
                if (epic != null) {
                    updateEpicStatus(epic);
                }
            }
        }
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            List<Integer> ids = epic.getSubTaskIds();
            for (Integer subId : ids) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubTask(int id) {
        SubTask sub = subtasks.remove(id);
        if (sub != null) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epic);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void clearAll() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }

        tasks.clear();
        epics.clear();
        subtasks.clear();
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
        epic.updateStatus(subs);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}

