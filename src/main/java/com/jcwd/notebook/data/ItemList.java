package com.jcwd.notebook.data;

import java.util.HashMap;
import java.util.Map;

public class ItemList {
    private String name;
    private Map<String, Integer> items;

    public ItemList(String name) {
        this.name = name;
        this.items = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void addItem(String itemIdentifier, int quantity) {
        items.put(itemIdentifier, items.getOrDefault(itemIdentifier, 0) + quantity);
    }

    public boolean removeItem(String itemIdentifier, int quantity) {
        if (!items.containsKey(itemIdentifier)) return false;
        
        int current = items.get(itemIdentifier);
        if (current <= quantity) {
            items.remove(itemIdentifier);
        } else {
            items.put(itemIdentifier, current - quantity);
        }
        return true;
    }
}