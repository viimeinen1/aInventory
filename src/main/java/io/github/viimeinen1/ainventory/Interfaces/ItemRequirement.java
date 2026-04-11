package io.github.viimeinen1.ainventory.Interfaces;

import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ItemRequirement {
    boolean run(ItemStack item);
}
