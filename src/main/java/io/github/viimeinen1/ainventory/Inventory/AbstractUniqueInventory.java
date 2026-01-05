package io.github.viimeinen1.ainventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.InventoryBuilder.AbstractUniqueInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryBuilder.UniqueInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

public abstract class AbstractUniqueInventory <K extends AbstractUniqueInventoryBuilder<K, T>, T extends AbstractUniqueInventory<K, T>> extends AbstractInventory<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, K, T> {

    public final Map<UUID, DefaultInventoryView> views = new HashMap<>();

    public AbstractUniqueInventory(K builder) {
        super(builder);
    }

    @Override
    public DefaultInventoryView createView() {
        return new DefaultInventoryView(
            builder.size,
            builder.title,
            builder.initialization,
            builder.openFunction,
            builder.closeFunction,
            builder.requirementFunction,
            builder.defaultClickAction,
            builder.owner,
            builder.values,
            builder.disableDrag
        );
    }

    /**
     * Initialize all open inventories.
     * 
     * Unique inventories are always initialized on open to make sure global artifacts are synced.
     */
    @Override
    public void initialize() {
        views.forEach((key, value) ->
            value.getInventory().getViewers().forEach(value::initialize)
        );
    }

    @Override
    public void initialize(@Nullable HumanEntity player) {
        if (player == null) {
            initialize();
            return;
        }

        // create new if missing
        if (!views.containsKey(player.getUniqueId())) {
            views.put(
                player.getUniqueId(), 
                createView()
            );
        }
        views.get(player.getUniqueId()).initialize(player);
    }

    @Override
    public void open(@NotNull HumanEntity player) {
        initialize(player); // always initialize inventory on open
        views.get(player.getUniqueId()).open(player);
    }

    /**
     * Reload all open inventories.
     * 
     * Inventories are always reloaded when they are opened.
     */
    @Override
    public void reload() {
        views.forEach((key, value) ->
            value.getInventory().getViewers().forEach(value::reload)
        );
    }

    @Override
    public void reload(@Nullable HumanEntity player) {
        if (player == null) {
            reload(); // reload all
            return;
        }

        if (!views.containsKey(player.getUniqueId())) {
            views.put(
                player.getUniqueId(), 
                createView()
            );
        }
        views.get(player.getUniqueId()).reload();
    }

    /**
     * Reload slots in all open inventories.
     * 
     * @param slots slots to reload.
     */
    @Override
    public void reload(Integer... slots) {
        views.forEach((key, value) -> value.getInventory().getViewers().forEach(pl ->
            value.reload(pl, slots))
        );
    }

    @Override
    public void reload(@Nullable HumanEntity player, Integer... slots) {
        if (player == null) {
            reload(slots); // reload all
            return;
        }

        if (!views.containsKey(player.getUniqueId())) {
            views.put(
                player.getUniqueId(), 
                createView()
            );
        }
        views.get(player.getUniqueId()).reload(slots);
    }

    public static UniqueInventoryBuilder builder() {
        return new UniqueInventoryBuilder();
    }


}
