package ru.kanban.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    protected String readBody(HttpExchange h) throws IOException {
        try (InputStream is = h.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendText(HttpExchange h, int code, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendJson(HttpExchange h, int code, Object obj) throws IOException {
        sendText(h, code, gson.toJson(obj));
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, 404, "404: не найдено");
    }

    protected void sendHasOverlaps(HttpExchange h) throws IOException {
        sendText(h, 406, "406: задачи пересекаются");
    }

    protected void sendServerError(HttpExchange h) throws IOException {
        sendText(h, 500, "500: внутренняя ошибка сервера");
    }

    protected Integer extractId(HttpExchange h) {
        URI uri = h.getRequestURI();
        String[] parts = uri.getPath().split("/");
        parts = Arrays.stream(parts).filter(s -> !s.isBlank()).toArray(String[]::new);
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}

