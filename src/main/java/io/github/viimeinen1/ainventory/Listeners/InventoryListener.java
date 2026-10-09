package io.github.viimeinen1.ainventory.Listeners;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryListener implements Listener {

    /**
     * If this listener is initialized
     */
    public static boolean initialized = false;

    private static final InventoryListener instance = new InventoryListener();

    /**
     * Register listener for aInventory.
     * Without initializing the listener, the click functions will not work.
     * <br><br>
     * Will fail silently if this listener was already initialized
     */
    public static void registerListener() {
        if (initialized) return;
        var plugin = JavaPlugin.getProvidingPlugin(InventoryListener.class);
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        initialized = true;
    }

    public static void unregisterListener() {
        if (!initialized) return;
        HandlerList.unregisterAll(instance);
    }

    @EventHandler
    public static void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onOpen(event);
    }

    @EventHandler
    public static void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onClose(event);
    }

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onClick(event);
    }

    @EventHandler
    public static void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onDrag(event);
    }

}
