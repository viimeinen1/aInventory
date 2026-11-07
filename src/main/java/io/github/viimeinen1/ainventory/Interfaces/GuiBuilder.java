package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.GUI.Gui;

/**
 * Gui builder
 *
 * @param <T> type of keys in gui.
 */
@FunctionalInterface
public interface GuiBuilder<T> {
    /**
     * Run gui builder
     * @param builder gui builder
     */
    void run(Gui.Builder<T> builder);
}
