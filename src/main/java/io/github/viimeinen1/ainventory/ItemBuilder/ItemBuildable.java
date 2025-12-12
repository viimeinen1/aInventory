package io.github.viimeinen1.ainventory.ItemBuilder;

import java.util.Map;

import org.bukkit.inventory.Inventory;

import io.github.viimeinen1.ainventory.InventoryView.ItemData;

/**
 * Inventory that items can be built on using something that extends {@link AbstractItemBuilder}
 */
public interface ItemBuildable <A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {
    public Map<Integer, ItemData<A, B>> itemData();
    public Inventory getInventory();
}
