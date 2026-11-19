package com.jcwd.notebook.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.jcwd.notebook.NotebookMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Shared JSON load/save used by the list, to-do and config files. */
final class Store {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Store() {}

    /** Returns the parsed file, or null if it is missing, empty or unreadable. A corrupt file is kept as .bak, never overwritten. */
    static <T> T load(Path file, Type type) {
        if (!Files.exists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, type);
        } catch (IOException | JsonParseException e) {
            NotebookMod.LOGGER.error("Failed to read {}", file.getFileName(), e);
            try {
                Files.move(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {}
            return null;
        }
    }

    /** Writes to a temp file first so a crash mid-save cannot truncate the real file. */
    static void save(Path file, Object data) {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            NotebookMod.LOGGER.error("Failed to write {}", file.getFileName(), e);
            return;
        }
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            NotebookMod.LOGGER.error("Failed to replace {}", file.getFileName(), e);
        }
    }
}
