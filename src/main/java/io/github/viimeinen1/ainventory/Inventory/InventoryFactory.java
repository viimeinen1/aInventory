package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.InventoryBuilder.DefaultInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryBuilder.UniqueInventoryBuilder;

public class InventoryFactory {
    
    /**
     * Get default inventory builder. (modifications are synced to everyone, including page changes).
     * 
     * @return default inventory builder.
     */
    public static DefaultInventoryBuilder builder() {
        return new DefaultInventoryBuilder();
    }

    /**
     * Get unique inventory builder. (everyone has their own view of the inventory. Syncing happens manually with initialize() and reload()).
     * 
     * @return unique inventory builder.
     */
    public static UniqueInventoryBuilder uniqueBuilder() {
        return new UniqueInventoryBuilder();
    }

}
