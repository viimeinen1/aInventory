package io.github.viimeinen1.ainventory.Common;

import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;

public interface InventoryProvider <K extends AbstractInventory<?, ?, ?, ?> & Named<?, ?>> {
    public K put(K inventory);
}
