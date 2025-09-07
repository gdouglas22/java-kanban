package ru.kanban.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;
import ru.kanban.task.SubTask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    public SubtasksHandler(TaskManager manager, Gson gson) { super(manager, gson); }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            switch (h.getRequestMethod()) {
                case "GET" -> {
                    Integer id = extractId(h);
                    if (id == null) {
                        sendJson(h, 200, manager.getAllSubTasks());
                    } else {
                        SubTask s = manager.getSubTask(id);
                        if (s == null) { sendNotFound(h); return; }
                        sendJson(h, 200, s);
                    }
                }
                case "POST" -> {
                    String body = readBody(h);
                    SubTask s = gson.fromJson(body, SubTask.class);
                    if (s == null) { sendServerError(h); return; }

                    if (s.getId() == 0) {
                        SubTask created = manager.createSubTask(s.getTitle(), s.getDescription(), s.getEpicId());
                        if (s.getStartTime() != null) created.setStartTime(s.getStartTime());
                        if (s.getDuration()  != null) created.setDuration(s.getDuration());
                        if (s.getStatus()    != null) created.setStatus(s.getStatus());
                        manager.updateSubTask(created);
                        sendJson(h, 201, created);
                    } else {
                        manager.updateSubTask(s);
                        sendJson(h, 201, s);
                    }
                }
                case "DELETE" -> {
                    Integer id = extractId(h);
                    if (id == null) { sendServerError(h); return; }
                    if (manager.getSubTask(id) == null) { sendNotFound(h); return; }
                    manager.removeSubTask(id);
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
