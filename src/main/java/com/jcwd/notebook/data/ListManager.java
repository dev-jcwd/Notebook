package com.jcwd.notebook.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ListManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("notebook_lists.json");
    
    // Stores all lists with the list name as the key
    private static Map<String, ItemList> lists = new HashMap<>();

    public static void loadLists() {
        if (Files.exists(FILE_PATH)) {
            try (Reader reader = Files.newBufferedReader(FILE_PATH)) {
                Type type = new TypeToken<Map<String, ItemList>>(){}.getType();
                lists = GSON.fromJson(reader, type);
                if (lists == null) lists = new HashMap<>();
            } catch (IOException e) {
                System.err.println("Failed to load Notebook lists!");
                e.printStackTrace();
            }
        }
    }

    public static void saveLists() {
        try (Writer writer = Files.newBufferedWriter(FILE_PATH)) {
            GSON.toJson(lists, writer);
        } catch (IOException e) {
            System.err.println("Failed to save Notebook lists!");
            e.printStackTrace();
        }
    }

    public static boolean createList(String name) {
        if (lists.containsKey(name)) return false;
        lists.put(name, new ItemList(name));
        saveLists();
        return true;
    }

    public static boolean deleteList(String name) {
        if (lists.remove(name) != null) {
            saveLists();
            return true;
        }
        return false;
    }

    public static ItemList getList(String name) {
        return lists.get(name);
    }

    public static Map<String, ItemList> getAllLists() {
        return lists;
    }
}