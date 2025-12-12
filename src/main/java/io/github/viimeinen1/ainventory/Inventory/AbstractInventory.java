package io.github.viimeinen1.ainventory.Inventory;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.InventoryBuilder.AbstractInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;

// TODO: javadocs
// TODO: default builders (for exit, next page, last page, etc.)
// TODO: gui that accepts multiple types of inventories

/**
 * Custom inventory with addinational features like reloading individual slots.
 */
public abstract class AbstractInventory <A extends AbstractItemBuilder<A, C>, C extends AbstractInventoryView<A, C>, E extends AbstractInventoryBuilder<A, C, E, F>, F extends AbstractInventory<A, C, E, F>> {

    public final E builder;
    public final C view;

    public abstract F getThis();

    /**
     * Initialize view from scratch.
     * 
     * @return view extending {@link AbstractInventoryView}
     */
    public abstract C createView();

    /**
     * Create new {@link AbstractInventory}.
     * 
     * Prefer {@link AbstractInventory.Builder#build()} to build new inventory.
     * 
     * @param builder extends {@link AbstractInventory.Builder}
     */
    public AbstractInventory(E builder) {
        this.builder = builder;
        this.view = createView();
        this.view.initialize(null);
    }

    /**
     * Re-Initialize inventory.
     * 
     * Will:
     * - clear inventory.
     * - run initialization function again.
     * - update inventory to all it's viewers.
     */
    public void initialize() {
        initialize(null);
    }

    /**
     * Re-Initialize inventory.
     * 
     * Will:
     * - clear inventory.
     * - run initialization function again.
     * - update inventory to all it's viewers.
     */
    public void initialize(@Nullable HumanEntity player) {
        this.view.clear();
        this.view.initialize(player);
        this.view.update();
    }

    public void open(@NotNull HumanEntity player) {
        view.open(player);
    }

    public void reload() {
        view.reload();
    }

    public void reload(@Nullable HumanEntity player) {
        view.reload(player);
    }

    public void reload(Integer... slots) {
        reload(null, slots);
    }

    public void reload(@Nullable HumanEntity player, Integer... slots) {
        view.reload(player, slots);
    }

}
