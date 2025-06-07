package ru.kanban.task;

import org.jetbrains.annotations.NotNull;
import ru.kanban.util.Messages;

import java.util.ArrayList;
import java.util.List;

public class Epic extends TaskBase {

    private final List<Long> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(String title, String description, List<Long> subtaskIds) {
        super(title, description);
        if (subtaskIds != null) {
            this.subtaskIds.addAll(subtaskIds);
        }
    }

    public Epic(String title, String description, long @NotNull ... subtaskIds) {
        super(title, description);
        for (long id : subtaskIds) {
            this.subtaskIds.add(id);
        }
    }

    public List<Long> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(long subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(long subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException(Messages.ERROR_EPIC_STATUS_SET_MANUALLY);
    }

    // обновление статуса через метод с package-private
    void epicSetStatus(TaskStatus status) {
        super.setStatus(status);
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}

