package ru.kanban.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TaskTest {

    @Test
    void shouldReturnTitle() {
        Task t1 = new Task(1, "t1", "desc1");
        assertEquals("t1", t1.getTitle());
    }

    @Test
    void shouldReturnDescription() {
        Task t1 = new Task(1, "t1", "desc1");
        assertEquals("desc1", t1.getDescription());
    }

    @Test
    void shouldHaveNewStatusByDefault() {
        Task t1 = new Task(1, "t1", "desc1");
        assertEquals(TaskStatus.NEW, t1.getStatus());
    }

    @Test
    void shouldSetTitle() {
        Task t1 = new Task(1, "t1", "desc1");
        t1.setTitle("t2");
        assertEquals("t2", t1.getTitle());
    }

    @Test
    void shouldSetDescription() {
        Task t1 = new Task(1, "t1", "desc1");
        t1.setDescription("desc2");
        assertEquals("desc2", t1.getDescription());
    }

    @Test
    void shouldSetStatus() {
        Task t1 = new Task(1, "t1", "desc1");
        t1.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, t1.getStatus());
    }

    @Test
    void shouldBeEqualIfSameFields() {
        Task t1 = new Task(1, "t1", "desc1");
        Task t2 = new Task(1, "t1", "desc1");
        assertEquals(t1, t2);
    }

    @Test
    void shouldHaveSameHashCodeIfEqual() {
        Task t1 = new Task(1, "t1", "desc1");
        Task t2 = new Task(1, "t1", "desc1");
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void shouldContainAllFieldsInToString() {
        Task t1 = new Task(1, "t1", "desc1");
        String s = t1.toString();
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("t1"));
        assertTrue(s.contains("desc1"));
        assertTrue(s.contains("NEW"));
    }
}
