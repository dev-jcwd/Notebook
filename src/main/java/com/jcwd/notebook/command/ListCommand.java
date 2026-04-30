package com.jcwd.notebook.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.jcwd.notebook.data.ListManager;
import com.jcwd.notebook.data.ItemList;
import com.jcwd.notebook.data.NotebookConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.Map;

public class ListCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            
            // Explicitly register /itemlists to just show the overview
            dispatcher.register(ClientCommandManager.literal("itemlists")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("§eYour Item Lists: §f" + ListManager.getAllLists().keySet()));
                    return 1;
                })
            );

            // Register the main /itemlist command tree
            dispatcher.register(buildTree(ClientCommandManager.literal("itemlist")));
            
            // Register the exact same tree under the /il alias
            dispatcher.register(buildTree(ClientCommandManager.literal("il")));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildTree(LiteralArgumentBuilder<FabricClientCommandSource> root) {
        return root
            .executes(context -> {
                context.getSource().sendFeedback(Text.literal("§eYour Item Lists: §f" + ListManager.getAllLists().keySet()));
                return 1;
            })
            .then(ClientCommandManager.literal("create")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        if (ListManager.createList(name)) {
                            NotebookConfig.activeItemList = name;
                            NotebookConfig.itemPage = 0; 
                            NotebookConfig.save();
                            context.getSource().sendFeedback(Text.literal("§aCreated and displaying list: §f" + name));
                        } else {
                            context.getSource().sendError(Text.literal("§cList already exists."));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("delete")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        if (ListManager.deleteList(name)) {
                            context.getSource().sendFeedback(Text.literal("§cDeleted list: §f" + name));
                        } else {
                            context.getSource().sendError(Text.literal("§cList not found."));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("display")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        if (ListManager.getList(name) != null) {
                            NotebookConfig.activeItemList = name;
                            NotebookConfig.itemPage = 0; 
                            NotebookConfig.save();
                            context.getSource().sendFeedback(Text.literal("§aNow displaying list: §f" + name));
                        } else {
                            context.getSource().sendError(Text.literal("§cList not found."));
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .then(ClientCommandManager.argument("item", StringArgumentType.word())
                        .then(ClientCommandManager.argument("quantity", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String item = StringArgumentType.getString(context, "item");
                                int quantity = IntegerArgumentType.getInteger(context, "quantity");
                                ItemList list = ListManager.getList(name);
                                
                                if (list != null) {
                                    if (!item.contains(":")) item = "minecraft:" + item;
                                    
                                    // FIXED: Validation Check for invalid items
                                    Identifier id = Identifier.tryParse(item);
                                    if (id == null || !Registries.ITEM.containsId(id)) {
                                        context.getSource().sendError(Text.literal("§cInvalid item name! §7(Example: dirt or minecraft:stone)"));
                                        return 0; // Kills the command
                                    }
                                    
                                    list.addItem(item, quantity);
                                    ListManager.saveLists();
                                    context.getSource().sendFeedback(Text.literal("§aAdded " + quantity + " " + item + " to " + name));
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            })))))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .then(ClientCommandManager.argument("item", StringArgumentType.word())
                        .then(ClientCommandManager.argument("quantity", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                String item = StringArgumentType.getString(context, "item");
                                int quantity = IntegerArgumentType.getInteger(context, "quantity");
                                ItemList list = ListManager.getList(name);
                                
                                if (list != null) {
                                    if (!item.contains(":")) item = "minecraft:" + item;
                                    if (list.removeItem(item, quantity)) {
                                        ListManager.saveLists();
                                        context.getSource().sendFeedback(Text.literal("§cRemoved " + quantity + " " + item + " from " + name));
                                    } else {
                                        context.getSource().sendError(Text.literal("§cItem not found in list."));
                                    }
                                } else {
                                    context.getSource().sendError(Text.literal("§cList not found."));
                                }
                                return 1;
                            })))))
            .then(ClientCommandManager.literal("share")
                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(context -> {
                        String name = StringArgumentType.getString(context, "name");
                        ItemList list = ListManager.getList(name);
                        if (list != null) {
                            StringBuilder sb = new StringBuilder("[NBSHARE:L:" + name + ":");
                            if (list.getItems().isEmpty()) {
                                sb.append("EMPTY");
                            } else {
                                for (Map.Entry<String, Integer> entry : list.getItems().entrySet()) {
                                    String item = entry.getKey();
                                    // COMPRESSION: Strip "minecraft:" to save massive space!
                                    if (item.startsWith("minecraft:")) {
                                        item = item.substring(10);
                                    }
                                    sb.append(item).append("=").append(entry.getValue()).append(",");
                                }
                            }
                            sb.append("]");
                            if (sb.length() > 256) {
                                context.getSource().sendError(Text.literal("§cEven with compression, this list is too large for a single chat message!"));
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
                .executes(context -> { NotebookConfig.itemPage++; return 1; }))
            .then(ClientCommandManager.literal("n")
                .executes(context -> { NotebookConfig.itemPage++; return 1; }))
            .then(ClientCommandManager.literal("previous")
                .executes(context -> { NotebookConfig.itemPage--; return 1; }))
            .then(ClientCommandManager.literal("p")
                .executes(context -> { NotebookConfig.itemPage--; return 1; }));
    }
}