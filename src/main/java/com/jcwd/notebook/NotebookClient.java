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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.text.Text;

public class NotebookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NotebookConfig.load();
        ListManager.loadLists();
        TodoManager.loadLists(); 
        
        ListCommand.register();
        TodoCommand.register();
        KeybindManager.register();
        HudRenderCallback.EVENT.register(new NotebookOverlayHud());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§b[NB] §fNotebook Mod is active! Type §e[/notebook] §fto show commands!"), false);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("notebook")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("§6Todo Commands"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo] §7- shows all todo list that was created"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo create/delete <name>] §7- create and delete todo list"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo add/remove <name> <action>] §7- add and remove things on todo list"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo check <name> <action>] §7- check/uncheck a task"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo share <name>] §7- share todo on the chat"));
                    context.getSource().sendFeedback(Text.literal("§e[/todo next/previous] or [n/p] §7- flip to next/previous page"));
                    
                    context.getSource().sendFeedback(Text.literal("")); 
                    
                    context.getSource().sendFeedback(Text.literal("§6List Commands (Alias: /il)"));
                    context.getSource().sendFeedback(Text.literal("§e[/itemlists] §7- shows all the list the player created"));
                    context.getSource().sendFeedback(Text.literal("§e[/il create/delete <name>] §7- create and delete list"));
                    context.getSource().sendFeedback(Text.literal("§e[/il add/remove <name> <item> <quantity>] §7- add and remove items"));
                    context.getSource().sendFeedback(Text.literal("§e[/il share <name>] §7- share the list on the chat"));
                    context.getSource().sendFeedback(Text.literal("§e[/il next/previous] or [n/p] §7- flip to next/previous page"));
                    
                    return 1;
                })
            );
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("notebook_import")
                .then(ClientCommandManager.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(ClientCommandManager.argument("data", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
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
                                            try { list.addItem(pair[0], Integer.parseInt(pair[1])); } catch (NumberFormatException ignored) {}
                                        }
                                    }
                                    ListManager.saveLists();
                                    context.getSource().sendFeedback(Text.literal("§aSuccessfully imported List: §f" + name));
                                } else if (type.equals("T")) {
                                    TodoManager.createList(name);
                                    TodoList list = TodoManager.getList(name);
                                    for (String taskData : data.split("\\|")) {
                                        if (!taskData.isEmpty()) {
                                            String[] parts = taskData.split("~");
                                            if (parts.length == 2) {
                                                list.addTask(parts[0]);
                                                // If it was checked by the sharer, check it for the receiver!
                                                if (parts[1].equals("true")) list.toggleTask(parts[0]);
                                            } else {
                                                list.addTask(taskData);
                                            }
                                        }
                                    }
                                    TodoManager.saveLists();
                                    context.getSource().sendFeedback(Text.literal("§aSuccessfully imported To-Do: §f" + name));
                                }
                                return 1;
                            })))));
        });
    }
}