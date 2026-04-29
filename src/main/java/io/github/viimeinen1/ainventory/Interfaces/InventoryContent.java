package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.View.View;

/**
 * Inventory content building action
 */
@FunctionalInterface
public interface InventoryContent {
    void run(View.ContentBuilder builder);
}
