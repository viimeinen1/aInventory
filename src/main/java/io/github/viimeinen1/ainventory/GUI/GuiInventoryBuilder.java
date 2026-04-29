package io.github.viimeinen1.ainventory.GUI;

import io.github.viimeinen1.ainventory.Inventory.Inventory;
import io.github.viimeinen1.ainventory.Inventory.SharedInventory;

/**
 * Gui inventory builder.
 *
 * @param <T> key type.
 */
public class GuiInventoryBuilder<T> extends Inventory.Builder {

    /**
     * GUI Associated with this builder.
     */
    public final Gui<T> GUI;

    /**
     * Name of the inventory associated with this builder.
     */
    public final T key;

    /**
     * Create new gui inventory builder.
     *
     * @param name unique name of the inventory in the GUI
     * @param GUI GUI to add the inventory to
     */
    public GuiInventoryBuilder(T name, Gui<T> GUI) {
        this.key = name;
        this.GUI = GUI;
    }

    @Override
    public SharedInventory buildShared() {
        var inv = new SharedInventory(this);
        GUI.inventories.put(key, inv);
        return inv;
    }

    @Override
    public Inventory build() {
        var inv = new Inventory(this);
        GUI.inventories.put(key, inv);
        return inv;
    }
}
