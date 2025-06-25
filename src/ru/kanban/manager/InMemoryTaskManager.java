package ru.kanban.manager;

import ru.kanban.task.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private long idCounter = 0;

    private final Map<Long, Task> tasks = new HashMap<>();
    private final Map<Long, Epic> epics = new HashMap<>();
    private final Map<Long, SubTask> subtasks = new HashMap<>();
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
    public SubTask createSubTask(String title, String description, long epicId) {
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
    public Task getTask(long id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(long id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public SubTask getSubTask(long id) {
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
    public List<SubTask> getSubTasksOfEpic(long epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return new ArrayList<>();
        List<SubTask> result = new ArrayList<>();
        for (Long id : epic.getSubTaskIds()) {
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
    public void removeTask(long id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpic(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            List<Long> ids = epic.getSubTaskIds();
            for (Long subId : ids) {
                subtasks.remove(subId);
            }
        }
    }

    @Override
    public void removeSubTask(long id) {
        SubTask sub = subtasks.remove(id);
        if (sub != null) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void clearAll() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        idCounter = 0;
    }

    private void updateEpicStatus(Epic epic) {
        List<SubTask> subs = new ArrayList<>();
        List<Long> ids = epic.getSubTaskIds();
        for (Long id : ids) {
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

