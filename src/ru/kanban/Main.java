package ru.kanban;

import ru.kanban.manager.Managers;
import ru.kanban.manager.TaskManager;
import ru.kanban.task.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task t1 = manager.createTask("t1", "desc1");
        Task t2 = manager.createTask("t2", "desc2");
        Epic e1 = manager.createEpic("e1", "desc3");
        SubTask s1 = manager.createSubTask("s1", "desc4", e1.getId());
        SubTask s2 = manager.createSubTask("s2", "desc5", e1.getId());
        Epic e2 = manager.createEpic("e2", "desc6");
        SubTask s3 = manager.createSubTask("s3", "desc7", e2.getId());

        System.out.println("задачи");
        List<Task> tasks = manager.getAllTasks();
        for (Task value : tasks) {
            System.out.println(value);
        }

        System.out.println("эпики");
        List<Epic> epics = manager.getAllEpics();
        for (Epic epic : epics) {
            System.out.println(epic);
        }

        System.out.println("подзадачи");
        List<SubTask> subtasks = manager.getAllSubTasks();
        for (SubTask subtask : subtasks) {
            System.out.println(subtask);
        }

        t1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(t1);

        s1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(s1);

        s2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(s2);

        s3.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(s3);

        System.out.println("статусы");
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(e1);
        System.out.println(e2);

        manager.removeTask(t2.getId());
        manager.removeEpic(e2.getId());

        System.out.println("удаляем task2 epic2");

        System.out.println("задачи");
        tasks = manager.getAllTasks();
        for (Task task : tasks) {
            System.out.println(task);
        }

        System.out.println("эпики");
        epics = manager.getAllEpics();
        for (Epic epic : epics) {
            System.out.println(epic);
        }

        System.out.println("подзадачи");
        subtasks = manager.getAllSubTasks();
        for (SubTask subtask : subtasks) {
            System.out.println(subtask);
        }
    }
}

