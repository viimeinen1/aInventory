package io.github.viimeinen1.ainventory.Listeners;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Houses all listeners of this (plugin) library
 */
public class InventoryListener implements Listener {

    /**
     * If this listener is initialized
     */
    public static boolean initialized = false;

    private static final InventoryListener instance = new InventoryListener();

    /**
     * Houses all listeners of this (plugin) library
     */
    public InventoryListener() {}

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

    /**
     * Unregister this listener. Will render all existing inventories built with this instance of library useless.
     */
    public static void unregisterListener() {
        if (!initialized) return;
        HandlerList.unregisterAll(instance);
    }

    /**
     * Inventory open event. Event will be passed to the respective view that triggers this event.
     * @param event event
     */
    @EventHandler
    public static void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onOpen(event);
    }

    /**
     * Inventory close event. Event will be passed to the respective view that triggers this event.
     * @param event event
     */
    @EventHandler
    public static void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onClose(event);
    }

    /**
     * Inventory click event. Event will be passed to the respective view that triggers this event.
     * @param event event
     */
    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onClick(event);
    }

    /**
     * Inventory drag event. Event will be passed to the respective view that triggers this event.
     * @param event event
     */
    @EventHandler
    public static void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (!(inv.getHolder(false) instanceof View view)) {return;}
        view.onDrag(event);
    }

    /**
     * Player quit event. Handles deleting views of players that have left.
     * @param event event
     */
    @EventHandler
    public static void onPlayerLeave(PlayerQuitEvent event) {
        // delete async to avoid any lag caused by dubious amounts of views.
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(InventoryListener.class), () -> View.Storage.deletePlayer(event.getPlayer().getUniqueId()));
    }

}
