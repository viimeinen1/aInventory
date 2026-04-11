package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryCloseEvent;

@FunctionalInterface
public interface InventoryClose {
    void run(InventoryCloseEvent event);
}
