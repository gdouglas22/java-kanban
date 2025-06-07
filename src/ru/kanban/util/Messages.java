package ru.kanban.util;

public final class Messages {
    public static final String ERROR_TITLE_TOO_LONG =
            "Ошибка: длина заголовка превышает допустимый максимум (%d символов)";
    public static final String ERROR_DESCRIPTION_TOO_LONG =
            "Ошибка: длина описания превышает допустимый максимум (%d символов)";
    public static final String ERROR_EPIC_SUBTASK_LIMIT_REACHED =
            "У эпика с ID %d превышено максимальное число сабзадач";
    public static final String ERROR_TASK_NOT_FOUND = "Ошибка: задача с id = %d не найдена";
    public static final String ERROR_EPIC_NOT_FOUND = "Ошибка: эпик с id = %d не найден";
    public static final String ERROR_SUBTASK_NOT_FOUND = "Ошибка: подзадача с id = %d не найдена";
    public static final String ERROR_NULL_TASK = "Ошибка: задача не может быть NULL";
    public static final String ERROR_TASK_LIMIT_REACHED = "Ошибка: Достигнут лимит задач";
    public static final String ERROR_EPIC_LIMIT_REACHED = "Ошибка: Достигнут лимит задач-эпиков";
    public static final String ERROR_SUBTASK_LIMIT_REACHED = "Ошибка: Достигнут общий лимит сабзадач";
    public static final String ERROR_EPIC_STATUS_SET_MANUALLY = "Ошибка: статус эпика запрещено менять вручную";

    private Messages() {}
}

