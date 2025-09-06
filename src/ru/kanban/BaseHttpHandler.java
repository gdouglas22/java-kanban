package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager; this.gson = gson;
    }

    protected void sendJson(HttpExchange h, int code, Object body) throws IOException {
        byte[] resp = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendText(HttpExchange h, int code, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected Integer pathId(HttpExchange h) {
        String[] parts = h.getRequestURI().getPath().split("/");
        try { return parts.length >= 3 ? Integer.parseInt(parts[2]) : null; }
        catch (NumberFormatException e) { return null; }
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, 404, "{\"Ошибка 404\":\"Не найдено\"}");
    }

    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        sendText(h, 406, "{\"Ошибка 406\":\"Ошибка в задаче\"}");
    }

    protected void sendServerError(HttpExchange h) throws IOException {
        sendText(h, 500, "{\"Ошибка 500\":\"Внутренняя ошибка сервера\"}");
    }
}
