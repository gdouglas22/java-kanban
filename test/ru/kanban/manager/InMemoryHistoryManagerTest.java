package ru.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager history;

    @BeforeEach
    void setUp() {
        history = new InMemoryHistoryManager();
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    void shouldAddTaskToHistory() {
        Task t1 = new Task(1, "t1", "desc1");
        history.add(t1);
        List<Task> result = history.getHistory();

        assertEquals(1, result.size());
        assertEquals(t1, result.getFirst());
    }

    @Test
    void shouldReturnTasksInOrder() {
        Task t1 = new Task(1, "t1", "desc1");
        Task t2 = new Task(2, "t2", "desc2");
        Task t3 = new Task(3, "t3", "desc3");

        history.add(t1);
        history.add(t2);
        history.add(t3);

        List<Task> result = history.getHistory();

        assertEquals(List.of(t1, t2, t3), result);
    }

    @Test
    void shouldLimitHistorySizeTo10() {
        for (int i = 1; i <= 12; i++) {
            history.add(new Task(i, "t" + i, "desc" + i));
        }

        List<Task> result = history.getHistory();

        assertEquals(10, result.size());
        assertEquals("t3", result.get(0).getTitle());
        assertEquals("t12", result.get(9).getTitle());
    }
}
