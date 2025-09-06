package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;

public class HistoryHandler implements HttpHandler {
    public HistoryHandler(TaskManager manager, Gson gson) {
    }
}
