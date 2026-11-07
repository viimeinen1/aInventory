package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.entity.Player;

/**
 * Inventory requirement action
 */
@FunctionalInterface
public interface InventoryRequirement {
    /**
     * Run inventory requirement function.
     * @param Player player
     * @return true if player can access this inventory
     */
    boolean run(Player Player);
}
