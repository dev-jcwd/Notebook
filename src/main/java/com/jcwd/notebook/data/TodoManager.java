package com.jcwd.notebook.data;

import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class TodoManager {
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("notebook_todos.json");

    // Stores all lists with the list name as the key
    private static Map<String, TodoList> lists = new LinkedHashMap<>();

    public static void loadLists() {
        Map<String, TodoList> loaded = Store.load(FILE_PATH, new TypeToken<LinkedHashMap<String, TodoList>>(){}.getType());
        lists = loaded != null ? loaded : new LinkedHashMap<>();
    }

    public static void saveLists() {
        Store.save(FILE_PATH, lists);
    }

    public static boolean createList(String name) {
        if (lists.containsKey(name)) return false;
        lists.put(name, new TodoList(name));
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

    public static TodoList getList(String name) {
        return lists.get(name);
    }

    public static Map<String, TodoList> getAllLists() {
        return lists;
    }
}
