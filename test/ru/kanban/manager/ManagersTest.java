package ru.kanban.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void shouldReturnTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
        assertInstanceOf(InMemoryTaskManager.class, manager);
    }

    @Test
    void shouldReturnHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history);
        assertInstanceOf(InMemoryHistoryManager.class, history);
    }
}
