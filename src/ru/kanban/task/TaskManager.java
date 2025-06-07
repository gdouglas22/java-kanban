package ru.kanban.task;

import org.jetbrains.annotations.NotNull;
import ru.kanban.util.Messages;

import java.util.*;

public class TaskManager {

    private final Map<Long, Task> tasks = new HashMap<>();
    private final Map<Long, Epic> epics = new HashMap<>();
    private final Map<Long, SubTask> subtasks = new HashMap<>();

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public Task getTaskById(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_TASK_NOT_FOUND, id));
        }
        return task;
    }

    public void addTask(Task task) {
        if (tasks.size() >= TaskConfig.maxTasks) {
            throw new IllegalStateException(Messages.ERROR_TASK_LIMIT_REACHED);
        }
        tasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NoSuchElementException(String.format(Messages.ERROR_TASK_NOT_FOUND, task.getId()));
        }
        tasks.put(task.getId(), task);
    }

    public void removeTaskById(long id) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException(String.format(Messages.ERROR_TASK_NOT_FOUND, id));
        }
        tasks.remove(id);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void clearEpics() {
        for (Epic epic : epics.values()) {
            for (Long subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
        epics.clear();
    }

    public Epic getEpicById(long id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_EPIC_NOT_FOUND, id));
        }
        return epic;
    }

    public void addEpic(Epic epic) {
        if (epics.size() >= TaskConfig.maxEpicTasks) {
            throw new IllegalStateException(Messages.ERROR_EPIC_LIMIT_REACHED);
        }
        epics.put(epic.getId(), epic);
    }

    public void removeEpicById(long id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_EPIC_NOT_FOUND, id));
        }
        for (Long subId : epic.getSubtaskIds()) {
            subtasks.remove(subId);
        }
    }

    public List<SubTask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        }
    }

    public SubTask getSubtaskById(long id) {
        SubTask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_SUBTASK_NOT_FOUND, id));
        }
        return subtask;
    }

    public void addSubtask(@NotNull SubTask subtask) {
        if (subtasks.size() >= TaskConfig.maxSubTasks) {
            throw new IllegalStateException(Messages.ERROR_SUBTASK_LIMIT_REACHED);
        }

        long epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException(String.format(Messages.ERROR_EPIC_NOT_FOUND, epicId));
        }

        if (epic.getSubtaskIds().size() >= TaskConfig.maxSubTasksPerEpic) {
            throw new IllegalStateException(String.format(Messages.ERROR_EPIC_SUBTASK_LIMIT_REACHED, epicId));
        }

        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
    }

    public void updateSubtask(@NotNull SubTask subtask) {
        long epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            throw new IllegalArgumentException(String.format(Messages.ERROR_EPIC_NOT_FOUND, epicId));
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(epicId));
    }

    public void removeSubtaskById(long id) {
        SubTask subtask = subtasks.remove(id);
        if (subtask == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_SUBTASK_NOT_FOUND, id));
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    public List<SubTask> getSubtasksOfEpic(long epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NoSuchElementException(String.format(Messages.ERROR_EPIC_NOT_FOUND, epicId));
        }

        List<SubTask> result = new ArrayList<>();
        for (Long subId : epic.getSubtaskIds()) {
            SubTask sub = subtasks.get(subId);
            if (sub != null) {
                result.add(sub);
            }
        }
        return result;
    }

    private void updateEpicStatus(@NotNull Epic epic) {
        List<Long> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.epicSetStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Long id : subIds) {
            SubTask sub = subtasks.get(id);
            if (sub == null) continue;
            if (sub.getStatus() != TaskStatus.NEW) allNew = false;
            if (sub.getStatus() != TaskStatus.DONE) allDone = false;
        }

        if (allDone) {
            epic.epicSetStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.epicSetStatus(TaskStatus.NEW);
        } else {
            epic.epicSetStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public int getTasksSize() {
        return tasks.size();
    }

    public int getEpicsSize() {
        return epics.size();
    }

    public int getSubtasksSize() {
        return subtasks.size();
    }
}

