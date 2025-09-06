package ru.kanban;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.kanban.manager.TaskManager;

public class EpicsHandler implements HttpHandler {
    public EpicsHandler(TaskManager manager, Gson gson) {
    }
}
