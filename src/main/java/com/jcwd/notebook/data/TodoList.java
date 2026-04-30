package com.jcwd.notebook.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class TodoList {
    private String name;
    private Map<String, Boolean> tasks; 
    private boolean useNumbers;

    public TodoList(String name) {
        this.name = name;
        this.tasks = new LinkedHashMap<>();
        this.useNumbers = false; 
    }

    public String getName() { return name; }
    public Map<String, Boolean> getTasks() { return tasks; }
    public boolean usesNumbers() { return useNumbers; }
    public void setUseNumbers(boolean useNumbers) { this.useNumbers = useNumbers; }

    public void addTask(String task) {
        this.tasks.put(task.trim(), false); 
    }

    public boolean removeTask(String task) {
        return this.tasks.remove(task.trim()) != null;
    }

    public boolean toggleTask(String task) {
        task = task.trim();
        if (this.tasks.containsKey(task)) {
            boolean currentState = this.tasks.get(task);
            this.tasks.put(task, !currentState); 
            return true;
        }
        return false;
    }

    public void clearTasks() {
        this.tasks.clear();
    }
}