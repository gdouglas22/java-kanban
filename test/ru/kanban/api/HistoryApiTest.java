package ru.kanban.api;

import org.junit.jupiter.api.Test;
import ru.kanban.task.Task;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryApiTest extends ApiTest {

    @Test
    void historyReflectsReads_returns200() throws Exception {
        Task t1 = manager.createTask("A","d");
        Task t2 = manager.createTask("B","d");

        manager.getTask(t1.getId());
        manager.getTask(t2.getId());

        var resp = get("/history");
        assertEquals(200, resp.statusCode());
        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, arr.length);
    }
}