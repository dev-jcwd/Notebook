package com.jcwd.notebook.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.jcwd.notebook.data.TodoManager;
import com.jcwd.notebook.data.TodoList;
import com.jcwd.notebook.data.NotebookConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import java.util.Map;

public class TodoCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("todo")
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal("§eYour To-Do Lists: §f" + TodoManager.getAllLists().keySet()));
                    return 1;
                })
                .then(ClientCommands.literal("create")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.createList(name)) {
                                NotebookConfig.activeTodoList = name;
                                NotebookConfig.todoPage = 0;
                                NotebookConfig.save();
                                context.getSource().sendFeedback(Component.literal("§aCreated and displaying To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Component.literal("§cList already exists."));
                            }
                            return 1;
                        })))
                .then(ClientCommands.literal("delete")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.deleteList(name)) {
                                context.getSource().sendFeedback(Component.literal("§cDeleted To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Component.literal("§cList not found."));
                            }
                            return 1;
                        })))
                .then(ClientCommands.literal("display")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.getList(name) != null) {
                                NotebookConfig.activeTodoList = name;
                                NotebookConfig.todoPage = 0;
                                NotebookConfig.save();
                                context.getSource().sendFeedback(Component.literal("§aNow displaying To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Component.literal("§cList not found."));
                            }
                            return 1;
                        })))
                .then(ClientCommands.literal("add")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .then(ClientCommands.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    list.addTask(action);
                                    TodoManager.saveLists();
                                    context.getSource().sendFeedback(Component.literal("§aAdded task to " + name));
                                } else {
                                    context.getSource().sendError(Component.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                .then(ClientCommands.literal("remove")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .then(ClientCommands.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().remove(action) != null) {
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Component.literal("§cRemoved task from " + name));
                                    } else {
                                        context.getSource().sendError(Component.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Component.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                // EXPLICIT CHECK COMMAND
                .then(ClientCommands.literal("check")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .then(ClientCommands.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().containsKey(action)) {
                                        list.getTasks().put(action, true);
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Component.literal("§aChecked task in " + name));
                                    } else {
                                        context.getSource().sendError(Component.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Component.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                // EXPLICIT UNCHECK COMMAND
                .then(ClientCommands.literal("uncheck")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .then(ClientCommands.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().containsKey(action)) {
                                        list.getTasks().put(action, false);
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Component.literal("§eUnchecked task in " + name));
                                    } else {
                                        context.getSource().sendError(Component.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Component.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                .then(ClientCommands.literal("share")
                    .then(ClientCommands.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            TodoList list = TodoManager.getList(name);
                            if (list != null) {
                                StringBuilder sb = new StringBuilder("[NBSHARE:T:" + name + ":");
                                if (list.getTasks().isEmpty()) {
                                    sb.append("EMPTY");
                                } else {
                                    for (Map.Entry<String, Boolean> entry : list.getTasks().entrySet()) {
                                        // "~", "|" and "]" are share-format delimiters, so they cannot appear inside a task
                                        String task = entry.getKey().replaceAll("[~|\\]]", " ");
                                        sb.append(task).append("~").append(entry.getValue() ? "1" : "0").append("|");
                                    }
                                }
                                sb.append("]");
                                if (sb.length() > 256) {
                                    context.getSource().sendError(Component.literal("§cThis To-Do list is too large for a single chat message!"));
                                } else {
                                    context.getSource().getPlayer().connection.sendChat(sb.toString());
                                }
                            } else {
                                context.getSource().sendError(Component.literal("§cList not found."));
                            }
                            return 1;
                        })))
                // PAGINATION COMMANDS
                .then(ClientCommands.literal("next")
                    .executes(context -> { NotebookConfig.todoPage++; return 1; }))
                .then(ClientCommands.literal("n")
                    .executes(context -> { NotebookConfig.todoPage++; return 1; }))
                .then(ClientCommands.literal("previous")
                    .executes(context -> { NotebookConfig.todoPage--; return 1; }))
                .then(ClientCommands.literal("p")
                    .executes(context -> { NotebookConfig.todoPage--; return 1; }))
            );
        });
    }
}