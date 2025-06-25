package ru.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.task.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task t1 = manager.createTask("t1", "desc1");
        Task t = manager.getTask(t1.getId());
        assertEquals(t1, t);
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic e1 = manager.createEpic("e1", "desc1");
        Epic e = manager.getEpic(e1.getId());
        assertEquals(e1, e);
    }

    @Test
    void shouldCreateAndGetSubTask() {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());
        SubTask s = manager.getSubTask(s1.getId());
        assertEquals(s1, s);
    }

    @Test
    void shouldGetAllTasks() {
        manager.createTask("t1", "desc1");
        manager.createTask("t2", "desc2");
        assertEquals(2, manager.getAllTasks().size());
    }

    @Test
    void shouldUpdateTask() {
        Task t1 = manager.createTask("t1", "desc1");
        t1.setTitle("updated");
        manager.updateTask(t1);
        assertEquals("updated", manager.getTask(t1.getId()).getTitle());
    }

    @Test
    void shouldRemoveTask() {
        Task t1 = manager.createTask("t1", "desc1");
        manager.removeTask(t1.getId());
        assertNull(manager.getTask(t1.getId()));
    }

    @Test
    void shouldClearAll() {
        manager.createTask("t1", "desc1");
        manager.createEpic("e1", "desc1");
        manager.createSubTask("s1", "desc1", manager.createEpic("e2", "desc2").getId());

        manager.clearAll();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    @Test
    void shouldReturnSubTasksOfEpic() {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());
        SubTask s2 = manager.createSubTask("s2", "desc2", e1.getId());

        List<SubTask> list = manager.getSubTasksOfEpic(e1.getId());

        assertEquals(2, list.size());
        assertTrue(list.contains(s1));
        assertTrue(list.contains(s2));
    }

    @Test
    void shouldUpdateEpicStatus() {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());
        SubTask s2 = manager.createSubTask("s2", "desc2", e1.getId());

        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.NEW);

        manager.updateSubTask(s1);
        manager.updateSubTask(s2);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(e1.getId()).getStatus());
    }

    @Test
    void shouldTrackHistoryOnGet() {
        Task t1 = manager.createTask("t1", "desc1");
        manager.getTask(t1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t1, history.getFirst());
    }

    @Test
    void shouldNotCreateWithNullTitleOrDescription() {
        assertNull(manager.createTask(null, "desc"));
        assertNull(manager.createEpic("e1", null));
        assertNull(manager.createSubTask("s1", null, 0));
    }

    @Test
    void shouldAddTaskManually() {
        Task t1 = new Task(100, "t1", "desc1");
        manager.addTask(t1);

        Task t = manager.getTask(100);
        assertEquals(t1, t);
    }

    @Test
    void shouldAddEpicManually() {
        Epic e1 = new Epic(200, "e1", "desc1");
        manager.addEpic(e1);

        Epic e = manager.getEpic(200);
        assertEquals(e1, e);
        assertEquals(TaskStatus.NEW, e.getStatus());
    }

    @Test
    void shouldAddSubTaskManually() {
        Epic e1 = new Epic(300, "e1", "desc1");
        manager.addEpic(e1);

        SubTask s1 = new SubTask(301, "s1", "desc1", 300);
        manager.addSubTask(s1);

        SubTask s = manager.getSubTask(301);
        assertEquals(s1, s);

        List<SubTask> list = manager.getSubTasksOfEpic(300);
        assertTrue(list.contains(s1));
    }

    @Test
    void shouldThrowWhenAddingSubTaskWithUnknownEpic() {
        SubTask s1 = new SubTask(400, "s1", "desc1", 999);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manager.addSubTask(s1));

        assertEquals("Эпик с id 999 не существует", exception.getMessage());
    }

    @Test
    void shouldRemoveEpicAndItsSubtasks() {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());
        SubTask s2 = manager.createSubTask("s2", "desc2", e1.getId());

        manager.removeEpic(e1.getId());

        assertNull(manager.getEpic(e1.getId()));
        assertNull(manager.getSubTask(s1.getId()));
        assertNull(manager.getSubTask(s2.getId()));
    }

    @Test
    void shouldRemoveSubTaskAndUpdateEpic() {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());

        manager.removeSubTask(s1.getId());

        assertNull(manager.getSubTask(s1.getId()));

        Epic e = manager.getEpic(e1.getId());
        assertFalse(e.getSubTaskIds().contains(s1.getId()));
    }
}
