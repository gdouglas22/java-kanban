package ru.kanban.manager;

import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.Task;

import java.util.List;

public interface TaskManager {
    Task createTask(String title, String description);

    Epic createEpic(String title, String description);

    SubTask createSubTask(String title, String description, long epicId);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subtask);

    Task getTask(long id);

    Epic getEpic(long id);

    SubTask getSubTask(long id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    List<SubTask> getSubTasksOfEpic(long epicId);

    void updateTask(Task updatedTask);

    void updateSubTask(SubTask updatedSubTask);

    void removeTask(long id);

    void removeEpic(long id);

    void removeSubTask(long id);

    void clearAll();

    List<Task> getHistory();
}
