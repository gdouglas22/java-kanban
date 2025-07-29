package ru.kanban.task;

import java.util.Objects;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(int id, String title, String description, int epicId) {
        super(id, title, description);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    public SubTask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description);
        this.epicId = epicId;
        this.status = status;
        this.type = TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SubTask other = (SubTask) obj;

        return id == other.id &&
                epicId == other.epicId &&
                Objects.equals(title, other.getTitle()) &&
                Objects.equals(description, other.description) &&
                status == other.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, epicId);
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + getEpicId() +
                '}';
    }
}

