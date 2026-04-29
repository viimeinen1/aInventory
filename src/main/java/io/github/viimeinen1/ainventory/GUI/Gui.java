package io.github.viimeinen1.ainventory.GUI;

import io.github.viimeinen1.ainventory.Interfaces.GuiBuilder;
import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;
import org.bukkit.entity.HumanEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI for storing multiple inventories easier.
 *
 * @param <T> Type of keys in this GUI
 */
public class Gui<T> {

    /**
     * Inventories in this GUI
     */
    public final Map<T, AbstractInventory> inventories = new HashMap<>();

    /**
     * Open inventory that is associated with that key.
     * If there is no such inventory, will fail silently.
     *
     * @param key key of the inventory
     * @param player player to open the inventory to
     */
    public void open(T key, HumanEntity player) {
        var inv = inventories.get(key);
        if (inv == null) return;
        inv.open(player);
    }

    /**
     * If this GUI has inventory with this key.
     *
     * @param key key
     * @return if GUI contains inventory
     */
    public boolean has(T key) {
        return inventories.containsKey(key);
    }

    /**
     * Get inventory associated with this key-
     *
     * @param key key
     * @return AbstractInventory, or null if no inventory exists with that key.
     */
    public AbstractInventory get(T key) {
        return inventories.get(key);
    }

    /**
     * Create new GUI.
     *
     * @param builder GUI builder
     * @return new GUI
     * @param <T> type of keys in the GUI
     */
    public static <T> Gui<T> from(GuiBuilder<T> builder) {
        var GUI = new Gui<T>();
        builder.run(new Builder<>(GUI));
        return GUI;
    }

    /**
     * GUI builder
     *
     * @param GUI GUI
     * @param <T> type of keys in the GUI
     */
    public record Builder<T>(Gui<T> GUI) {

        /**
         * Create new inventory and set it to this key in the GUI.
         *
         * @param key key to associate the inventory in the GUI
         * @return gui inventory builder
         */
        public GuiInventoryBuilder<T> set(T key) {
            return new GuiInventoryBuilder<>(key, GUI);
        }
    }

}
