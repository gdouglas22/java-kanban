package ru.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kanban.task.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private Path tempPath;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setup() throws IOException {
        tempPath = Files.createTempFile("kanban-test", ".csv");
        manager = new FileBackedTaskManager(tempPath);
    }

    @Test
    void shouldSaveAndLoad_TaskCorrectly() {
        Task t1 = manager.createTask("t1", "desc1");
        manager.getTask(t1.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempPath);
        Task restored = loaded.getTask(t1.getId());

        assertEquals(t1, restored);
    }

    @Test
    void shouldAddTask_AndPersistToFile() throws IOException {
        Task t1 = manager.createTask("t1", "desc1");

        assertNotNull(t1);
        assertEquals("t1", manager.getTask(t1.getId()).getTitle());

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("t1") && line.contains("desc1")));
    }

    @Test
    void shouldAddEpic_AndPersistToFile() throws IOException {
        Epic e1 = manager.createEpic("e1", "desc1");

        assertNotNull(e1);
        assertTrue(manager.getEpic(e1.getId()).getSubTaskIds().isEmpty());

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("e1") && line.contains("desc1")
                && line.contains("EPIC")));
    }

    @Test
    void shouldAddSubtaskAndLinkToEpic() throws IOException {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());

        assertTrue(manager.getEpic(e1.getId()).getSubTaskIds().contains(s1.getId()));

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("s1") && line.contains("SUBTASK")
                && line.contains(String.valueOf(e1.getId()))));
    }

    @Test
    void shouldUpdateTaskAndPersistChanges() throws IOException {
        Task t1 = manager.createTask("t1", "desc1");
        t1.setTitle("updated");
        manager.updateTask(t1);

        assertEquals("updated", manager.getTask(t1.getId()).getTitle());

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("updated")));
    }

    @Test
    void shouldUpdateSubtaskAndPersistChanges() throws IOException {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());
        s1.setDescription("updated desc");
        manager.updateSubTask(s1);

        assertEquals("updated desc", manager.getSubTask(s1.getId()).getDescription());

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("updated desc")));
    }

    @Test
    void shouldRemoveTask() throws IOException {
        Task t1 = manager.createTask("t1", "desc1");
        manager.removeTask(t1.getId());

        assertNull(manager.getTask(t1.getId()));

        List<String> lines = Files.readAllLines(tempPath);
        assertFalse(lines.stream().anyMatch(line -> line.contains("t1")));
    }


    @Test
    void shouldRemoveEpicAndItsSubtasks() throws IOException {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());

        manager.removeEpic(e1.getId());

        assertNull(manager.getEpic(e1.getId()));
        assertNull(manager.getSubTask(s1.getId()));

        List<String> lines = Files.readAllLines(tempPath);
        assertFalse(lines.stream().anyMatch(line -> line.contains("e1")));
        assertFalse(lines.stream().anyMatch(line -> line.contains("s1")));
    }

    @Test
    void shouldRemoveSubtaskAndUnlinkFromEpic() throws IOException {
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());

        manager.removeSubTask(s1.getId());

        assertFalse(manager.getEpic(e1.getId()).getSubTaskIds().contains(s1.getId()));

        List<String> lines = Files.readAllLines(tempPath);
        assertFalse(lines.stream().anyMatch(line -> line.contains("s1")));
    }

    @Test
    void shouldClearAllTasksAndEpics() throws IOException {
        Task t1 = manager.createTask("t1", "desc1");
        Epic e1 = manager.createEpic("e1", "desc1");
        SubTask s1 = manager.createSubTask("s1", "desc1", e1.getId());

        manager.clearAll();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());

        List<String> lines = Files.readAllLines(tempPath);
        assertEquals(1, lines.size());
        assertEquals("id,type,title,status,description,epic", lines.getFirst());
    }

    @Test
    void shouldManuallyAddTaskAndPersistToFile() throws IOException {
        Task t1 = new Task(100, "t1", "desc1");
        manager.addTask(t1);

        Task t = manager.getTask(100);
        assertEquals(t1, t);

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("t1") && line.contains("TASK")));
    }

    @Test
    void shouldManuallyAddEpicAndPersistToFile() throws IOException {
        Epic e1 = new Epic(200, "e1", "desc1");
        manager.addEpic(e1);

        Epic e = manager.getEpic(200);
        assertEquals(e1, e);
        assertEquals(TaskStatus.NEW, e.getStatus());

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("e1") && line.contains("EPIC")));
    }

    @Test
    void shouldManuallyAddSubtask_LinkedToEpicAndPersistToFile() throws IOException {
        Epic e1 = new Epic(300, "e1", "desc1");
        manager.addEpic(e1);

        SubTask s1 = new SubTask(301, "s1", "desc1", 300);
        manager.addSubTask(s1);

        SubTask s = manager.getSubTask(301);
        assertEquals(s1, s);

        List<SubTask> list = manager.getSubTasksOfEpic(300);
        assertTrue(list.contains(s1));

        List<String> lines = Files.readAllLines(tempPath);
        assertTrue(lines.stream().anyMatch(line -> line.contains("s1") && line.contains("SUBTASK")
                && line.contains("300")));
    }

    @Test
    void shouldThrowWhenSavingToInvalidPath() throws IOException {
        Path tempDir = Files.createTempDirectory("readonly");
        Path brokenFile = tempDir.resolve("broken.csv");

        Files.delete(tempDir);

        FileBackedTaskManager brokenManager = new FileBackedTaskManager(brokenFile);
        Task task = new Task(1, "t1", "desc1");

        ManagerSaveException ex = assertThrows(ManagerSaveException.class, () ->
                brokenManager.addTask(task));
        assertTrue(ex.getMessage().contains("Ошибка при сохранении"));
    }

    @Test
    void shouldThrowWhenLoadingFromNonExistentFile() {
        Path fakePath = Path.of("lorem/lorem/lorem/file.csv");

        ManagerSaveException ex = assertThrows(ManagerSaveException.class, () ->
                FileBackedTaskManager.loadFromFile(fakePath));
        assertTrue(ex.getMessage().contains("Ошибка при загрузке файла"));
    }

    @Test
    void shouldThrowWhenFileContainsInvalidTaskLine() throws IOException {
        Path brokenFile = Files.createTempFile("broken", ".csv");
        Files.writeString(brokenFile, """
                id,type,title,status,description,epic
                lorem ipsum maxima
                """);

        ManagerSaveException ex = assertThrows(ManagerSaveException.class, () ->
                FileBackedTaskManager.loadFromFile(brokenFile));
        assertTrue(ex.getMessage().contains("Ошибка при разборе строки"));
    }

}
