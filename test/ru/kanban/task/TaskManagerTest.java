package ru.kanban.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = new TaskManager();
    }

    @Test
    void addAndGetTask() {
        Task task1 = new Task("task1", "desc");
        manager.addTask(task1);
        Task result = manager.getTaskById(task1.getId());
        assertEquals(task1, result);
    }

    @Test
    void updateTask() {
        Task task1 = new Task("task1", "desc");
        manager.addTask(task1);

        Task updated = new Task("task1 new", "desc new", task1.getId());
        updated.setStatus(TaskStatus.DONE);

        manager.updateTask(updated);
        Task result = manager.getTaskById(task1.getId());

        assertEquals("task1 new", result.getTitle());
        assertEquals("desc new", result.getDescription());
        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    void removeTask() {
        Task task1 = new Task("task1", "desc");
        manager.addTask(task1);
        manager.removeTaskById(task1.getId());

        List<Task> tasks = manager.getAllTasks();
        assertTrue(tasks.isEmpty());
    }

    @Test
    void addAndGetEpic() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        Epic result = manager.getEpicById(epic1.getId());
        assertEquals(epic1, result);
    }

    @Test
    void removeEpicAlsoRemovesSubtasks() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        SubTask subtask1 = new SubTask("sub1", "desc", epic1.getId());
        manager.addSubtask(subtask1);

        manager.removeEpicById(epic1.getId());

        List<Epic> epics = manager.getAllEpics();
        List<SubTask> subtasks = manager.getAllSubtasks();

        assertTrue(epics.isEmpty());
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void addSubtasksAndUpdateEpicStatus() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        SubTask subtask1 = new SubTask("sub1", "desc", epic1.getId());
        SubTask subtask2 = new SubTask("sub2", "desc", epic1.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        Epic result1 = manager.getEpicById(epic1.getId());
        assertEquals(TaskStatus.IN_PROGRESS, result1.getStatus());

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        Epic result2 = manager.getEpicById(epic1.getId());
        assertEquals(TaskStatus.DONE, result2.getStatus());
    }

    @Test
    void getSubtasksOfEpic() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        SubTask subtask1 = new SubTask("sub1", "desc", epic1.getId());
        manager.addSubtask(subtask1);

        List<SubTask> subtasks = manager.getSubtasksOfEpic(epic1.getId());

        assertEquals(1, subtasks.size());
        assertEquals(subtask1, subtasks.getFirst());
    }

    @Test
    void getTaskByIdNotFound() {
        try {
            manager.getTaskById(999);
            fail("Expected exception not thrown");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("999"));
        }
    }

    @Test
    void getEpicByIdNotFound() {
        try {
            manager.getEpicById(123);
            fail("Expected exception not thrown");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("123"));
        }
    }

    @Test
    void getSubtaskByIdNotFound() {
        try {
            manager.getSubtaskById(456);
            fail("Expected exception not thrown");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("456"));
        }
    }

    @Test
    void clearEpicsAndSubtasks() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        SubTask subtask1 = new SubTask("sub1", "desc", epic1.getId());
        manager.addSubtask(subtask1);

        Epic epic2 = new Epic("epic2", "desc");
        manager.addEpic(epic2);

        SubTask subtask2 = new SubTask("sub2", "desc", epic1.getId());
        manager.addSubtask(subtask2);

        assertEquals(2, manager.getSubtasksSize());
        assertEquals(2, manager.getEpicsSize());

        manager.clearSubtasks();
        manager.clearEpics();

        assertEquals(0, manager.getSubtasksSize());
        assertEquals(0, manager.getEpicsSize());
    }

    @Test
    void clearTasks() {
        Task task1 = new Task("task1", "desc");
        manager.addTask(task1);
        Task task2 = new Task("task1", "desc");
        manager.addTask(task2);
        Task task3 = new Task("task1", "desc");
        manager.addTask(task3);

        assertEquals(3, manager.getTasksSize());

        manager.clearTasks();

        assertEquals(0, manager.getTasksSize());
    }

    @Test
    void removeSubtaskById() {
        Epic epic1 = new Epic("epic1", "desc");
        manager.addEpic(epic1);

        SubTask subtask1 = new SubTask("sub1", "desc", epic1.getId());
        manager.addSubtask(subtask1);

        assertEquals(1, manager.getSubtasksSize());
        assertEquals(1, manager.getEpicsSize());

        manager.removeSubtaskById(subtask1.getId());

        assertEquals(0, manager.getSubtasksSize());
        assertEquals(1, manager.getEpicsSize());
    }
}

