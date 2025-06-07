package ru.kanban.task;
import ru.kanban.util.Messages;

public abstract class TaskBase {

    protected final long id;
    protected String title;
    protected String description;
    protected TaskStatus status;

    public TaskBase(String title, String description) {
        validate(title, description);
        this.id = TaskIdGenerator.nextId();
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public TaskBase(String title, String description, long id) {
        validate(title, description);
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    private void validate(String title, String description) {
        if (title.length() > TaskConfig.maxTitleLength) {
            throw new IllegalArgumentException(
                    String.format(Messages.ERROR_TITLE_TOO_LONG, TaskConfig.maxTitleLength));
        }
        if (description.length() > TaskConfig.maxDescriptionLength) {
            throw new IllegalArgumentException(
                    String.format(Messages.ERROR_DESCRIPTION_TOO_LONG, TaskConfig.maxDescriptionLength));
        }
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaskBase that = (TaskBase) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}

