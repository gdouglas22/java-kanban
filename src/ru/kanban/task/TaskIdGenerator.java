package ru.kanban.task;

/*
TaskIdGenerator - класс для защиты глобального ID задач от прямого изменения
через приватный конструктор делаю невозможным создание экземпляра класса, а package-private
 е даст доступ к IdCounter вне пакета
*/

class TaskIdGenerator {

    private static long idCounter = 1;

    private TaskIdGenerator() {}

    static long nextId() {
        return idCounter++;
    }

    static void reset() {
        idCounter = 1;
    }
}
