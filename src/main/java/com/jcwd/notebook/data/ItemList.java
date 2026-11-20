package com.jcwd.notebook.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemList {
    private String name;
    private Map<String, Integer> items;

    public ItemList(String name) {
        this.name = name;
        this.items = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    /** Item ids are always stored with a namespace so "dirt" and "minecraft:dirt" are the same entry. */
    public static String normalize(String item) {
        return item.contains(":") ? item : "minecraft:" + item;
    }

    public void addItem(String itemIdentifier, int quantity) {
        items.merge(itemIdentifier, quantity, Integer::sum);
    }

    public boolean removeItem(String itemIdentifier, int quantity) {
        Integer current = items.get(itemIdentifier);
        if (current == null) return false;

        if (current <= quantity) {
            items.remove(itemIdentifier);
        } else {
            items.put(itemIdentifier, current - quantity);
        }
        return true;
    }
}
