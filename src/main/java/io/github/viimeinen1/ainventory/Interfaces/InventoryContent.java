package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.View.View;

/**
 * Inventory content building action
 */
@FunctionalInterface
public interface InventoryContent {
    /**
     * Run inventory content builder
     * @param builder content builder
     */
    void run(View.ContentBuilder builder);
}
