package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.kanban.manager.TaskManager;
import ru.kanban.task.Task;

import java.io.IOException;

class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager manager, Gson gson) { super(manager, gson); }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            Integer id = pathId(h);

            switch (method) {
                case "GET" -> {
                    if (id == null) {
                        sendJson(h, 200, manager.getAllTasks());
                    } else {
                        Task t = manager.getTask(id);
                        sendJson(h, 200, t);
                    }
                }
                case "POST" -> {
                    String body = readBody(h);
                    Task t = gson.fromJson(body, Task.class);
                    if (t.getId() == 0) {
                        try {
                            manager.addTask(t);
                            sendJson(h, 201, t);
                        } catch (IllegalArgumentException e) {
                            sendHasOverlaps(h);
                        }
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
                    if (id == null) { sendNotFound(h); return; }
                    manager.removeTask(id);
                    sendText(h, 200, "{\"status\":\"deleted\"}");
                }
                default -> sendText(h, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (IllegalArgumentException e) {
            sendNotFound(h);
        } catch (Exception e) {
            sendServerError(h);
        }
    }
}
