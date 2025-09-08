package ru.kanban.manager;

import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.Task;

import java.util.List;

public interface TaskManager {
    Task createTask(String title, String description);

    Epic createEpic(String title, String description);

    SubTask createSubTask(String title, String description, int epicId);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subtask);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    List<SubTask> getSubTasksOfEpic(int epicId);

    void updateTask(Task updatedTask);

    void updateSubTask(SubTask updatedSubTask);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubTask(int id);

    void clearAll();

    List<Task> getHistory();
}
