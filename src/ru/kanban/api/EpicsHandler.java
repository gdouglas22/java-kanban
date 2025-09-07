package ru.kanban.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;
import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    public EpicsHandler(TaskManager manager, Gson gson) { super(manager, gson); }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            URI uri = h.getRequestURI();
            String[] parts = Arrays.stream(uri.getPath().split("/"))
                    .filter(s -> !s.isBlank()).toArray(String[]::new);

            boolean subPath = parts.length == 3 && "subtasks".equals(parts[2]);

            switch (method) {
                case "GET" -> {
                    if (subPath) {
                        Integer epicId = extractId(h);
                        Epic epic = manager.getEpic(epicId == null ? -1 : epicId);
                        if (epic == null) { sendNotFound(h); return; }
                        if (epicId != null) {
                            List<SubTask> list = manager.getSubTasksOfEpic(epicId);
                            sendJson(h, 200, list);
                        }
                        return;
                    }
                    Integer id = extractId(h);
                    if (id == null) {
                        sendJson(h, 200, manager.getAllEpics());
                    } else {
                        Epic e = manager.getEpic(id);
                        if (e == null) { sendNotFound(h); return; }
                        sendJson(h, 200, e);
                    }
                }
                case "POST" -> {
                    String body = readBody(h);
                    Epic e = gson.fromJson(body, Epic.class);
                    if (e == null) { sendServerError(h); return; }

                    if (e.getId() == 0) {
                        Epic created = manager.createEpic(e.getTitle(), e.getDescription());
                        sendJson(h, 201, created);
                    } else {
                        manager.addEpic(e);
                        sendJson(h, 201, e);
                    }
                }
                case "DELETE" -> {
                    Integer id = extractId(h);
                    if (id == null) { sendServerError(h); return; }
                    if (manager.getEpic(id) == null) { sendNotFound(h); return; }
                    manager.removeEpic(id);
                    sendText(h, 200, "ОК");
                }
                default -> sendServerError(h);
            }
        } catch (Exception e) {
            sendServerError(h);
        }
    }
}
