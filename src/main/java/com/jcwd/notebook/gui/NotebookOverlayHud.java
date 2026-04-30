package com.jcwd.notebook.gui;

import com.jcwd.notebook.data.*;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class NotebookOverlayHud implements HudRenderCallback {

    private int countItemInInventory(MinecraftClient client, Item targetItem) {
        if (client.player == null) return 0;
        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(targetItem)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public void onHudRender(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int boxWidth = 140; 
        int itemsPerPage = 10;


        if (NotebookConfig.showItemList && NotebookConfig.activeItemList != null && !NotebookConfig.activeItemList.isEmpty()) {
            ItemList activeList = ListManager.getList(NotebookConfig.activeItemList);
            if (activeList != null) {
                int x = NotebookConfig.itemListX;
                int y = NotebookConfig.itemListY;
                boolean isRightAligned = x > (screenWidth / 2);


                List<Map.Entry<String, Integer>> entryList = new ArrayList<>(activeList.getItems().entrySet());
                int totalPages = Math.max(1, (int) Math.ceil((double) entryList.size() / itemsPerPage));
                if (NotebookConfig.itemPage >= totalPages) NotebookConfig.itemPage = totalPages - 1;
                if (NotebookConfig.itemPage < 0) NotebookConfig.itemPage = 0;

                int startIndex = NotebookConfig.itemPage * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, entryList.size());

                String pageString = totalPages > 1 ? " (" + (NotebookConfig.itemPage + 1) + "/" + totalPages + ")" : "";
                String title = "List: " + activeList.getName() + pageString;
                int titleX = isRightAligned ? x + boxWidth - textRenderer.getWidth(title) : x;
                
                context.drawTextWithShadow(textRenderer, title, titleX, y, 0xFFFFAA00); 
                y += 12;

                for (int i = startIndex; i < endIndex; i++) {
                    Map.Entry<String, Integer> entry = entryList.get(i);
                    String itemStr = entry.getKey();
                    int targetQuantity = entry.getValue();
                    Identifier id = Identifier.tryParse(itemStr);
                    
                    if (id != null && Registries.ITEM.containsId(id)) {
                        Item item = Registries.ITEM.get(id);
                        ItemStack stack = item.getDefaultStack();
                        int currentCount = countItemInInventory(client, item);
                        String progressText = stack.getName().getString() + " " + currentCount + " / " + targetQuantity;
                        

                        int color;
                        if (currentCount > targetQuantity) {
                            color = 0xFFFF5555; // Red (Over limit)
                        } else if (currentCount == targetQuantity) {
                            color = 0xFF55FF55; // Green (Exact match)
                        } else {
                            color = 0xFFFFFFFF; // White (Still gathering)
                        }
                        
                        int iconX = isRightAligned ? x + boxWidth - 16 : x;
                        int textX = isRightAligned ? iconX - textRenderer.getWidth(progressText) - 4 : x + 20;

                        context.drawItem(stack, iconX, y);
                        context.drawTextWithShadow(textRenderer, progressText, textX, y + 4, color);
                    } else {
                        String fallback = "- " + itemStr + " ? / " + targetQuantity;
                        int textX = isRightAligned ? x + boxWidth - textRenderer.getWidth(fallback) : x;
                        
                        context.drawTextWithShadow(textRenderer, fallback, textX, y + 4, 0xFFAAAAAA);
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


                List<Map.Entry<String, Boolean>> todoList = new ArrayList<>(activeTodo.getTasks().entrySet());
                int totalPages = Math.max(1, (int) Math.ceil((double) todoList.size() / itemsPerPage));
                if (NotebookConfig.todoPage >= totalPages) NotebookConfig.todoPage = totalPages - 1;
                if (NotebookConfig.todoPage < 0) NotebookConfig.todoPage = 0;

                int startIndex = NotebookConfig.todoPage * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, todoList.size());

                String pageString = totalPages > 1 ? " (" + (NotebookConfig.todoPage + 1) + "/" + totalPages + ")" : "";
                String title = "To-Do: " + activeTodo.getName() + pageString;
                int titleX = isRightAligned ? x + boxWidth - textRenderer.getWidth(title) : x;
                
                context.drawTextWithShadow(textRenderer, title, titleX, y, 0xFF55FF55);
                y += 12;

                for (int i = startIndex; i < endIndex; i++) {
                    Map.Entry<String, Boolean> entry = todoList.get(i);
                    String task = entry.getKey();
                    boolean isChecked = entry.getValue();
                    
                    int color = isChecked ? 0xFFAAAAAA : 0xFFFFFFFF;
                    int visualIndex = i + 1;

                    String fullText;
                    if (isRightAligned) {
                        String suffix = activeTodo.usesNumbers() ? (" ." + visualIndex) : "";
                        fullText = (isChecked ? "§m" : "") + task + "§r " + (isChecked ? "[x]" : "[ ]") + suffix;
                    } else {
                        String prefix = activeTodo.usesNumbers() ? (visualIndex + ". ") : "• ";
                        fullText = prefix + (isChecked ? "[x] §m" : "[ ] ") + task;
                    }

                    int textX = isRightAligned ? x + boxWidth - textRenderer.getWidth(fullText) : x;
                    context.drawTextWithShadow(textRenderer, fullText, textX, y, color);
                    y += 12; 
                }
            }
        }
    }
}