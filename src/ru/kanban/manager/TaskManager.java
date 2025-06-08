package ru.kanban.manager;

import ru.kanban.task.*;
import java.util.*;

public class TaskManager {
    private long idCounter = 0;

    private final Map<Long, Task> tasks = new HashMap<>();
    private final Map<Long, Epic> epics = new HashMap<>();
    private final Map<Long, SubTask> subtasks = new HashMap<>();

    public Task createTask(String title, String description) {
        if (title == null || description == null) return null;
        Task task = new Task(idCounter++, title, description);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(String title, String description) {
        if (title == null || description == null) return null;
        Epic epic = new Epic(idCounter++, title, description);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createSubTask(String title, String description, long epicId) {
        if (title == null || description == null) return null;
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + epicId + " не существует");
        }
        SubTask subTask = new SubTask(idCounter++, title, description, epicId);
        subtasks.put(subTask.getId(), subTask);
        epic.addSubTask(subTask);
        return subTask;
    }

    public void addTask(Task task) {
        if (task != null) {
            tasks.put(task.getId(), task);
        }
    }

    public void addEpic(Epic epic) {
        if (epic != null) {
            epics.put(epic.getId(), epic);
            epic.updateStatus();
        }
    }

    public void addSubTask(SubTask subtask) {
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic == null) {
                throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует");
            }
            subtasks.put(subtask.getId(), subtask);
            epic.addSubTask(subtask);
            epic.updateStatus();
        }
    }

    public Task getTask(long id) {
        return tasks.get(id);
    }

    public Epic getEpic(long id) {
        return epics.get(id);
    }

    public SubTask getSubTask(long id) {
        return subtasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<SubTask> getSubTasksOfEpic(long epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        return epic.getSubTasks();
    }

    public void updateTask(Task updatedTask) {
        if (updatedTask != null && tasks.containsKey(updatedTask.getId())) {
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    public void updateSubTask(SubTask updatedSubTask) {
        if (updatedSubTask != null && subtasks.containsKey(updatedSubTask.getId())) {
            subtasks.put(updatedSubTask.getId(), updatedSubTask);
            Epic epic = epics.get(updatedSubTask.getEpicId());
            if (epic != null) {
                epic.updateStatus();
            }
        }
    }

    public void removeTask(long id) {
        tasks.remove(id);
    }

    public void removeEpic(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (SubTask sub : epic.getSubTasks()) {
                subtasks.remove(sub.getId());
            }
        }
    }

    public void removeSubTask(long id) {
        SubTask sub = subtasks.remove(id);
        if (sub != null) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.removeSubTask(sub);
                epic.updateStatus();
            }
        }
    }

    public void clearAll() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        idCounter = 0;
    }
}
