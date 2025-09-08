package ru.kanban.api;

import org.junit.jupiter.api.Test;
import ru.kanban.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedApiTest extends ApiTest {

    @Test
    void prioritizedReturnsSortedByStartTime_returns200() throws Exception {
        Task a = manager.createTask("A","d");
        a.setStartTime(LocalDateTime.now().plusMinutes(30));
        a.setDuration(Duration.ofMinutes(5));
        manager.updateTask(a);

        Task b = manager.createTask("B","d");
        b.setStartTime(LocalDateTime.now().plusMinutes(10));
        b.setDuration(Duration.ofMinutes(5));
        manager.updateTask(b);

        var resp = get("/prioritized");
        assertEquals(200, resp.statusCode());

        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, arr.length);
        assertEquals(b.getId(), arr[0].getId());
    }
}
