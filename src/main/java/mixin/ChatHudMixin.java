package com.jcwd.notebook.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Text modifyNotebookShareMessage(Text message) {
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

                    
                    MutableText clickable = Text.literal("§7[Notebook shared! " + displayType + " " + name + " §nclick to add on your notebook!§7]");
                    clickable.setStyle(clickable.getStyle()
                        .withClickEvent(new ClickEvent.RunCommand("/notebook_import " + type + " " + name + " " + data))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("§aClick to import!"))));

                    return Text.literal(prefix).append(clickable);
                }
            }
        }
        return message;
    }
}