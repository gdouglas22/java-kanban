package ru.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.kanban.task.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager() throws IOException;

    @BeforeEach
    void setUp() throws IOException{
        manager = createManager();
    }

    protected static void plan(Task t, int yyyy, int mm, int dd, int hh, int min, long minutes) {
        t.setStartTime(LocalDateTime.of(yyyy, mm, dd, hh, min));
        t.setDuration(Duration.ofMinutes(minutes));
    }

    protected static void assertEpicStatus(TaskManager m, Epic e, TaskStatus expected) {
        Epic stored = m.getEpic(e.getId());
        assertNotNull(stored);
        assertEquals(expected, stored.getStatus());
    }

    @Test
    void createAndGetTask_storesAndAppearsInHistory() {
        Task t = manager.createTask("A", "desc");
        assertNotNull(t);
        Task got = manager.getTask(t.getId());
        assertEquals(t.getId(), got.getId());

        List<Task> hist = manager.getHistory();
        assertEquals(1, hist.size());
        assertEquals(t.getId(), hist.get(0).getId());
    }

    @Test
    void subtaskHasLinkedEpic_andGetSubTasksOfEpic() {
        Epic e = manager.createEpic("E", "desc");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        assertNotNull(manager.getEpic(e.getId()));
        assertEquals(e.getId(), manager.getSubTask(s1.getId()).getEpicId());

        List<SubTask> subs = manager.getSubTasksOfEpic(e.getId());
        assertEquals(2, subs.size());
        assertTrue(subs.stream().anyMatch(s -> s.getId() == s1.getId()));
        assertTrue(subs.stream().anyMatch(s -> s.getId() == s2.getId()));
    }

    @Test
    void epicStatus_allNew() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.NEW);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        assertEpicStatus(manager, e, TaskStatus.NEW);
    }

    @Test
    void epicStatus_allDone() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        assertEpicStatus(manager, e, TaskStatus.DONE);
    }

    @Test
    void epicStatus_mixedNewDone() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        assertEpicStatus(manager, e, TaskStatus.IN_PROGRESS);
    }

    @Test
    void epicStatus_withInProgress() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.NEW);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        assertEpicStatus(manager, e, TaskStatus.IN_PROGRESS);
    }

    @Test
    void prioritizedTasks_ordersByStart_andSkipsNullStart() {
        Task a = manager.createTask("A", "d");
        Task b = manager.createTask("B", "d");
        Task c = manager.createTask("C", "d");

        plan(b, 2025, 1, 1, 10, 0, 30);
        plan(c, 2025, 1, 1, 11, 0, 30);

        manager.updateTask(b);
        manager.updateTask(c);
        manager.updateTask(a);

        List<Task> p = manager.getPrioritizedTasks();
        assertEquals(List.of(b, c), p);
    }

    @Test
    void addingOverlappingTasks_throwsIllegalArgument() {
        Task a = manager.createTask("A", "d");
        plan(a, 2025, 1, 1, 10, 0, 30);
        manager.updateTask(a);

        Task b = manager.createTask("B", "d");
        plan(b, 2025, 1, 1, 10, 15, 30);

        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(b));
    }

    @Test
    void createTask_setsId_stores_and_notInPriorityWithoutTime() {
        Task t = manager.createTask("T", "desc");
        assertNotNull(t);
        assertNotNull(manager.getTask(t.getId()));
        assertFalse(manager.getPrioritizedTasks().contains(t));
    }

    @Test
    void createEpic_initialCalculatedFields_andNotInPriority() {
        Epic e = manager.createEpic("E", "desc");
        assertNotNull(e);
        Epic stored = manager.getEpic(e.getId());
        assertNotNull(stored);
        assertEquals(TaskStatus.NEW, stored.getStatus());
        assertEquals(Duration.ZERO, stored.getDuration());
        assertNull(stored.getStartTime());
        assertNull(stored.getEndTime());
        assertFalse(manager.getPrioritizedTasks().contains(stored));
    }

    @Test
    void createSubTask_linksToEpic_andRecalculatesEpicTimeWhenPlanned() {
        Epic e = manager.createEpic("E", "d");
        SubTask s = manager.createSubTask("S", "d", e.getId());
        assertEquals(e.getId(), s.getEpicId());
        assertTrue(manager.getSubTasksOfEpic(e.getId()).stream().anyMatch(x -> x.getId() == s.getId()));

        plan(s, 2025, 1, 1, 10, 0, 30);
        manager.updateSubTask(s);
        Epic after = manager.getEpic(e.getId());
        assertEquals(Duration.ofMinutes(30), after.getDuration());
        assertEquals(s.getStartTime(), after.getStartTime());
        assertEquals(s.getEndTime(), after.getEndTime());
    }

    @Test
    void createSubTask_throwsIfEpicMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubTask("S", "d", 9999));
    }

    @Test
    void addTask_putsIntoStorage_andPriority_andRejectsOverlap() {

        Task a = new Task(9001, "A", "d", TaskStatus.NEW);
        plan(a, 2025, 1, 1, 10, 0, 30);
        assertDoesNotThrow(() -> manager.addTask(a));
        assertEquals(a, manager.getTask(9001));
        assertTrue(manager.getPrioritizedTasks().contains(a));

        Task b = new Task(9002, "B", "d", TaskStatus.NEW);
        plan(b, 2025, 1, 1, 10, 15, 20);
        assertThrows(IllegalArgumentException.class, () -> manager.addTask(b));
        assertNull(manager.getTask(9002));
    }

    @Test
    void addEpic_adds_andInitializesCalculatedFields() {
        Epic e = new Epic(9010, "E", "d");
        manager.addEpic(e);
        Epic stored = manager.getEpic(9010);
        assertNotNull(stored);
        assertEquals(TaskStatus.NEW, stored.getStatus());
        assertEquals(Duration.ZERO, stored.getDuration());
        assertNull(stored.getStartTime());
        assertNull(stored.getEndTime());
    }

    @Test
    void addSubTask_linksEpic_addsToPriority_andRecalculatesEpic() {
        Epic e = new Epic(9020, "E", "d");
        manager.addEpic(e);

        SubTask s = new SubTask(9021, "S", "d", TaskStatus.NEW, e.getId());
        plan(s, 2025, 1, 1, 9, 0, 45);
        manager.addSubTask(s);

        assertEquals(s, manager.getSubTask(9021));
        assertTrue(manager.getPrioritizedTasks().contains(s));

        Epic after = manager.getEpic(e.getId());
        assertTrue(after.getSubTaskIds().contains(9021));
        assertEquals(Duration.ofMinutes(45), after.getDuration());
        assertEquals(s.getStartTime(), after.getStartTime());
        assertEquals(s.getEndTime(), after.getEndTime());
    }

    @Test
    void addSubTask_throwsIfEpicMissing() {
        SubTask s = new SubTask(9030, "S", "d", TaskStatus.NEW, 7777);
        plan(s, 2025, 1, 1, 9, 0, 30);
        assertThrows(IllegalArgumentException.class, () -> manager.addSubTask(s));
    }

    @Test
    void updateTask_reindexesAndRejectsOverlap() {
        Task a = manager.createTask("A", "d");
        Task b = manager.createTask("B", "d");
        plan(a, 2025, 1, 1, 9, 0, 30);
        plan(b, 2025, 1, 1, 10, 0, 30);
        manager.updateTask(a);
        manager.updateTask(b);

        b.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 15));
        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(b));

        b.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        assertDoesNotThrow(() -> manager.updateTask(b));
        List<Task> p = manager.getPrioritizedTasks();
        assertEquals(List.of(a, b), p);
    }

    @Test
    void updateSubTask_recalculatesEpicTimeAndStatus() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());

        plan(s1, 2025, 1, 1, 9, 0, 30);
        plan(s2, 2025, 1, 1, 11, 0, 30);
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.NEW);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        Epic after1 = manager.getEpic(e.getId());
        assertEquals(Duration.ofMinutes(60), after1.getDuration());
        assertEquals(LocalDateTime.of(2025,1,1,9,0), after1.getStartTime());
        assertEquals(LocalDateTime.of(2025,1,1,11,30), after1.getEndTime());
        assertEquals(TaskStatus.IN_PROGRESS, after1.getStatus());

        s2.setStartTime(LocalDateTime.of(2025,1,1,8,0));
        manager.updateSubTask(s2);
        Epic after2 = manager.getEpic(e.getId());
        assertEquals(LocalDateTime.of(2025,1,1,8,0), after2.getStartTime());
    }

    @Test
    void removeTask_removesFromStoragePriorityAndHistory() {
        Task t = manager.createTask("T", "d");
        plan(t, 2025, 1, 1, 8, 0, 30);
        manager.updateTask(t);
        manager.getTask(t.getId());

        manager.removeTask(t.getId());

        assertNull(manager.getTask(t.getId()));
        assertFalse(manager.getPrioritizedTasks().contains(t));
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void removeSubTask_removes_and_recalculatesEpic_and_cleansHistory() {
        Epic e = manager.createEpic("E", "d");
        SubTask s = manager.createSubTask("S", "d", e.getId());
        plan(s, 2025, 1, 1, 9, 0, 30);
        manager.updateSubTask(s);
        manager.getSubTask(s.getId());

        manager.removeSubTask(s.getId());

        assertNull(manager.getSubTask(s.getId()));
        assertFalse(manager.getSubTasksOfEpic(e.getId()).contains(s));
        assertTrue(manager.getHistory().isEmpty());

        Epic after = manager.getEpic(e.getId());
        assertEquals(Duration.ZERO, after.getDuration());
        assertNull(after.getStartTime());
        assertNull(after.getEndTime());
    }

    @Test
    void removeEpic_removesEpic_allSubs_and_history() {
        Epic e = manager.createEpic("E", "d");
        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        SubTask s2 = manager.createSubTask("S2", "d", e.getId());
        plan(s1, 2025, 1, 1, 9, 0, 30);
        plan(s2, 2025, 1, 1, 10, 0, 30);
        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        manager.getEpic(e.getId());
        manager.getSubTask(s1.getId());
        manager.getSubTask(s2.getId());

        manager.removeEpic(e.getId());

        assertNull(manager.getEpic(e.getId()));
        assertNull(manager.getSubTask(s1.getId()));
        assertNull(manager.getSubTask(s2.getId()));
        assertTrue(manager.getHistory().isEmpty());
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void clearAll_clearsEverything_includingPriorityAndHistory() {
        Task t = manager.createTask("T", "d");
        Epic e = manager.createEpic("E", "d");
        SubTask s = manager.createSubTask("S", "d", e.getId());
        plan(t, 2025, 1, 1, 8, 0, 30);
        plan(s, 2025, 1, 1, 9, 0, 30);
        manager.updateTask(t);
        manager.updateSubTask(s);

        manager.getTask(t.getId());
        manager.getEpic(e.getId());
        manager.getSubTask(s.getId());

        manager.clearAll();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
        assertTrue(manager.getPrioritizedTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }
}

