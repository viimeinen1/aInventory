package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Inventory close action
 */
@FunctionalInterface
public interface InventoryClose {
    void run(InventoryCloseEvent event);
}
