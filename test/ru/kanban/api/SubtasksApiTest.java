package ru.kanban.api;

import org.junit.jupiter.api.Test;
import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksApiTest extends ApiTest {

    @Test
    void createSubtask_returns201_andAppearsUnderEpic() throws Exception {
        Epic epic = manager.createEpic("E","d");

        SubTask s = new SubTask(0, "S","d", TaskStatus.NEW, epic.getId());
        s.setDuration(Duration.ofMinutes(5));
        s.setStartTime(LocalDateTime.now().plusMinutes(5));
        var resp = post("/subtasks", gson.toJson(s));
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getSubTasksOfEpic(epic.getId()).size());
    }

    @Test
    void getSubtaskById_returns200() throws Exception {
        Epic e = manager.createEpic("E","d");
        SubTask s = manager.createSubTask("S","d", e.getId());
        var resp = get("/subtasks/"+s.getId());
        assertEquals(200, resp.statusCode());
        SubTask back = gson.fromJson(resp.body(), SubTask.class);
        assertEquals(s.getId(), back.getId());
    }

    @Test
    void deleteSubtask_returns200() throws Exception {
        Epic e = manager.createEpic("E","d");
        SubTask s = manager.createSubTask("S","d", e.getId());
        var resp = delete("/subtasks/"+s.getId());
        assertEquals(200, resp.statusCode());
        assertNull(manager.getSubTask(s.getId()));
    }

    @Test
    void getMissingSubtask_returns404() throws Exception {
        var resp = get("/subtasks/999");
        assertEquals(404, resp.statusCode());
    }
}