package com.jcwd.notebook.util;

import com.jcwd.notebook.data.NotebookConfig;
import com.jcwd.notebook.gui.EditHudScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class KeybindManager {

    public static final KeyMapping.Category NOTEBOOK_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("notebook", "main"));

    public static KeyMapping toggleItemsKey;
    public static KeyMapping toggleTodoKey;
    public static KeyMapping editHudKey;
    public static KeyMapping listNextKey;
    public static KeyMapping listPrevKey;
    public static KeyMapping todoNextKey;
    public static KeyMapping todoPrevKey;

    // Keys are looked up by name at runtime: key codes changed in 26.3, so compiled-in constants would be wrong there.
    private static KeyMapping key(String name, String keyName) {
        int code = keyName == null ? InputConstants.UNKNOWN.getValue() : InputConstants.getKey(keyName).getValue();
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(name, code, NOTEBOOK_CATEGORY));
    }

    public static void register() {
        toggleItemsKey = key("key.notebook.toggle_items", "key.keyboard.i");
        toggleTodoKey = key("key.notebook.toggle_todo", "key.keyboard.o");
        editHudKey = key("key.notebook.edit_hud", "key.keyboard.right.bracket");

        listNextKey = key("key.notebook.list_next", null);
        listPrevKey = key("key.notebook.list_prev", null);
        todoNextKey = key("key.notebook.todo_next", null);
        todoPrevKey = key("key.notebook.todo_prev", null);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleItemsKey.consumeClick()) { NotebookConfig.showItemList = !NotebookConfig.showItemList; }
            while (toggleTodoKey.consumeClick()) { NotebookConfig.showTodoList = !NotebookConfig.showTodoList; }
            while (editHudKey.consumeClick()) { if (Compat.currentScreen(client) == null) { Compat.openScreen(client, new EditHudScreen(Component.literal("Edit Notebook HUD"))); } }

            while (listNextKey.consumeClick()) { NotebookConfig.itemPage++; }
            while (listPrevKey.consumeClick()) { NotebookConfig.itemPage--; }
            while (todoNextKey.consumeClick()) { NotebookConfig.todoPage++; }
            while (todoPrevKey.consumeClick()) { NotebookConfig.todoPage--; }
        });
    }
}
