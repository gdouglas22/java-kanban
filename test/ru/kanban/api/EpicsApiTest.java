package ru.kanban.api;

import org.junit.jupiter.api.Test;
import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;

import java.time.Duration;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

public class EpicsApiTest extends ApiTest {

    @Test
    void createEpic_returns201_andManagerHasIt() throws Exception {
        Epic e = new Epic(1, "e","desc");
        var resp = post("/epics", gson.toJson(e));
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getAllEpics().size());
    }

    @Test
    void getEpicSubtasks_returns200_andEmptyInitially() throws Exception {
        Epic e = manager.createEpic("E","d");
        var resp = get("/epics/"+e.getId()+"/subtasks");
        assertEquals(200, resp.statusCode());
        SubTask[] list = gson.fromJson(resp.body(), SubTask[].class);
        assertEquals(0, list.length);
    }

    @Test
    void deleteEpic_returns200_andRemovesChildrenToo() throws Exception {
        Epic e = manager.createEpic("E","d");
        SubTask s = manager.createSubTask("S","d", e.getId());
        s.setStartTime(LocalDateTime.now().plusMinutes(30));
        s.setDuration(Duration.ofMinutes(10));
        manager.updateSubTask(s);

        var resp = delete("/epics/"+e.getId());
        assertEquals(200, resp.statusCode());
        assertNull(manager.getEpic(e.getId()));
        assertNull(manager.getSubTask(s.getId()));
    }

    @Test
    void getMissingEpic_returns404() throws Exception {
        var resp = get("/epics/999");
        assertEquals(404, resp.statusCode());
    }
}