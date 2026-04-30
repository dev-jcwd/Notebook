package com.jcwd.notebook;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotebookMod implements ModInitializer {
    public static final String MOD_ID = "notebook";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Notebook Mod initialized!");
    }
}