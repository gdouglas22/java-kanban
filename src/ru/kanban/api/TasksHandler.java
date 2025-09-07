package ru.kanban.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.kanban.manager.TaskManager;
import ru.kanban.task.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager manager, Gson gson) { super(manager, gson); }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            switch (method) {
                case "GET" -> {
                    Integer id = extractId(h);
                    if (id == null) {
                        sendJson(h, 200, manager.getAllTasks());
                    } else {
                        Task t = manager.getTask(id);
                        if (t == null) { sendNotFound(h); return; }
                        sendJson(h, 200, t);
                    }
                }
                case "POST" -> {
                    String body = readBody(h);
                    Task t = gson.fromJson(body, Task.class);
                    if (t == null) { sendServerError(h); return; }

                    if (t.getId() == 0) {
                        Task created = manager.createTask(t.getTitle(), t.getDescription());
                        if (t.getStartTime() != null) created.setStartTime(t.getStartTime());
                        if (t.getDuration()  != null) created.setDuration(t.getDuration());
                        if (t.getStatus()    != null) created.setStatus(t.getStatus());
                        manager.updateTask(created);
                        sendJson(h, 201, created);
                    } else {
                        try {
                            manager.updateTask(t);
                            sendJson(h, 201, t);
                        } catch (IllegalArgumentException e) {
                            sendHasOverlaps(h);
                        }
                    }
                }
                case "DELETE" -> {
                    Integer id = extractId(h);
                    if (id == null) { sendServerError(h); return; }
                    if (manager.getTask(id) == null) { sendNotFound(h); return; }
                    manager.removeTask(id);
                    sendText(h, 200, "ОК");
                }
                default -> sendServerError(h);
            }
        } catch (IllegalArgumentException e) {
            sendHasOverlaps(h);
        } catch (Exception e) {
            sendServerError(h);
        }
    }
}
