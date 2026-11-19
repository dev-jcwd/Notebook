package com.jcwd.notebook.data;

import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ListManager {
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("notebook_lists.json");

    // Stores all lists with the list name as the key
    private static Map<String, ItemList> lists = new LinkedHashMap<>();

    public static void loadLists() {
        Map<String, ItemList> loaded = Store.load(FILE_PATH, new TypeToken<LinkedHashMap<String, ItemList>>(){}.getType());
        lists = loaded != null ? loaded : new LinkedHashMap<>();
    }

    public static void saveLists() {
        Store.save(FILE_PATH, lists);
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
