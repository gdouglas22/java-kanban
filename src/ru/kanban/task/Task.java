package ru.kanban.task;

public class Task extends TaskBase{

    public Task(String title, String description) {
        super(title, description);
    }

    public Task(String title, String description, long id) {
        super(title, description, id);
    }
}
