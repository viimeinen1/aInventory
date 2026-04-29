package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Inventory click action on single slot
 */
@FunctionalInterface
public interface ItemClick {
    void run(InventoryClickEvent event);
}
