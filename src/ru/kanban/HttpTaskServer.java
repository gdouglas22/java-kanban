package ru.kanban;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.kanban.api.*;
import ru.kanban.manager.Managers;
import ru.kanban.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer server;
    private final TaskManager manager;
    private static final Gson GSON = buildGson();

    public int getPort() {
        return port;
    }

    private final int port;

    public HttpTaskServer(TaskManager manager, int port) {
        this.manager = manager;
        this.port = port;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при старте", e);
        }
        registerContexts();
    }

    private void registerContexts() {
        server.createContext("/tasks",       new TasksHandler(manager, GSON));
        server.createContext("/subtasks",    new SubtasksHandler(manager, GSON));
        server.createContext("/epics",       new EpicsHandler(manager, GSON));
        server.createContext("/history",     new HistoryHandler(manager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(manager, GSON));
    }

    public void start() { server.start(); }
    public void stop()  { server.stop(0); }

    public static Gson getGson() { return GSON; }

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager, 8080);
        server.start();
        System.out.println("HTTP сервер запущен на http://localhost:" + server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
