package ru.kanban;

import ru.kanban.task.*;

public class Main {
    public static void main(String[] args) {

        // Для ревьюера
        // *Первый проект(пустой) отправил случайно
        // По итогу сделал через юнит-тесты проверку работы

        // Я не уверен по поводу своей архитектуры - кажется, что не до конца верно понял ТЗ

//        TaskManager m = new TaskManager();
//        Task t1 = new Task("t1", "desc");
//        Task t2 = new Task("t2", "desc");
//        m.addTask(t1);
//        m.addTask(t2);
//
//        try {
//            Task foundT1 = m.getTaskById(t1.getId());
//            System.out.println("FOUND: " + foundT1);
//        } catch (Exception e) {
//            System.out.println("ERROR (get task): " + e.getMessage());
//        }
//
//        t1.setStatus(TaskStatus.DONE);
//        try {
//            m.updateTask(t1);
//        } catch (Exception e) {
//            System.out.println("ERROR (update task): " + e.getMessage());
//        }
//
//        System.out.println("\nTASKS:");
//        for (Task t : m.getAllTasks()) {
//            System.out.println(t);
//        }
//
//        Epic e1 = new Epic("e1", "desc");
//        Epic e2 = new Epic("e2", "desc");
//        m.addEpic(e1);
//        m.addEpic(e2);
//
//        SubTask s1 = new SubTask("s1", "desc", e1.getId());
//        SubTask s2 = new SubTask("s2", "desc", e1.getId());
//        SubTask s3 = new SubTask("s3", "desc", e2.getId());
//
//        m.addSubtask(s1);
//        m.addSubtask(s2);
//        m.addSubtask(s3);
//
//        try {
//            Epic foundE1 = m.getEpicById(e1.getId());
//            System.out.println("FOUND: " + foundE1);
//        } catch (Exception e) {
//            System.out.println("ERROR (get epic): " + e.getMessage());
//        }
//
//        try {
//            SubTask foundS3 = m.getSubtaskById(s3.getId());
//            System.out.println("FOUND: " + foundS3);
//        } catch (Exception e) {
//            System.out.println("ERROR (get subtask): " + e.getMessage());
//        }
//
//        s1.setStatus(TaskStatus.IN_PROGRESS);
//        try {
//            m.updateSubtask(s1);
//        } catch (Exception e) {
//            System.out.println("ERROR (update subtask): " + e.getMessage());
//        }
//
//        System.out.println("\nEPICS:");
//        for (Epic e : m.getAllEpics()) {
//            System.out.println(e);
//        }
//
//        System.out.println("\nSUBTASKS:");
//        for (SubTask s : m.getAllSubtasks()) {
//            System.out.println(s);
//        }
//
//        System.out.println("\nSUBTASKS OF e1:");
//        try {
//            for (SubTask s : m.getSubtasksOfEpic(e1.getId())) {
//                System.out.println(s);
//            }
//        } catch (Exception e) {
//            System.out.println("ERROR (get subtasks of epic): " + e.getMessage());
//        }
//
//        try {
//            m.removeTaskById(t2.getId());
//            System.out.println("Removed t2");
//        } catch (Exception e) {
//            System.out.println("ERROR (remove task): " + e.getMessage());
//        }
//
//        try {
//            m.removeSubtaskById(s2.getId());
//            System.out.println("Removed s2");
//        } catch (Exception e) {
//            System.out.println("ERROR (remove subtask): " + e.getMessage());
//        }
//
//        try {
//            m.removeEpicById(e2.getId());
//            System.out.println("Removed e2");
//        } catch (Exception e) {
//            System.out.println("ERROR (remove epic): " + e.getMessage());
//        }
//
//        m.clearTasks();
//        m.clearEpics();
//        m.clearSubtasks();
//
//        System.out.println("\nCLEARED:");
//        System.out.println("Tasks: " + m.getAllTasks().size());
//        System.out.println("Epics: " + m.getAllEpics().size());
//        System.out.println("Subtasks: " + m.getAllSubtasks().size());
//
//        try {
//            m.getTaskById(999L);
//        } catch (Exception e) {
//            System.out.println("EXPECTED ERROR (task): " + e.getMessage());
//        }
//
//        try {
//            m.removeEpicById(888L);
//        } catch (Exception e) {
//            System.out.println("EXPECTED ERROR (epic): " + e.getMessage());
//        }
//
//        try {
//            m.removeSubtaskById(777L);
//        } catch (Exception e) {
//            System.out.println("EXPECTED ERROR (subtask): " + e.getMessage());
//        }
//    }
    }
}
