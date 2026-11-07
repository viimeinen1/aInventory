package io.github.viimeinen1.ainventory.Plugin;

import io.github.viimeinen1.ainventory.Listeners.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class
 */
public class aInventory extends JavaPlugin {

    /**
     * Main plugin class
     */
    public aInventory() {}

    /**
     * On enable
     */
    @Override
    public void onEnable() {
        super.onEnable();
        InventoryListener.registerListener();
    }

    /**
     * On disable
     */
    @Override
    public void onDisable() {
        super.onDisable();
        InventoryListener.unregisterListener();
    }

}
