package com.jcwd.notebook.data;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class NotebookConfig {
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("notebook_config.json");

    // Toggle States
    public static boolean showItemList = false;
    public static boolean showTodoList = false;

    // Active Lists to Display
    public static String activeItemList = "";
    public static String activeTodoList = "";

    // HUD Positions
    public static int itemListX = 10;
    public static int itemListY = 10;
    public static int todoListX = 150;
    public static int todoListY = 10;
    public static int itemPage = 0;
    public static int todoPage = 0;

    // Create an inner class to structure the JSON
    private static class ConfigData {
        int itemListX = NotebookConfig.itemListX;
        int itemListY = NotebookConfig.itemListY;
        int todoListX = NotebookConfig.todoListX;
        int todoListY = NotebookConfig.todoListY;
        String activeItemList = NotebookConfig.activeItemList;
        String activeTodoList = NotebookConfig.activeTodoList;
    }

    public static void load() {
        ConfigData data = Store.load(FILE_PATH, ConfigData.class);
        if (data == null) return;
        itemListX = data.itemListX;
        itemListY = data.itemListY;
        todoListX = data.todoListX;
        todoListY = data.todoListY;
        activeItemList = data.activeItemList != null ? data.activeItemList : "";
        activeTodoList = data.activeTodoList != null ? data.activeTodoList : "";
    }

    public static void save() {
        Store.save(FILE_PATH, new ConfigData());
    }
}
