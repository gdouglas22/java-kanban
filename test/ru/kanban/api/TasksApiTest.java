package ru.kanban.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import ru.kanban.task.Task;
import ru.kanban.task.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

public class TasksApiTest extends ApiTest {

    @Test
    void createTask_returns201_andAppearsInManager() throws Exception {
        Task t = new Task(0,"t", "T", TaskStatus.NEW);
        t.setDuration(Duration.ofMinutes(5));
        t.setStartTime(LocalDateTime.now().plusMinutes(10));
        String json = gson.toJson(t);

        var resp = post("/tasks", json);
        assertEquals(201, resp.statusCode(), "201");

        List<Task> all = manager.getAllTasks();
        assertNotNull(all);
        assertEquals(1, all.size());
        assertEquals("t", all.getFirst().getTitle());
    }

    @Test
    void getTaskById_returns200_andBody() throws Exception {
        Task created = manager.createTask("A","desc");
        var resp = get("/tasks/"+created.getId());
        assertEquals(200, resp.statusCode());
        Task fromJson = gson.fromJson(resp.body(), Task.class);
        assertEquals(created.getId(), fromJson.getId());
        assertEquals("A", fromJson.getTitle());
    }

    @Test
    void updateTask_returns201_andChangesApplied() throws Exception {
        Task created = manager.createTask("A","d");
        created.setStatus(TaskStatus.IN_PROGRESS);
        String json = gson.toJson(created);

        var resp = post("/tasks", json);
        assertEquals(201, resp.statusCode());

        Task reloaded = manager.getTask(created.getId());
        assertEquals(TaskStatus.IN_PROGRESS, reloaded.getStatus());
    }

    @Test
    void deleteTask_returns200_andRemoves() throws Exception {
        Task created = manager.createTask("A","d");
        var resp = delete("/tasks/"+created.getId());
        assertEquals(200, resp.statusCode());
        assertNull(manager.getTask(created.getId()));
    }

    @Test
    void getMissingTask_returns404() throws Exception {
        var resp = get("/tasks/9999");
        assertEquals(404, resp.statusCode());
    }

    @Test
    void createOverlappingTask_returns406() throws Exception {
        Task a = manager.createTask("A","d");
        a.setStartTime(LocalDateTime.now().plusMinutes(5));
        a.setDuration(Duration.ofMinutes(30));
        manager.updateTask(a);

        Task b = new Task(0,"B", "d", TaskStatus.NEW);
        b.setDuration(Duration.ofMinutes(5));
        b.setStartTime(LocalDateTime.now().plusMinutes(10));
        String json = gson.toJson(b);

        var resp = post("/tasks", json);
        assertEquals(406, resp.statusCode(), "406");
    }
}

