package io.github.viimeinen1.ainventory.Common;

import io.github.viimeinen1.ainventory.GUI.AbstractGUI;

public interface Named <T extends Enum<T>, K extends AbstractGUI<T, ?, ?, ?, ?>> {
    public T name();
    public K provider();
}
