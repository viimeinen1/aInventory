package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ItemClick {
    void run(InventoryClickEvent event);
}
