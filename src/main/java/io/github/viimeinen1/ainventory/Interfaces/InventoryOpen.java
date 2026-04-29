package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Inventory open action
 */
@FunctionalInterface
public interface InventoryOpen {
    void run(InventoryOpenEvent event);
}
