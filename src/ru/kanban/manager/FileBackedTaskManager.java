package ru.kanban.manager;

import ru.kanban.task.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path filePath;

    public FileBackedTaskManager(Path filePath) {
        super();
        this.filePath = filePath;
    }

    protected void save() throws ManagerSaveException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("id,type,title,status,description,startTime,durationInMinutes,epicId\n");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (SubTask subTask : getAllSubTasks()) {
                writer.write(toString(subTask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + filePath, e);
        }
    }

    private String toString(Task task) {
        String start = task.getStartTime() == null ? "" : task.getStartTime().toString();
        String dur   = task.getDuration() == null ? "" : Long.toString(task.getDuration().toMinutes());

        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",")
                .append(task.getType()).append(",")
                .append(task.getTitle()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",")
                .append(start).append(",")
                .append(dur).append(",");

        if (task instanceof SubTask) {
            sb.append(((SubTask) task).getEpicId());
        }
        return sb.toString();
    }

    public static FileBackedTaskManager loadFromFile(Path filePath) {
        FileBackedTaskManager manager = new FileBackedTaskManager(filePath);
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            reader.readLine();
            reader.readLine();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                try {
                    Task task = manager.fromString(line);
                    manager.addWithoutSaving(task);
                } catch (Exception parseException) {
                    throw new ManagerSaveException("Ошибка при разборе строки: " + line, parseException);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла: " + filePath, e);
        }

        for (Epic e : manager.getAllEpics()) {
            e.recalcTimeFields(manager.subtasks);
        }

        manager.prioritizedTasks.clear();
        manager.tasks.values().forEach(manager::indexForPriority);
        manager.subtasks.values().forEach(manager::indexForPriority);

        return manager;
    }

    private void addWithoutSaving(Task task) {
        if (task == null) return;

        int id = task.getId();
        if (id > idCounter) {
            idCounter = id + 1;
        }

        switch (task.getType()) {
            case TASK -> tasks.put(id, task);
            case EPIC -> epics.put(id, (Epic) task);
            case SUBTASK -> {
                SubTask subtask = (SubTask) task;
                subtasks.put(id, subtask);
                Epic epic = epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubTaskId(id);
                    updateEpicStatus(epic);
                }
            }
            default -> throw new IllegalArgumentException(
                    "Неизвестный тип задачи: " + task.getType()
            );
        }
    }

    private Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        String startStr = parts.length > 5 ? parts[5] : "";
        String durStr = parts.length > 6 ? parts[6] : "";
        LocalDateTime start = startStr.isBlank() ? null : LocalDateTime.parse(startStr);
        Duration duration = durStr.isBlank() ? null : Duration.ofMinutes(Long.parseLong(durStr));

        return switch (type) {
            case TASK -> {
                Task t = new Task(id, title, description, status);
                t.setStartTime(start);
                t.setDuration(duration);
                yield t;
            }
            case EPIC -> {
                Epic e = new Epic(id, title, description);
                e.setStatus(status);
                yield e;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[7]);
                SubTask s = new SubTask(id, title, description, status, epicId);
                s.setStartTime(start);
                s.setDuration(duration);
                yield s;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        };
    }

    @Override
    public Task createTask(String title, String description) {
        if (title == null || description == null) return null;
        Task task = new Task(idCounter++, title, description);
        validateNoOverlap(task);
        tasks.put(task.getId(), task);
        indexForPriority(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        if (title == null || description == null) return null;
        Epic epic = new Epic(idCounter++, title, description);
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(String title, String description, int epicId) {
        if (title == null || description == null) return null;
        Epic epic = epics.get(epicId);
        if (epic == null) throw new IllegalArgumentException("Эпик с id " + epicId + " не существует");

        SubTask subTask = new SubTask(idCounter++, title, description, epicId);
        validateNoOverlap(subTask);
        subtasks.put(subTask.getId(), subTask);
        epic.addSubTaskId(subTask.getId());
        indexForPriority(subTask);
        updateEpicStatus(epic);
        save();
        return subTask;
    }

    @Override
    public void addTask(Task task) throws ManagerSaveException {
        if (task == null) return;
        validateNoOverlap(task);
        tasks.put(task.getId(), task);
        indexForPriority(task);
        save();
    }

    @Override
    public void addSubTask(SubTask subtask) {
        if (subtask == null) return;
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует");
        }
        validateNoOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubTaskId(subtask.getId());
        indexForPriority(subtask);
        updateEpicStatus(epic);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic != null) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
            save();
        }
    }

    @Override
    public void updateTask(Task updated) {
        if (updated == null) return;
        Task old = tasks.get(updated.getId());
        if (old == null) return;

        deindexForPriority(old);
        validateNoOverlap(updated);
        tasks.put(updated.getId(), updated);
        indexForPriority(updated);
        save();
    }

    @Override
    public void updateSubTask(SubTask updated) {
        if (updated == null) return;
        SubTask old = subtasks.get(updated.getId());
        if (old == null) return;

        deindexForPriority(old);
        validateNoOverlap(updated);
        subtasks.put(updated.getId(), updated);
        indexForPriority(updated);

        Epic epic = epics.get(updated.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        save();
    }

    @Override
    public void removeTask(int id) {
        Task t = tasks.remove(id);
        if (t != null) {
            deindexForPriority(t);
            historyManager.remove(id);
        }
        save();
    }

    @Override
    public void removeSubTask(int id) {
        SubTask st = subtasks.remove(id);
        if (st != null) {
            deindexForPriority(st);
            Epic epic = epics.get(st.getEpicId());
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
        save();
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;
        new ArrayList<>(epic.getSubTaskIds()).stream()
                .map(subtasks::remove)
                .filter(Objects::nonNull)
                .peek(this::deindexForPriority)
                .map(Task::getId)
                .forEach(historyManager::remove);
        historyManager.remove(id);
        save();
    }


    @Override
    public void clearAll() {
        tasks.values().forEach(this::deindexForPriority);
        subtasks.values().forEach(this::deindexForPriority);
        tasks.clear();
        epics.clear();
        subtasks.clear();
        historyManager.getHistory().forEach(t -> historyManager.remove(t.getId()));
        idCounter = 0;
        save();
    }

    private void updateEpicStatus(Epic epic) {
        List<SubTask> subs = new ArrayList<>();
        List<Integer> ids = epic.getSubTaskIds();
        for (Integer id : ids) {
            SubTask s = subtasks.get(id);
            if (s != null) {
                subs.add(s);
            }
        }
        epic.recalcTimeFields(subtasks);
        epic.updateStatus(subs);
    }
}
