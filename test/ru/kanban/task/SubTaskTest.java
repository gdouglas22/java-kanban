package ru.kanban.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    void shouldReturnTitle() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        assertEquals("s1", s1.getTitle());
    }

    @Test
    void shouldReturnDescription() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        assertEquals("desc1", s1.getDescription());
    }

    @Test
    void shouldReturnEpicId() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        assertEquals(100, s1.getEpicId());
    }

    @Test
    void shouldHaveNewStatusByDefault() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        assertEquals(TaskStatus.NEW, s1.getStatus());
    }

    @Test
    void shouldSetTitle() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        s1.setTitle("s2");
        assertEquals("s2", s1.getTitle());
    }

    @Test
    void shouldSetDescription() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        s1.setDescription("desc2");
        assertEquals("desc2", s1.getDescription());
    }

    @Test
    void shouldSetStatus() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        s1.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, s1.getStatus());
    }

    @Test
    void shouldBeEqualIfSameFields() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        SubTask s2 = new SubTask(1, "s1", "desc1", 100);
        assertEquals(s1, s2);
    }

    @Test
    void shouldContainAllFieldsInToString() {
        SubTask s1 = new SubTask(1, "s1", "desc1", 100);
        String s = s1.toString();
        assertTrue(s.contains("s1"));
        assertTrue(s.contains("desc1"));
        assertTrue(s.contains("100"));
        assertTrue(s.contains("NEW"));
    }
}
