package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Inventory close action
 */
@FunctionalInterface
public interface InventoryClose {
    /**
     * Run inventory close function
     * @param event close event
     */
    void run(InventoryCloseEvent event);
}
