package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.GUI.Gui;

/**
 * Gui builder
 *
 * @param <T> type of keys in gui.
 */
@FunctionalInterface
public interface GuiBuilder<T> {
    void run(Gui.Builder<T> builder);
}
