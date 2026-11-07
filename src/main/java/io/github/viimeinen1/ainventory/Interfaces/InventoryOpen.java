package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Inventory open action
 */
@FunctionalInterface
public interface InventoryOpen {
    /**
     * Run inventory open function
     * @param event open event
     */
    void run(InventoryOpenEvent event);
}
