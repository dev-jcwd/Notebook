package com.jcwd.notebook.gui;

import com.jcwd.notebook.data.NotebookConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditHudScreen extends Screen {
    private boolean draggingItems = false;
    private boolean draggingTodos = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean wasMouseDown = false;
    private final int boxWidth = 140; 
    private final int boxHeight = 150; 

    public EditHudScreen(Text title) { super(title); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

        if (isMouseDown) {
            if (!wasMouseDown) {

                if (mouseX >= NotebookConfig.itemListX && mouseX <= NotebookConfig.itemListX + boxWidth && mouseY >= NotebookConfig.itemListY && mouseY <= NotebookConfig.itemListY + boxHeight) {
                    draggingItems = true; dragOffsetX = mouseX - NotebookConfig.itemListX; dragOffsetY = mouseY - NotebookConfig.itemListY;
                } else if (mouseX >= NotebookConfig.todoListX && mouseX <= NotebookConfig.todoListX + boxWidth && mouseY >= NotebookConfig.todoListY && mouseY <= NotebookConfig.todoListY + boxHeight) {
                    draggingTodos = true; dragOffsetX = mouseX - NotebookConfig.todoListX; dragOffsetY = mouseY - NotebookConfig.todoListY;
                }
            } else {

                int maxX = this.width - boxWidth;
                int maxY = this.height - boxHeight;

                if (draggingItems) { 
                    NotebookConfig.itemListX = Math.max(0, Math.min(mouseX - dragOffsetX, maxX)); 
                    NotebookConfig.itemListY = Math.max(0, Math.min(mouseY - dragOffsetY, maxY)); 
                }
                if (draggingTodos) { 
                    NotebookConfig.todoListX = Math.max(0, Math.min(mouseX - dragOffsetX, maxX)); 
                    NotebookConfig.todoListY = Math.max(0, Math.min(mouseY - dragOffsetY, maxY)); 
                }
            }
        } else {

            if (wasMouseDown && (draggingItems || draggingTodos)) {
                draggingItems = false; draggingTodos = false; NotebookConfig.save();
            }
        }
        wasMouseDown = isMouseDown;

        context.fill(NotebookConfig.itemListX, NotebookConfig.itemListY, NotebookConfig.itemListX + boxWidth, NotebookConfig.itemListY + boxHeight, 0x4400FF00);
        context.drawTextWithShadow(this.textRenderer, "Item List (Drag Me)", NotebookConfig.itemListX + 5, NotebookConfig.itemListY + 5, 0xFFFFFFFF);
        
        context.fill(NotebookConfig.todoListX, NotebookConfig.todoListY, NotebookConfig.todoListX + boxWidth, NotebookConfig.todoListY + boxHeight, 0x440000FF);
        context.drawTextWithShadow(this.textRenderer, "To-Do List (Drag Me)", NotebookConfig.todoListX + 5, NotebookConfig.todoListY + 5, 0xFFFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}