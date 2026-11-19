package com.jcwd.notebook.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 26.2 and 26.3: screen and hide-GUI state moved onto Minecraft.gui. */
public final class Compat {
    private Compat() {}

    public static boolean isHudHidden(Minecraft client) { return client.gui.hud.isHidden(); }

    public static Screen currentScreen(Minecraft client) { return client.gui.screen(); }

    public static void openScreen(Minecraft client, Screen screen) { client.gui.setScreen(screen); }
}
