package ru.kanban.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.kanban.task.Epic;
import ru.kanban.task.SubTask;
import ru.kanban.task.Task;
import ru.kanban.task.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    Path tempPath;

    @Override
    protected FileBackedTaskManager createManager() throws IOException {
        tempPath = tempDir.resolve("kanban-test.csv");
        Files.createFile(tempPath);
        return new FileBackedTaskManager(tempPath);
    }

    @Test
    void saveAndLoad_preservesTasksAndPriorityAndEpicTime() {
        Task a = manager.createTask("A", "d");
        a.setStartTime(LocalDateTime.of(2025,1,1,10,0));
        a.setDuration(Duration.ofMinutes(30));
        manager.updateTask(a);

        Task b = manager.createTask("B", "d");
        b.setStartTime(LocalDateTime.of(2025,1,1,11,0));
        b.setDuration(Duration.ofMinutes(30));
        manager.updateTask(b);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);

        List<Task> p = loaded.getPrioritizedTasks();

        assertEquals(2, p.size());
        assertEquals(a.getId(), p.get(0).getId());
        assertEquals(b.getId(), p.get(1).getId());
    }

    @Test
    void saveAndLoad_epicWithSubtasks_restoresRelations_time_and_status() {
        Epic e = manager.createEpic("E", "d");

        SubTask s1 = manager.createSubTask("S1", "d", e.getId());
        s1.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        s1.setDuration(Duration.ofMinutes(30));
        s1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(s1);

        SubTask s2 = manager.createSubTask("S2", "d", e.getId());
        s2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        s2.setDuration(Duration.ofMinutes(45));
        s2.setStatus(TaskStatus.NEW);
        manager.updateSubTask(s2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);

        Epic eLoaded = loaded.getEpic(e.getId());
        assertNotNull(eLoaded, "Эпик должен загрузиться");
        List<SubTask> subsLoaded = loaded.getSubTasksOfEpic(eLoaded.getId());
        assertEquals(2, subsLoaded.size(), "Должны загрузиться обе подзадачи эпика");
        assertTrue(subsLoaded.stream().anyMatch(st -> st.getId() == s1.getId()));
        assertTrue(subsLoaded.stream().anyMatch(st -> st.getId() == s2.getId()));

        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), eLoaded.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 11, 45), eLoaded.getEndTime());
        assertEquals(Duration.ofMinutes(30 + 45), eLoaded.getDuration());

        assertEquals(TaskStatus.IN_PROGRESS, eLoaded.getStatus());

        List<Task> prioritized = loaded.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(s1.getId(), prioritized.get(0).getId()); // 09:00 первым
        assertEquals(s2.getId(), prioritized.get(1).getId()); // 11:00 вторым
    }

    @Test
    void saveAndLoad_subtaskWithNullTime_notInPrioritized_andEpicTimeAggregatesOnlyNonNull() {
        Epic e = manager.createEpic("E", "d");

        SubTask sNull = manager.createSubTask("S-null", "d", e.getId());
        manager.updateSubTask(sNull);

        SubTask sPlan = manager.createSubTask("S-plan", "d", e.getId());
        sPlan.setStartTime(LocalDateTime.of(2025, 1, 2, 10, 0));
        sPlan.setDuration(Duration.ofMinutes(30));
        manager.updateSubTask(sPlan);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);

        List<Task> p = loaded.getPrioritizedTasks();
        assertEquals(1, p.size());
        assertEquals(sPlan.getId(), p.get(0).getId());

        Epic eLoaded = loaded.getEpic(e.getId());
        assertEquals(LocalDateTime.of(2025, 1, 2, 10, 0), eLoaded.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 2, 10, 30), eLoaded.getEndTime());
        assertEquals(Duration.ofMinutes(30), eLoaded.getDuration());
    }

    @Test
    void saveAndLoad_epicWithoutSubtasks_hasZeroDurationAndNullTimes() {
        Epic e = manager.createEpic("E", "d");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);

        Epic eLoaded = loaded.getEpic(e.getId());
        assertNotNull(eLoaded);
        assertEquals(Duration.ZERO, eLoaded.getDuration());
        assertNull(eLoaded.getStartTime());
        assertNull(eLoaded.getEndTime());
        assertTrue(loaded.getPrioritizedTasks().isEmpty());
    }

    @Test
    void saveAndLoad_preservesSubtaskStatus() {
        Epic e = manager.createEpic("E", "d");

        SubTask s = manager.createSubTask("S", "d", e.getId());
        s.setStatus(TaskStatus.IN_PROGRESS);
        s.setStartTime(LocalDateTime.of(2025, 1, 3, 12, 0));
        s.setDuration(Duration.ofMinutes(25));
        manager.updateSubTask(s);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);

        SubTask sLoaded = loaded.getSubTask(s.getId());
        assertNotNull(sLoaded);
        assertEquals(TaskStatus.IN_PROGRESS, sLoaded.getStatus(), "Статус сабтаска должен сохраниться");
        assertEquals(TaskStatus.IN_PROGRESS, loaded.getEpic(e.getId()).getStatus());
    }

    @Test
    void loadFromFile_throwsManagerSaveException_ifFileMissingOrUnreadable(@TempDir Path dir) {
        Path badFile = dir.resolve("nonexistent.csv");
        assertFalse(Files.exists(badFile));

        assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(badFile),
                "Файла не существует");
    }
}

