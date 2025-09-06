package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.kanban.manager.Managers;
import ru.kanban.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось стартовать сервер", e);
        }
        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(manager, gson));
        server.createContext("/epics", new EpicsHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    public static void main(String[] args) {
        TaskManager m = Managers.getDefault();
        HttpTaskServer app = new HttpTaskServer(m);
        app.start();
        System.out.println("HTTP server started on :8080");
    }
}

