package com.jcwd.notebook;

import com.jcwd.notebook.command.ListCommand;
import com.jcwd.notebook.command.TodoCommand;
import com.jcwd.notebook.data.ItemList;
import com.jcwd.notebook.data.ListManager;
import com.jcwd.notebook.data.NotebookConfig;
import com.jcwd.notebook.data.TodoList;
import com.jcwd.notebook.data.TodoManager;
import com.jcwd.notebook.gui.NotebookOverlayHud;
import com.jcwd.notebook.util.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NotebookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NotebookConfig.load();
        ListManager.loadLists();
        TodoManager.loadLists(); 
        
        ListCommand.register();
        TodoCommand.register();
        KeybindManager.register();
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
            Identifier.fromNamespaceAndPath(NotebookMod.MOD_ID, "overlay"), new NotebookOverlayHud());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                client.player.sendSystemMessage(Component.literal("§b[NB] §fNotebook Mod is active! Type §e[/notebook] §fto show commands!"));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("notebook")
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal("§6Todo Commands"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo] §7- shows all todo list that was created"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo create/delete <name>] §7- create and delete todo list"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo add/remove <name> <action>] §7- add and remove things on todo list"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo check <name> <action>] §7- check/uncheck a task"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo share <name>] §7- share todo on the chat"));
                    context.getSource().sendFeedback(Component.literal("§e[/todo next/previous] or [n/p] §7- flip to next/previous page"));
                    
                    context.getSource().sendFeedback(Component.literal("")); 
                    
                    context.getSource().sendFeedback(Component.literal("§6List Commands (Alias: /il)"));
                    context.getSource().sendFeedback(Component.literal("§e[/itemlists] §7- shows all the list the player created"));
                    context.getSource().sendFeedback(Component.literal("§e[/il create/delete <name>] §7- create and delete list"));
                    context.getSource().sendFeedback(Component.literal("§e[/il add/remove <name> <item> <quantity>] §7- add and remove items"));
                    context.getSource().sendFeedback(Component.literal("§e[/il share <name>] §7- share the list on the chat"));
                    context.getSource().sendFeedback(Component.literal("§e[/il next/previous] or [n/p] §7- flip to next/previous page"));
                    
                    return 1;
                })
            );
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("notebook_import")
                .then(ClientCommands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .then(ClientCommands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(ClientCommands.argument("data", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                            .executes(context -> {
                                String type = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "type");
                                String name = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "name");
                                String data = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "data");

                                if (data.equals("EMPTY")) data = "";

                                if (type.equals("L")) {
                                    ListManager.createList(name);
                                    ItemList list = ListManager.getList(name);
                                    for (String itemStr : data.split(",")) {
                                        if (itemStr.isEmpty()) continue;
                                        String[] pair = itemStr.split("=");
                                        if (pair.length == 2) {
                                            // The sharer strips "minecraft:", so restore it; skip anything that is not a real item.
                                            String item = ItemList.normalize(pair[0]);
                                            Identifier id = Identifier.tryParse(item);
                                            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
                                            try {
                                                int quantity = Integer.parseInt(pair[1]);
                                                if (quantity > 0) list.addItem(item, quantity);
                                            } catch (NumberFormatException ignored) {}
                                        }
                                    }
                                    ListManager.saveLists();
                                    context.getSource().sendFeedback(Component.literal("§aSuccessfully imported List: §f" + name));
                                } else if (type.equals("T")) {
                                    TodoManager.createList(name);
                                    TodoList list = TodoManager.getList(name);
                                    for (String taskData : data.split("\\|")) {
                                        if (!taskData.isEmpty()) {
                                            String[] parts = taskData.split("~");
                                            if (parts.length == 2) {
                                                list.addTask(parts[0]);
                                                // If it was checked by the sharer, check it for the receiver!
                                                if (parts[1].equals("1") || parts[1].equals("true")) list.getTasks().put(parts[0].trim(), true);
                                            } else {
                                                list.addTask(taskData);
                                            }
                                        }
                                    }
                                    TodoManager.saveLists();
                                    context.getSource().sendFeedback(Component.literal("§aSuccessfully imported To-Do: §f" + name));
                                }
                                return 1;
                            })))));
        });
    }
}