package ru.kanban.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.manager.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = new TaskManager();
    }

    @Test
    void createTask() {
        Task task1 = manager.createTask("task1", "desc1");
        assertNotNull(task1);
        assertEquals("task1", task1.getTitle());
        assertEquals("desc1", task1.getDescription());
        assertEquals(TaskStatus.NEW, task1.getStatus());
        assertEquals(task1, manager.getTask(task1.getId()));
    }

    @Test
    void createEpic() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        assertNotNull(epic1);
        assertEquals("epic1", epic1.getTitle());
        assertEquals(TaskStatus.NEW, epic1.getStatus());
    }

    @Test
    void createSubTask() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = manager.createSubTask("subtask1", "desc1", epic1.getId());

        assertNotNull(subtask1);
        assertEquals(epic1.getId(), subtask1.getEpicId());
        List<SubTask> subtasks = manager.getSubTasksOfEpic(epic1.getId());
        assertEquals(1, subtasks.size());
        assertEquals(subtask1, subtasks.getFirst());
    }

    @Test
    void addTask() {
        Task task1 = new Task(100L, "task1", "desc1");
        manager.addTask(task1);
        assertEquals(task1, manager.getTask(100L));
    }

    @Test
    void addEpic() {
        Epic epic1 = new Epic(200L, "epic1", "desc1");
        manager.addEpic(epic1);
        assertEquals(epic1, manager.getEpic(200L));
    }

    @Test
    void addSubTask() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = new SubTask(300L, "subtask1", "desc1", epic1.getId());
        manager.addSubTask(subtask1);
        assertEquals(subtask1, manager.getSubTask(300L));
    }

    @Test
    void updateTask() {
        Task task1 = manager.createTask("task1", "desc1");
        task1.setTitle("task1-updated");
        task1.setDescription("task1-updated-desc");
        manager.updateTask(task1);
        assertEquals("task1-updated", manager.getTask(task1.getId()).getTitle());
    }

    @Test
    void updateSubTask() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = manager.createSubTask("subtask1", "desc1", epic1.getId());
        SubTask subtask2 = manager.createSubTask("subtask2", "desc2", epic1.getId());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(subtask1);
        manager.updateSubTask(subtask2);

        assertEquals(TaskStatus.DONE, manager.getEpic(epic1.getId()).getStatus());
    }

    @Test
    void updateEpic() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = manager.createSubTask("subtask1", "desc1", epic1.getId());
        SubTask subtask2 = manager.createSubTask("subtask2", "desc2", epic1.getId());

        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(subtask1);
        manager.updateSubTask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic1.getId()).getStatus());
    }

    @Test
    void removeTask() {
        Task task1 = manager.createTask("task1", "desc1");
        manager.removeTask(task1.getId());
        assertNull(manager.getTask(task1.getId()));
    }

    @Test
    void removeSubTask() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = manager.createSubTask("subtask1", "desc1", epic1.getId());
        manager.removeSubTask(subtask1.getId());
        assertTrue(manager.getSubTasksOfEpic(epic1.getId()).isEmpty());
    }

    @Test
    void removeEpic() {
        Epic epic1 = manager.createEpic("epic1", "desc1");
        SubTask subtask1 = manager.createSubTask("subtask1", "desc1", epic1.getId());
        SubTask subtask2 = manager.createSubTask("subtask2", "desc2", epic1.getId());

        manager.removeEpic(epic1.getId());

        assertNull(manager.getEpic(epic1.getId()));
        assertNull(manager.getSubTask(subtask1.getId()));
        assertNull(manager.getSubTask(subtask2.getId()));
    }

    @Test
    void clearAll() {
        manager.createTask("task1", "desc1");
        Epic epic1 = manager.createEpic("epic1", "desc1");
        manager.createSubTask("subtask1", "desc1", epic1.getId());

        manager.clearAll();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }
}
