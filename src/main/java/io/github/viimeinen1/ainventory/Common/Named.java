package io.github.viimeinen1.ainventory.Common;

public interface Named <T extends Enum<T>, K extends InventoryProvider<?>> {
    T name();
    K provider();
}
