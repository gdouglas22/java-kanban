package ru.kanban.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<SubTask> subTasks;

    public Epic(long id, String title, String description) {
        super(id, title, description);
        this.subTasks = new ArrayList<>();
        updateStatus();
    }

    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
        updateStatus();
    }

    public void removeSubTask(SubTask subTask) {
        subTasks.remove(subTask);
        updateStatus();
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void updateStatus() {
        if (subTasks.isEmpty()) {
            this.status = TaskStatus.NEW;
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (SubTask st : subTasks) {
            if (st.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (st.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            this.status = TaskStatus.DONE;
        } else if (allNew) {
            this.status = TaskStatus.NEW;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    public void replaceSubTask(SubTask subtask) {
        for (int i = 0; i < subTasks.size(); i++) {
            if (subTasks.get(i).getId() == subtask.getId()) {
                subTasks.set(i, subtask);
                return;
            }
        }

        addSubTask(subtask);
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Epic other = (Epic) obj;

        return id == other.id &&
                Objects.equals(title, other.getTitle()) &&
                Objects.equals(description, other.description) &&
                status == other.status &&
                Objects.equals(subTasks, other.subTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, subTasks);
    }


    @Override
    public String toString() {
        return "EpicTask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + getSubTasks() +
                '}';
    }


}

