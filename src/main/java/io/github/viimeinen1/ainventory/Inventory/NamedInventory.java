package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.InventoryBuilder.NamedInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

/**
 * Inventory that has 'name' for identifying.
 */
public final class NamedInventory <T extends Enum<T>> extends AbstractInventory<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, NamedInventoryBuilder<T>, NamedInventory<T>> implements Named<T> {

    private final T name;
    public T name() {return name;}

    /**
     * Create new {@link AbstractInventory}.
     * 
     * Prefer {@link AbstractInventory.Builder#build()} to build new inventory.
     * 
     * @param builder extends {@link AbstractInventory.Builder}
     */
    public NamedInventory(NamedInventoryBuilder<T> builder) {
        super(builder);
        this.name = builder.name();
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

    @Override
    public NamedInventory<T> getThis() {return this;}

    public static <T extends Enum<T>> NamedInventoryBuilder<T> builder(T name) {
        return new NamedInventoryBuilder<T>(name);
    }

    // /**
    //  * Creating new inventory.
    //  * 
    //  * @param <T> Enum if inventories in GUI
    //  * @param inventoryEnum enum value of this inventory.
    //  * @return new {@link NamedInventory.Builder}
    //  */
    // public static <T extends Enum<T>> Builder<T> builder(@NotNull T inventoryEnum) {
    //     return new Builder<T>(inventoryEnum);
    // }

    // /**
    //  * Inventory builder for GUIs
    //  */
    // public static final class Builder <T extends Enum<?>> extends AbstractBuilder<NamedInventory<T>, Builder<T>> {

    //     final T inventoryEnum;

    //     /**
    //      * Create new builder
    //      * 
    //      * @param inventoryEnum enum value of this inventory
    //      */
    //     public Builder(@NotNull T inventoryEnum) {
    //         this.inventoryEnum = inventoryEnum;
    //     }

    //     /**
    //      * Get this builder.
    //      */
    //     @Override
    //     public Builder<T> getThis() {
    //         return this;
    //     }

    //     /**
    //      * Build new {@link NamedInventory} with the set details.
    //      * 
    //      * Will also initialize the inventory.
    //      * 
    //      * @return new {@link NamedInventory}
    //      */
    //     @Override
    //     public NamedInventory<T> build() {
    //         return new NamedInventory<>(this);
    //     }
    // }

    // @Override
    // public GUIItemBuilder<T> ItemBuilder(@NotNull int slot) {
    //     return new GUIItemBuilder<>(getThis(), slot);
    // }

    // @Override
    // public GUIItemBuilder<T> ItemBuilder(@NotNull Collection<Integer> slots) {
    //     return new GUIItemBuilder<>(getThis(), slots);
    // }

    // public static final class GUIItemBuilder <T extends Enum<?>> extends AbstractItemBuilder<NamedInventory<T>, GUIItemBuilder<T>> {

    //     /**
    //      * Modify single slot.
    //      * 
    //      * If slot already exists, the item will copy it's values.
    //      * 
    //      * Prefer {@link AbstractInventory#ItemBuilder(int)}
    //      * 
    //      * @param inventory Inventory that the item(s) will be set to.
    //      * @param slot slot number
    //      */
    //     public GUIItemBuilder(@NotNull NamedInventory<T> inventory, @NotNull int slot) {
    //         super(inventory, slot);
    //     }

    //     /**
    //      * Modify multiple slots.
    //      * 
    //      * All previous parameters will be taken from the first item in the list.
    //      * 
    //      * Prefer {@link AbstractInventory#ItemBuilder(Collection)}
    //      * 
    //      * @param inventory Inventory that the item(s) will be set to.
    //      * @param slots slot numbers
    //      */
    //     public GUIItemBuilder(@NotNull NamedInventory<T> inventory, @NotNull Collection<Integer> slots) {
    //         super(inventory, slots);
    //     }

    //     /**
    //      * Get the builder.
    //      */
    //     public GUIItemBuilder<T> getThis() {return this;}

    //     /**
    //      * Apply item(s) to linked inventory.
    //      * If not called, all changes to builder will be discarded.
    //      * 
    //      * @return {@link AbstractInventory} that the item(s) were set to.
    //      */
    //     public NamedInventory<T> build() {
    //         slots.forEach(slot -> {
    //             if (reloadFn != null) {
    //                 inventory.itemReloads.put(slot, reloadFn);
    //             } else if (removeReloadFunction) {
    //                 inventory.itemReloads.remove(slot);
    //             }
    //             if (slotFn != null) {
    //                     inventory.clickFunctions.put(slot, slotFn);
    //             } else if (removeSlotFuntion) {
    //                 inventory.clickFunctions.remove(slot);
    //             }
    //             inventory.inventory.setItem(slot, item);
    //         });
    //         return inventory;
    //     }

    // }

}
