package ru.kanban.task;

public final class TaskConfig {

    public static int maxTasks = 10_000;
    public static int maxEpicTasks = 10_000;
    public static int maxSubTasks = 10_000;
    public static int maxSubTasksPerEpic = 100;
    public static int maxTitleLength = 100;
    public static int maxDescriptionLength = 500;
    public static boolean allowDuplicateIds = false;

    private TaskConfig() {}

    public static void setMaxTitleLength(int max) {
        TaskConfig.maxTitleLength = max;
    }

    public static void setMaxDescriptionLength(int max) {
        TaskConfig.maxDescriptionLength = max;
    }

    public static void setMaxTasks(int max) {
        TaskConfig.maxTasks = max;
    }

    public static void setAllowDuplicateIds(boolean allow) {
        TaskConfig.allowDuplicateIds = allow;
    }

    public static void setMaxSubTasksPerEpic(int maxSubTasksPerEpic) {
        TaskConfig.maxSubTasksPerEpic = maxSubTasksPerEpic;
    }

    public static void setMaxSubTasks(int maxSubTasks) {
        TaskConfig.maxSubTasks = maxSubTasks;
    }

    public static void setMaxEpicTasks(int maxEpicTasks) {
        TaskConfig.maxEpicTasks = maxEpicTasks;
    }
}


