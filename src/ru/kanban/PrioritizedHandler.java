package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;

public class PrioritizedHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager manager, Gson gson) {
    }
}
