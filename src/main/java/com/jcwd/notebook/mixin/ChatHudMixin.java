package com.jcwd.notebook.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatHudMixin {

    // Every chat/system message funnels through this one private method.
    @ModifyVariable(method = "addMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Component modifyNotebookShareMessage(Component message) {
        String rawStr = message.getString();
        int startIndex = rawStr.indexOf("[NBSHARE:");

        if (startIndex != -1) {
            int endIndex = rawStr.indexOf("]", startIndex);
            if (endIndex != -1) {

                String payload = rawStr.substring(startIndex + 9, endIndex);
                String[] parts = payload.split(":", 3);

                if (parts.length >= 2) {
                    String type = parts[0];
                    String name = parts[1];
                    String data = parts.length == 3 ? parts[2] : "EMPTY";
                    String displayType = type.equals("L") ? "List" : "Todo";
                    String prefix = rawStr.substring(0, startIndex);

                    MutableComponent clickable = Component.literal("§7[Notebook shared! " + displayType + " " + name + " §nclick to add on your notebook!§7]");
                    clickable.setStyle(clickable.getStyle()
                        .withClickEvent(new ClickEvent.RunCommand("/notebook_import " + type + " " + name + " " + data))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("§aClick to import!"))));

                    return Component.literal(prefix).append(clickable);
                }
            }
        }
        return message;
    }
}
