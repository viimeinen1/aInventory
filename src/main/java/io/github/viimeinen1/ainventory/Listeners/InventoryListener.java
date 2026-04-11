package io.github.viimeinen1.ainventory.Listeners;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class InventoryListener implements Listener {

    public static boolean initialized = false;

    /**
     * Initialize listener for aInventory.
     * Without initializing the listener, the click functions will not work.
     *
     * @param plugin {@link JavaPlugin} that the listener will be listed for.
     */
    public static void initializeListener(@NotNull JavaPlugin plugin) {
        if (initialized) return;
        plugin.getServer().getPluginManager().registerEvents(new InventoryListener(), plugin);
        initialized = true;
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
