package ru.kanban.manager;

import ru.kanban.task.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path filePath;


    public FileBackedTaskManager(Path filePath) {
        super();
        this.filePath = filePath;
    }

    protected void save() throws ManagerSaveException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("id,type,title,status,description,epic");
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
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getTitle()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");

        if (task instanceof SubTask) {
            sb.append(((SubTask) task).getEpicId());
        }
        return sb.toString();
    }

    public static FileBackedTaskManager loadFromFile(Path filePath) {
        FileBackedTaskManager manager = new FileBackedTaskManager(filePath);
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            reader.readLine();
            String line;
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
        }
    }

    private Task fromString(String line) {
        String[] parts = line.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        return switch (type) {
            case TASK -> new Task(id, title, description, status);
            case EPIC -> new Epic(id, title, description);
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                yield new SubTask(id, title, description, status, epicId);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        };
    }

    @Override
    public Task createTask(String title, String description) {
        if (title == null || description == null) return null;
        Task task = new Task(idCounter++, title, description);
        tasks.put(task.getId(), task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        if (title == null || description == null) return null;
        Epic epic = new Epic(idCounter++, title, description);
        epics.put(epic.getId(), epic);
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(String title, String description, int epicId) {
        if (title == null || description == null) return null;
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + epicId + " не существует");
        }
        SubTask subTask = new SubTask(idCounter++, title, description, epicId);
        subtasks.put(subTask.getId(), subTask);
        epic.addSubTaskId(subTask.getId());
        updateEpicStatus(epic);
        save();
        return subTask;
    }

    @Override
    public void addTask(Task task) {
        if (task != null) {
            tasks.put(task.getId(), task);
        }
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic != null) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
        save();
    }

    @Override
    public void addSubTask(SubTask subtask) {
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic == null) {
                throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не существует");
            }
            subtasks.put(subtask.getId(), subtask);
            epic.addSubTaskId(subtask.getId());
            updateEpicStatus(epic);
        }
        save();
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (updatedTask != null) {
            if (tasks.containsKey(updatedTask.getId())) {
                tasks.put(updatedTask.getId(), updatedTask);
            }
        }
        save();
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        if (updatedSubTask != null) {
            if (subtasks.containsKey(updatedSubTask.getId())) {
                subtasks.put(updatedSubTask.getId(), updatedSubTask);
                Epic epic = epics.get(updatedSubTask.getEpicId());
                if (epic != null) {
                    updateEpicStatus(epic);
                }
            }
        }
        save();
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            List<Integer> ids = epic.getSubTaskIds();
            for (Integer subId : ids) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
        historyManager.remove(id);
        save();
    }

    @Override
    public void removeSubTask(int id) {
        SubTask sub = subtasks.remove(id);
        if (sub != null) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epic);
            }
        }
        historyManager.remove(id);
        save();
    }

    @Override
    public void clearAll() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }

        tasks.clear();
        epics.clear();
        subtasks.clear();
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
        epic.updateStatus(subs);
        save();
    }
}
