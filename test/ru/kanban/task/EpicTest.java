package ru.kanban.task;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void shouldReturnTitle() {
        Epic e1 = new Epic(1, "e1", "desc1");
        assertEquals("e1", e1.getTitle());
    }

    @Test
    void shouldReturnDescription() {
        Epic e1 = new Epic(1, "e1", "desc1");
        assertEquals("desc1", e1.getDescription());
    }

    @Test
    void shouldHaveNewStatusByDefault() {
        Epic e1 = new Epic(1, "e1", "desc1");
        assertEquals(TaskStatus.NEW, e1.getStatus());
    }

    @Test
    void shouldSetTitle() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.setTitle("e2");
        assertEquals("e2", e1.getTitle());
    }

    @Test
    void shouldSetDescription() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.setDescription("desc2");
        assertEquals("desc2", e1.getDescription());
    }

    @Test
    void shouldSetStatus() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, e1.getStatus());
    }

    @Test
    void shouldBeEqualIfSameFields() {
        Epic e1 = new Epic(1, "e1", "desc1");
        Epic e2 = new Epic(1, "e1", "desc1");
        assertEquals(e1, e2);
    }

    @Test
    void shouldContainAllFieldsInToString() {
        Epic e1 = new Epic(1, "e1", "desc1");
        String s = e1.toString();
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("e1"));
        assertTrue(s.contains("NEW"));
        assertTrue(s.contains("subtaskIds"));
    }

    @Test
    void shouldBeNewIfSubtaskListIsEmpty() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.updateStatus(List.of());
        assertEquals(TaskStatus.NEW, e1.getStatus());
    }

    @Test
    void shouldBeNewIfAllSubtasksAreNew() {
        Epic e1 = new Epic(1, "e1", "desc1");

        SubTask s1 = new SubTask(101, "s1", "d1", 1);
        SubTask s2 = new SubTask(102, "s2", "d2", 1);

        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.NEW);

        e1.updateStatus(List.of(s1, s2));
        assertEquals(TaskStatus.NEW, e1.getStatus());
    }

    @Test
    void shouldBeDoneIfAllSubtasksAreDone() {
        Epic e1 = new Epic(1, "e1", "desc1");

        SubTask s1 = new SubTask(101, "s1", "d1", 1);
        SubTask s2 = new SubTask(102, "s2", "d2", 1);

        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);

        e1.updateStatus(List.of(s1, s2));
        assertEquals(TaskStatus.DONE, e1.getStatus());
    }

    @Test
    void shouldBeInProgressIfSubtasksMixed() {
        Epic e1 = new Epic(1, "e1", "desc1");

        SubTask s1 = new SubTask(101, "s1", "d1", 1);
        SubTask s2 = new SubTask(102, "s2", "d2", 1);

        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);

        e1.updateStatus(List.of(s1, s2));
        assertEquals(TaskStatus.IN_PROGRESS, e1.getStatus());
    }

    @Test
    void shouldAddSubTaskId() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.addSubTaskId(101);
        assertTrue(e1.getSubTaskIds().contains(101));
    }

    @Test
    void shouldRemoveSubTaskId() {
        Epic e1 = new Epic(1, "e1", "desc1");
        e1.addSubTaskId(101);
        e1.removeSubTaskId(101);
        assertFalse(e1.getSubTaskIds().contains(101L));
    }

    @Test
    void shouldReturnEmptyList() {
        Epic e1 = new Epic(1, "e1", "desc1");
        assertTrue(e1.getSubTaskIds().isEmpty());
    }
}
