package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Inventory click action on single slot
 */
@FunctionalInterface
public interface ItemClick {
    /**
     * Run inventory click action
     * @param event event
     */
    void run(InventoryClickEvent event);
}
