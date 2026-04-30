package com.jcwd.notebook.util;

import com.jcwd.notebook.data.NotebookConfig;
import com.jcwd.notebook.gui.EditHudScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {

    public static final KeyBinding.Category NOTEBOOK_CATEGORY = KeyBinding.Category.create(Identifier.of("notebook", "main"));

    public static KeyBinding toggleItemsKey;
    public static KeyBinding toggleTodoKey;
    public static KeyBinding editHudKey;
    public static KeyBinding listNextKey;
    public static KeyBinding listPrevKey;
    public static KeyBinding todoNextKey;
    public static KeyBinding todoPrevKey;

    public static void register() {

        toggleItemsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.toggle_items", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, NOTEBOOK_CATEGORY));
        toggleTodoKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.toggle_todo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, NOTEBOOK_CATEGORY));
        editHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.edit_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, NOTEBOOK_CATEGORY));
        
        listNextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.list_next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, NOTEBOOK_CATEGORY));
        listPrevKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.list_prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, NOTEBOOK_CATEGORY));
        todoNextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.todo_next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, NOTEBOOK_CATEGORY));
        todoPrevKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.notebook.todo_prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, NOTEBOOK_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleItemsKey.wasPressed()) { NotebookConfig.showItemList = !NotebookConfig.showItemList; }
            while (toggleTodoKey.wasPressed()) { NotebookConfig.showTodoList = !NotebookConfig.showTodoList; }
            while (editHudKey.wasPressed()) { if (client.currentScreen == null) { client.setScreen(new EditHudScreen(Text.literal("Edit Notebook HUD"))); } }
            
            while (listNextKey.wasPressed()) { NotebookConfig.itemPage++; }
            while (listPrevKey.wasPressed()) { NotebookConfig.itemPage--; }
            while (todoNextKey.wasPressed()) { NotebookConfig.todoPage++; }
            while (todoPrevKey.wasPressed()) { NotebookConfig.todoPage--; }
        });
    }
}