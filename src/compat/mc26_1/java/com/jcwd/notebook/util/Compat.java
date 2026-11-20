package com.jcwd.notebook.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 26.1: screen and hide-GUI state live directly on Minecraft / Options. */
public final class Compat {
    private Compat() {}

    public static boolean isHudHidden(Minecraft client) { return client.options.hideGui; }

    public static Screen currentScreen(Minecraft client) { return client.screen; }

    public static void openScreen(Minecraft client, Screen screen) { client.setScreen(screen); }
}
