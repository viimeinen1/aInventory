package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.inventory.ItemStack;

/**
 *  If item should be allowed to be placed to this slot
 */
@FunctionalInterface
public interface ItemRequirement {
    boolean isAllowed(ItemStack item);
}
