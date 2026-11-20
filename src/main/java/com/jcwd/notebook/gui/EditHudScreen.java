package com.jcwd.notebook.gui;

import com.jcwd.notebook.data.NotebookConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class EditHudScreen extends Screen {
    private boolean draggingItems = false;
    private boolean draggingTodos = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private final int boxWidth = 140;
    private final int boxHeight = 150;

    public EditHudScreen(Component title) { super(title); }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();
            if (inside(mx, my, NotebookConfig.itemListX, NotebookConfig.itemListY, boxWidth, boxHeight)) {
                draggingItems = true; dragOffsetX = mx - NotebookConfig.itemListX; dragOffsetY = my - NotebookConfig.itemListY;
                return true;
            } else if (inside(mx, my, NotebookConfig.todoListX, NotebookConfig.todoListY, boxWidth, boxHeight)) {
                draggingTodos = true; dragOffsetX = mx - NotebookConfig.todoListX; dragOffsetY = my - NotebookConfig.todoListY;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        int maxX = this.width - boxWidth;
        int maxY = this.height - boxHeight;
        int mx = (int) event.x();
        int my = (int) event.y();

        if (draggingItems) {
            NotebookConfig.itemListX = Math.max(0, Math.min(mx - dragOffsetX, maxX));
            NotebookConfig.itemListY = Math.max(0, Math.min(my - dragOffsetY, maxY));
            return true;
        }
        if (draggingTodos) {
            NotebookConfig.todoListX = Math.max(0, Math.min(mx - dragOffsetX, maxX));
            NotebookConfig.todoListY = Math.max(0, Math.min(my - dragOffsetY, maxY));
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingItems || draggingTodos) {
            draggingItems = false; draggingTodos = false;
            NotebookConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(NotebookConfig.itemListX, NotebookConfig.itemListY, NotebookConfig.itemListX + boxWidth, NotebookConfig.itemListY + boxHeight, 0x4400FF00);
        graphics.text(this.font, "Item List (Drag Me)", NotebookConfig.itemListX + 5, NotebookConfig.itemListY + 5, 0xFFFFFFFF, true);

        graphics.fill(NotebookConfig.todoListX, NotebookConfig.todoListY, NotebookConfig.todoListX + boxWidth, NotebookConfig.todoListY + boxHeight, 0x440000FF);
        graphics.text(this.font, "To-Do List (Drag Me)", NotebookConfig.todoListX + 5, NotebookConfig.todoListY + 5, 0xFFFFFFFF, true);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
