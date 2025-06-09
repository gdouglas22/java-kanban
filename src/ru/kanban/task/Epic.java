package ru.kanban.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Long> subTaskIds = new ArrayList<>();

    public Epic(long id, String title, String description) {
        super(id, title, description);
        this.status = TaskStatus.NEW;
    }

    public void addSubTaskId(long subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubTaskId(long subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    public List<Long> getSubTaskIds() {
        return subTaskIds;
    }

    public void updateStatus(List<SubTask> subTasks) {
        if (subTasks.isEmpty()) {
            this.status = TaskStatus.NEW;
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (SubTask st : subTasks) {
            if (st.getStatus() != TaskStatus.NEW) allNew = false;
            if (st.getStatus() != TaskStatus.DONE) allDone = false;
        }

        if (allDone) {
            this.status = TaskStatus.DONE;
        } else if (allNew) {
            this.status = TaskStatus.NEW;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subTaskIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic epic)) return false;
        return id == epic.id &&
                Objects.equals(title, epic.title) &&
                Objects.equals(description, epic.description) &&
                status == epic.status &&
                Objects.equals(subTaskIds, epic.subTaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, subTaskIds);
    }
}

