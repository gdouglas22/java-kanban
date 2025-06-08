package ru.kanban.task;

import java.util.Objects;

public class SubTask extends Task {
    private final long epicId;

    public SubTask(long id, String title, String description, long epicId) {
        super(id, title, description);
        this.epicId = epicId;
    }

    public long getEpicId() {
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

