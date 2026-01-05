package io.github.viimeinen1.ainventory.GUI;

import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.Common.InventoryProvider;
import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;
import io.github.viimeinen1.ainventory.InventoryBuilder.AbstractInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;

public abstract class AbstractGUI <
        T extends Enum<T>, 
        A extends AbstractItemBuilder<A, C>, 
        C extends AbstractInventoryView<A, C>, 
        E extends AbstractInventoryBuilder<A, C, E, F> & Named<T, ?>, 
        F extends AbstractInventory<A, C, E, F> & Named<T, ?>
    > implements InventoryProvider<F> {

    @FunctionalInterface
    public interface GUIInventoryGetter <T extends Enum<T>, A extends AbstractItemBuilder<A, C>, C extends AbstractInventoryView<A, C>, E extends AbstractInventoryBuilder<A, C, E, F> & Named<T, ?>, F extends AbstractInventory<A, C, E, F> & Named<T, ?>> {
        F build(E builder);
    }

    public final Map<T, F> inventories;
    protected abstract E builder(T name);

    /**
     * create new GUI.
     * 
     * @param enumClass enum with all possible inventories in the GUI
     */
    public AbstractGUI(@NotNull Class<T> enumClass) {
        inventories = new EnumMap<>(enumClass);
    }

    /**
     * Create inventory to the gui.
     * 
     * @param name name of the inventory (as enum)
     * @param fn {@link GUIInventoryGetter} getter for inventory
     * @return the inventory that was added.
     */
    public @NotNull F create(@NotNull T name, @NotNull GUIInventoryGetter<T, A, C, E, F> fn) {
        F inventory = fn.build(builder(name));
        inventories.put(inventory.name(), inventory);
        return inventory;
    }

    /**
     * Put inventory into the gui.
     * 
     * @param inventory inventory
     * @return inventory that was put into the gui.
     */
    @Override
    public @NotNull F put(@NotNull F inventory) {
        inventories.put(inventory.name(), inventory);
        return inventory;
    }

    /**
     * Open inventory for player.
     * 
     * @param name Enum value of the inventory.
     * @param player Player who the inventory will be opened to.
     * @throws NoSuchElementException if the inventory has not been set.
     */
    public void open(@NotNull T name, @NotNull HumanEntity player) throws NoSuchElementException {
        if (!inventories.containsKey(name)) {
            throw new NoSuchElementException("This inventory has not been set.");
        }
        inventories.get(name).open(player);
    }

    /**
     * Get inventory from GUI.
     * 
     * @param name name of the inventory.
     * @return Optional with the inventory.
     */
    public Optional<F> get(@NotNull T name) {
        return Optional.ofNullable(inventories.get(name));
    }

    public void initialize(@NotNull T name) {
        initialize(name, null);
    }

    public void initialize(@NotNull T name, @Nullable HumanEntity player) {
        get(name).ifPresent(inv -> inv.initialize(player));
    }

    public void reload(@NotNull T name) {
        get(name).ifPresent(AbstractInventory::reload);
    }

    public void reload(@NotNull T name, @Nullable HumanEntity player) {
        get(name).ifPresent(inv -> inv.reload(player));
    }

    public void reload(@NotNull T name, Integer... slots) {
        reload(name, null, slots);
    }

    public void reload(@NotNull T name, @Nullable HumanEntity player, Integer... slots) {
        get(name).ifPresent(inv -> inv.reload(player, slots));
    }

}
