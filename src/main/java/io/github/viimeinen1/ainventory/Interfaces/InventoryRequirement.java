package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.entity.Player;

/**
 * Inventory requirement action
 */
@FunctionalInterface
public interface InventoryRequirement {
    boolean run(Player Player);
}
