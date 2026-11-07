package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.inventory.ItemStack;

/**
 *  If item should be allowed here.
 */
@FunctionalInterface
public interface ItemRequirement {
    /**
     * Check if item is allowed here.
     * @param item ItemStack
     * @return true if item is allowed
     */
    boolean isAllowed(ItemStack item);
}
