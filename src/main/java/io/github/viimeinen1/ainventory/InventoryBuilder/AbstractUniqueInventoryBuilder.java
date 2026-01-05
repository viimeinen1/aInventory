package io.github.viimeinen1.ainventory.InventoryBuilder;

import io.github.viimeinen1.ainventory.Inventory.AbstractUniqueInventory;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

public abstract class AbstractUniqueInventoryBuilder <
        K extends AbstractUniqueInventoryBuilder<K, T>,
        T extends AbstractUniqueInventory<K, T>
    > extends AbstractInventoryBuilder<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, K, T> {}
