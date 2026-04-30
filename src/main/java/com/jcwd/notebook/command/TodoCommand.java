package com.jcwd.notebook.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.jcwd.notebook.data.TodoManager;
import com.jcwd.notebook.data.TodoList;
import com.jcwd.notebook.data.NotebookConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import java.util.Map;

public class TodoCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("todo")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("§eYour To-Do Lists: §f" + TodoManager.getAllLists().keySet()));
                    return 1;
                })
                .then(ClientCommandManager.literal("create")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.createList(name)) {
                                NotebookConfig.activeTodoList = name;
                                NotebookConfig.todoPage = 0;
                                NotebookConfig.save();
                                context.getSource().sendFeedback(Text.literal("§aCreated and displaying To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Text.literal("§cList already exists."));
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("delete")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.deleteList(name)) {
                                context.getSource().sendFeedback(Text.literal("§cDeleted To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Text.literal("§cList not found."));
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("display")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (TodoManager.getList(name) != null) {
                                NotebookConfig.activeTodoList = name;
                                NotebookConfig.todoPage = 0;
                                NotebookConfig.save();
                                context.getSource().sendFeedback(Text.literal("§aNow displaying To-Do list: §f" + name));
                            } else {
                                context.getSource().sendError(Text.literal("§cList not found."));
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .then(ClientCommandManager.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    list.getTasks().put(action, false);
                                    TodoManager.saveLists();
                                    context.getSource().sendFeedback(Text.literal("§aAdded task to " + name));
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .then(ClientCommandManager.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().remove(action) != null) {
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Text.literal("§cRemoved task from " + name));
                                    } else {
                                        context.getSource().sendError(Text.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                // EXPLICIT CHECK COMMAND
                .then(ClientCommandManager.literal("check")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .then(ClientCommandManager.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().containsKey(action)) {
                                        list.getTasks().put(action, true);
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Text.literal("§aChecked task in " + name));
                                    } else {
                                        context.getSource().sendError(Text.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                // EXPLICIT UNCHECK COMMAND
                .then(ClientCommandManager.literal("uncheck")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .then(ClientCommandManager.argument("action", StringArgumentType.greedyString())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String action = StringArgumentType.getString(context, "action");
                                TodoList list = TodoManager.getList(name);
                                if (list != null) {
                                    if (list.getTasks().containsKey(action)) {
                                        list.getTasks().put(action, false);
                                        TodoManager.saveLists();
                                        context.getSource().sendFeedback(Text.literal("§eUnchecked task in " + name));
                                    } else {
                                        context.getSource().sendError(Text.literal("§cTask not found."));
                                    }
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            }))))
                .then(ClientCommandManager.literal("share")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            TodoList list = TodoManager.getList(name);
                            if (list != null) {
                                StringBuilder sb = new StringBuilder("[NBSHARE:T:" + name + ":");
                                if (list.getTasks().isEmpty()) {
                                    sb.append("EMPTY");
                                } else {
                                    for (Map.Entry<String, Boolean> entry : list.getTasks().entrySet()) {
                                        String state = entry.getValue() ? "1" : "0";
                                        sb.append(entry.getKey()).append("=").append(state).append(",");
                                    }
                                }
                                sb.append("]");
                                if (sb.length() > 256) {
                                    context.getSource().sendError(Text.literal("§cThis To-Do list is too large for a single chat message!"));
                                } else {
                                    net.minecraft.client.MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(sb.toString());
                                }
                            } else {
                                context.getSource().sendError(Text.literal("§cList not found."));
                            }
                            return 1;
                        })))
                // PAGINATION COMMANDS
                .then(ClientCommandManager.literal("next")
                    .executes(context -> { NotebookConfig.todoPage++; return 1; }))
                .then(ClientCommandManager.literal("n")
                    .executes(context -> { NotebookConfig.todoPage++; return 1; }))
                .then(ClientCommandManager.literal("previous")
                    .executes(context -> { NotebookConfig.todoPage--; return 1; }))
                .then(ClientCommandManager.literal("p")
                    .executes(context -> { NotebookConfig.todoPage--; return 1; }))
            );
        });
    }
}