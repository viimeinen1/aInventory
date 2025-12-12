package io.github.viimeinen1.ainventory.InventoryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import io.github.viimeinen1.ainventory.Common.DataValue;
import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.INVENTORY_SIZE;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.inventoryCloseFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.inventoryFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.inventoryOpenFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.inventoryRequirementFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemClickFunction;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class AbstractInventoryBuilder <
        A extends AbstractItemBuilder<A, C>, 
        C extends AbstractInventoryView<A, C>, 
        E extends AbstractInventoryBuilder<A, C, E, F>, 
        F extends AbstractInventory<A, C, E, F>
    > {

    public UUID owner;
    public inventoryFunction<A, C> initialization;
    public Map<String, DataValue> values = new HashMap<>();
    public itemClickFunction defaultClickAction;
    public INVENTORY_SIZE size = INVENTORY_SIZE.CHEST_9x3;
    public Component title;
    public inventoryRequirementFunction requirementFunction;
    public inventoryOpenFunction openFunction;
    public inventoryCloseFunction closeFunction;
    public boolean disableDrag = true;

    // for subclasses to override
    public abstract E getThis();
    public abstract F build();

    /**
     * Set initialization function for the inventory.
     * 
     * The function can be called later with {@link AbstractInventory#initialize()}.
     * 
     * @param fn {@link inventoryFunction}
     * @return builder
     */
    public E initialization(inventoryFunction<A, C> fn) {
        this.initialization = fn;
        return getThis();
    }

    /**
     * Create new value to the inventory.
     * Values can be used to make pages.
     * 
     * Minimum value is 0.
     * 
     * @param key key.
     * @param max maxumum value (inclusive).
     * @return builder
     */
    public E value(String key, int max) {
        this.values.put(key, new DataValue(max));
        return getThis();
    }

    /**
     * Function that will be ran every time inventory is clicked.
     * 
     * @param fn {@link itemClickFunction}
     * @return builder
     */
    public E defaultAction(@NotNull itemClickFunction fn) {
        this.defaultClickAction = fn;
        return getThis();
    }

    /**
     * Inventory open function.
     * 
     * @param fn {@link inventoryOpenFunction}
     * @return builder
     */
    public E openFunction(inventoryOpenFunction fn) {
        this.openFunction = fn;
        return getThis();
    }

    /**
     * Inventory close function.
     * 
     * @param fn {@link inventoryCloseFunction}
     * @return builder
     */
    public E closeFunction(inventoryCloseFunction fn) {
        this.closeFunction = fn;
        return getThis();
    }

    /**
     * Set size of the inventory.
     * 
     * @param size {@link INVENTORY_SIZE}
     * @return builder
     */
    public E size(@NotNull INVENTORY_SIZE size) {
        this.size = size;
        return getThis();
    }

    /**
     * Set the title of the inventory.
     * 
     * Accepts both {@link String} and {@link Component}.
     * Giving object with other types will fail silently.
     * 
     * If {@link String}, the string will be deserialized as minimessage.
     * 
     * @param <S> {@link String} or {@link Component}
     * @param title title that will be set as the inventory title.
     * @return builder
     */
    public <S> E title(S title) {
        switch (title) {
            case Component component -> {
                this.title = component;
            }
            case String txt -> {
                this.title = MiniMessage.miniMessage().deserialize(txt);
            }
            default -> {}
        }
        return getThis();
    }

    /**
     * Set owner of the inventory.
     * 
     * Setting owner of the inventory will restrict it's usage to only it's owner.
     * 
     * @param owner {@link UUID} of the owner.
     * @return builder
     */
    public E owner(UUID owner) {
        this.owner = owner;
        return getThis();
    }

    /**
     * Function that is ran every time inventory is opened, or slot is clicked.
     * 
     * If function returns false, all futher execution is blocked.
     * 
     * @param fn {@link requirementFunction}
     * @return builder
     */
    public E require(inventoryRequirementFunction fn) {
        this.requirementFunction = fn;
        return getThis();
    }

    /**
     * If dragging items should be disabled in the inventory. Disabled by default.
     * 
     * @param disableDrag boolean if drag should be disabled.
     * @return builder
     */
    public E disableDrag(boolean disableDrag) {
        this.disableDrag = disableDrag;
        return getThis();
    }

}
