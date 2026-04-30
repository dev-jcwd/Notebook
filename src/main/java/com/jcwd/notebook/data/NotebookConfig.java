package com.jcwd.notebook.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NotebookConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
        if (Files.exists(FILE_PATH)) {
            try (Reader reader = Files.newBufferedReader(FILE_PATH)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    itemListX = data.itemListX;
                    itemListY = data.itemListY;
                    todoListX = data.todoListX;
                    todoListY = data.todoListY;
                    activeItemList = data.activeItemList;
                    activeTodoList = data.activeTodoList;
                }
            } catch (IOException e) {
                System.err.println("Failed to load Notebook Config!");
            }
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(FILE_PATH)) {
            ConfigData data = new ConfigData();
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save Notebook Config!");
        }
    }
}