package io.github.viimeinen1.ainventory.InventoryView;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class InventoryListener implements Listener {
    
    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || !(inv.getHolder(false) instanceof AbstractInventoryView<?,?> inventory)) {

            if (event.getView().getTopInventory().getHolder(false) instanceof AbstractInventoryView<?,?> transferInventory
            && event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                transferInventory.onInventoryTransfer(event);
            } else if (event.getView().getTopInventory().getHolder(false) instanceof AbstractInventoryView<?,?> transferInventory
            && event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                transferInventory.onInventoryDoubleClick(event);
            }
            return;
        }
        inventory.onInventoryClick(event);
    }

    @EventHandler
    public static void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        // return if we don't have this plugin's inv
        if (!(inv.getHolder(false) instanceof AbstractInventoryView<?,?> inventory)) {
            return;
        }
        inventory.onInventoryDrag(event);
    }

    @EventHandler
    public static void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder(false) instanceof AbstractInventoryView<?,?> inventory)) {return;}
        inventory.onInventoryOpen(event);
    }

    @EventHandler
    public static void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder(false) instanceof AbstractInventoryView<?,?> inventory)) {return;}
        inventory.onInventoryClose(event);
    }

    /**
     * Initialize listener for aInventory.
     * Without initializing the listener, the click functions will not work.
     * 
     * @param plugin {@link JavaPlugin} that the listener will be listed for.
     */
    public static void initializeListener(@NotNull JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new InventoryListener(), plugin);
    }

}
