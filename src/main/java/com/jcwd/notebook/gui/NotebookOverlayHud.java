package com.jcwd.notebook.gui;

import com.jcwd.notebook.data.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import com.jcwd.notebook.util.Compat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotebookOverlayHud implements HudElement {

    private static final int BOX_WIDTH = 140;
    private static final int ITEMS_PER_PAGE = 10;

    /** Resolved once per item id: registry lookup, default stack and display name never change at runtime. */
    private record Resolved(Item item, ItemStack stack, String name) {}
    private static final Resolved UNKNOWN = new Resolved(null, null, null);
    private final Map<String, Resolved> resolved = new HashMap<>();
    private final Map<Item, Integer> inventoryCounts = new HashMap<>();

    private Resolved resolve(String itemStr) {
        return resolved.computeIfAbsent(itemStr, key -> {
            Identifier id = Identifier.tryParse(key);
            if (id == null) return UNKNOWN;
            return BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> {
                    ItemStack stack = item.getDefaultInstance();
                    return new Resolved(item, stack, stack.getHoverName().getString());
                })
                .orElse(UNKNOWN);
        });
    }

    /** One pass over the inventory per frame instead of one pass per listed item. */
    private void countInventory(Minecraft client) {
        inventoryCounts.clear();
        if (client.player == null) return;
        Inventory inventory = client.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) inventoryCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
    }

    private static int totalPages(int size) {
        return Math.max(1, (size + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (Compat.isHudHidden(client)) return;

        Font font = client.font;
        int screenWidth = client.getWindow().getGuiScaledWidth();

        if (NotebookConfig.showItemList && NotebookConfig.activeItemList != null && !NotebookConfig.activeItemList.isEmpty()) {
            ItemList activeList = ListManager.getList(NotebookConfig.activeItemList);
            if (activeList != null) {
                countInventory(client);
                int x = NotebookConfig.itemListX;
                int y = NotebookConfig.itemListY;
                boolean isRightAligned = x > (screenWidth / 2);

                Map<String, Integer> entries = activeList.getItems();
                int totalPages = totalPages(entries.size());
                NotebookConfig.itemPage = Math.max(0, Math.min(NotebookConfig.itemPage, totalPages - 1));

                String pageString = totalPages > 1 ? " (" + (NotebookConfig.itemPage + 1) + "/" + totalPages + ")" : "";
                String title = "List: " + activeList.getName() + pageString;
                int titleX = isRightAligned ? x + BOX_WIDTH - font.width(title) : x;

                graphics.text(font, title, titleX, y, 0xFFFFAA00, true);
                y += 12;

                Iterator<Map.Entry<String, Integer>> it = entries.entrySet().iterator();
                for (int skip = NotebookConfig.itemPage * ITEMS_PER_PAGE; skip > 0 && it.hasNext(); skip--) it.next();

                for (int shown = 0; shown < ITEMS_PER_PAGE && it.hasNext(); shown++) {
                    Map.Entry<String, Integer> entry = it.next();
                    int targetQuantity = entry.getValue();
                    Resolved r = resolve(entry.getKey());

                    if (r != UNKNOWN) {
                        int currentCount = inventoryCounts.getOrDefault(r.item(), 0);
                        String progressText = r.name() + " " + currentCount + " / " + targetQuantity;

                        int color;
                        if (currentCount > targetQuantity) {
                            color = 0xFFFF5555; // Red (Over limit)
                        } else if (currentCount == targetQuantity) {
                            color = 0xFF55FF55; // Green (Exact match)
                        } else {
                            color = 0xFFFFFFFF; // White (Still gathering)
                        }

                        int iconX = isRightAligned ? x + BOX_WIDTH - 16 : x;
                        int textX = isRightAligned ? iconX - font.width(progressText) - 4 : x + 20;

                        graphics.item(r.stack(), iconX, y);
                        graphics.text(font, progressText, textX, y + 4, color, true);
                    } else {
                        String fallback = "- " + entry.getKey() + " ? / " + targetQuantity;
                        int textX = isRightAligned ? x + BOX_WIDTH - font.width(fallback) : x;

                        graphics.text(font, fallback, textX, y + 4, 0xFFAAAAAA, true);
                    }
                    y += 18;
                }
            }
        }

        if (NotebookConfig.showTodoList && NotebookConfig.activeTodoList != null && !NotebookConfig.activeTodoList.isEmpty()) {
            TodoList activeTodo = TodoManager.getList(NotebookConfig.activeTodoList);
            if (activeTodo != null) {
                int x = NotebookConfig.todoListX;
                int y = NotebookConfig.todoListY;
                boolean isRightAligned = x > (screenWidth / 2);

                Map<String, Boolean> tasks = activeTodo.getTasks();
                int totalPages = totalPages(tasks.size());
                NotebookConfig.todoPage = Math.max(0, Math.min(NotebookConfig.todoPage, totalPages - 1));

                String pageString = totalPages > 1 ? " (" + (NotebookConfig.todoPage + 1) + "/" + totalPages + ")" : "";
                String title = "To-Do: " + activeTodo.getName() + pageString;
                int titleX = isRightAligned ? x + BOX_WIDTH - font.width(title) : x;

                graphics.text(font, title, titleX, y, 0xFF55FF55, true);
                y += 12;

                int visualIndex = NotebookConfig.todoPage * ITEMS_PER_PAGE;
                Iterator<Map.Entry<String, Boolean>> it = tasks.entrySet().iterator();
                for (int skip = visualIndex; skip > 0 && it.hasNext(); skip--) it.next();

                for (int shown = 0; shown < ITEMS_PER_PAGE && it.hasNext(); shown++) {
                    Map.Entry<String, Boolean> entry = it.next();
                    String task = entry.getKey();
                    boolean isChecked = entry.getValue();
                    visualIndex++;

                    int color = isChecked ? 0xFFAAAAAA : 0xFFFFFFFF;

                    String fullText;
                    if (isRightAligned) {
                        String suffix = activeTodo.usesNumbers() ? (" ." + visualIndex) : "";
                        fullText = (isChecked ? "§m" : "") + task + "§r " + (isChecked ? "[x]" : "[ ]") + suffix;
                    } else {
                        String prefix = activeTodo.usesNumbers() ? (visualIndex + ". ") : "• ";
                        fullText = prefix + (isChecked ? "[x] §m" : "[ ] ") + task;
                    }

                    int textX = isRightAligned ? x + BOX_WIDTH - font.width(fullText) : x;
                    graphics.text(font, fullText, textX, y, color, true);
                    y += 12;
                }
            }
        }
    }
}
