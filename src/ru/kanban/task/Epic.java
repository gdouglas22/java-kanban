package ru.kanban.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(int id, String title, String description) {
        super(id, title, description);
        this.status = TaskStatus.NEW;
        this.type = TaskType.EPIC;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {}

    @Override
    public void setDuration(Duration duration) {}

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void recalcTimeFields(Map<Integer, SubTask> subtasks) {
        if (subTaskIds.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
        } else {

            Duration total = Duration.ZERO;
            LocalDateTime minStart = null;
            LocalDateTime maxEnd = null;

            for (Integer id : subTaskIds) {
                SubTask s = subtasks.get(id);
                if (s == null) continue;

                if (s.getDuration() != null) total = total.plus(s.getDuration());

                LocalDateTime sStart = s.getStartTime();
                LocalDateTime sEnd = s.getEndTime();

                if (sStart != null) {
                    minStart = (minStart == null || sStart.isBefore(minStart)) ? sStart : minStart;
                }
                if (sEnd != null) {
                    maxEnd = (maxEnd == null || sEnd.isAfter(maxEnd)) ? sEnd : maxEnd;
                }
            }

            this.duration = total;
            this.startTime = minStart;
            this.endTime = maxEnd;
        }
    }

    public void addSubTaskId(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubTaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }

    public List<Integer> getSubTaskIds() {
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

