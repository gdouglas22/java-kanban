package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;

public class SubtasksHandler implements HttpHandler {
    public SubtasksHandler(TaskManager manager, Gson gson) {
    }
}
