package io.github.viimeinen1.ainventory.ItemBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.InventoryView.ItemData;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemClickFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemReloadFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemRequirementFunction;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Abstract item builder.
 */
public abstract class AbstractItemBuilder <A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {

    /**
     * Different types of slots.
     */
    public enum ItemSlotType {
        BUTTON,
        CONTAINER,
        CRAFTING,
        BORDER,
        RESULT,
        CUSTOM
    }

    B inventory;
    itemClickFunction slotFn;
    ItemStack item;
    Set<Integer> slots = new HashSet<>();
    itemReloadFunction<A, B> reloadFn;
    ItemSlotType slotType = ItemSlotType.CUSTOM;
    itemRequirementFunction requirementFn;

    // for subclasses to override
    public abstract A getThis();

    public B build() {
        slots.forEach(slot -> {
            inventory.itemData().put(slot, new ItemData<>(slotFn, reloadFn, requirementFn, slotType));
            inventory.getInventory().setItem(slot, item);
        });
        return inventory;
    }

    /**
     * Modify single slot.
     * 
     * If slot already exists, the item will copy its values.
     * 
     * @param inventory Inventory that the item(s) will be set to.
     * @param slot slot number
     */
    public AbstractItemBuilder(@NotNull B inventory, int slot) {
        this.inventory = inventory;
        this.slots.add(slot);
        var data = inventory.itemData().get(slot);
        if (data != null) {
            this.reloadFn = data.reloadFn().orElse(null);
            this.slotFn = data.clickFn().orElse(null);
            this.slotType = data.slotType();
        }
        this.item = inventory.getInventory().getItem(slot);
        if (this.item == null) {
            this.item = ItemStack.of(Material.AIR);
        } else {
            this.item = this.item.clone();
        }
    }

    /**
     * Modify multiple slots.
     * 
     * All previous parameters will be taken from the first item in the list.
     *
     * @param inventory Inventory that the item(s) will be set to.
     * @param slots slot numbers
     */
    public AbstractItemBuilder(@NotNull B inventory, @NotNull Collection<Integer> slots) {
        this.inventory = inventory;
        this.slots.addAll(slots);
        Integer first = slots.iterator().next();
        var data = inventory.itemData().get(first);
        if (data != null) {
            this.reloadFn = data.reloadFn().orElse(null);
            this.slotFn = data.clickFn().orElse(null);
            this.slotType = data.slotType();
        }
        this.item = inventory.getInventory().getItem(first);
        if (this.item == null) {
            this.item = ItemStack.of(Material.AIR);
        } else {
            this.item = this.item.clone();
        }
    }

    /**
     * Add slot that the item(s) will be copied to.
     * 
     * @param slot slot number
     * @return builder
     */
    public A addSlot(@NotNull Integer slot) {
        this.slots.add(slot);
        return getThis();
    }

    /**
     * Add multiple slots that this item(s) will be copied to.
     * 
     * @param slots slot numbers
     * @return builder
     */
    public A addSlot(@NotNull Collection<Integer> slots) {
        this.slots.addAll(slots);
        return getThis();
    }

    /**
     * Replace item in the builder.
     * 
     * This will discard all item related values added before.
     * 
     * @param item {@link ItemStack}
     * @return builder
     */
    public A setItem(@NotNull ItemStack item) {
        this.item = item.clone();
        return getThis();
    }

    /**
     * Get {@link ItemBuildable} the item(s) will be set to.
     * 
     * @return the {@link ItemBuildable} the items will be set to.
     */
    public B inventory() {
        return inventory;
    }

        /**
         * Get slots that item(s) will be copied to.
        * 
        * @return {@link List} of slot numbers
        */
    public Set<Integer> getSlots() {
        return slots;
    }

    /**
     * Set the type of the item(s).
     * 
     * @param material new {@link Material} for item(s).
     * @return builder
     */
    public A material(@NotNull Material material) {
        if (this.item == null || this.item.getType().equals(Material.AIR)) {
            this.item = ItemStack.of(material);
        } else {
            this.item = this.item.withType(material);
        }
        return getThis();
    }

    /**
     * Set amount for item(s).
     * 
     * @param amount amount for item(s).
     * @return builder
     */
    public A amount(@NotNull Integer amount) {
        this.item.setAmount(amount);
        return getThis();
    }

    /**
     * Clear item(s) completely.
     * Material will be set to AIR.
     * 
     * @return builder
     */
    public A clear() {
        item = ItemStack.of(Material.AIR);
        slotFn = null;
        reloadFn = null;
        return getThis();
    }

    /**
     * Reload function.
     * The function will be called every time inventory is opened.
     *
     * @param fn reload function
     * @return builder
     */
    public A reload(@Nullable itemReloadFunction<A, B> fn) {
        this.reloadFn = fn;
        return getThis();
    }

    /**
     * Set displayname of item(s).
     * Using null will retain old displayname.
     * 
     * Accepts both {@link String} and {@link Component}.
     * Giving object with other types will fail silently.
     * 
     * If {@link String}, the string will be deserialized as minimessage.
     * 
     * @param <R> {@link String} or {@link Component}
     * @param name displayname of item(s)
     * @param italic if name should be italic (default true)
     * @return builder
     */
    @SuppressWarnings("UnstableApiUsage")
    public <R> A name(@Nullable R name, boolean italic) {
        switch (name) {
            case null -> {return getThis();}
            case String str -> item.setData(DataComponentTypes.CUSTOM_NAME, MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, italic));
            case Component component -> item.setData(DataComponentTypes.CUSTOM_NAME, component.decoration(TextDecoration.ITALIC, italic));
            default -> {}
        }
        return getThis();
    }

    /**
     * Set lore of item(s).
     * Using null will do nothing.
     * 
     * Accepts both {@link String} and {@link Component}.
     * If other types are used, those lines will be skipped.
     * 
     * @param <R> {@link String} or {@link Component}
     * @param loreLines lines
     * @param italic if lore should be italic
     * @return builder
     */
    @SuppressWarnings("UnstableApiUsage")
    public <R> A lore(@Nullable Collection<R> loreLines, boolean italic) {
        if (loreLines == null) {return getThis();}
        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreLines.forEach(line -> {
            switch (line) {
                case null -> loreBuilder.addLine(Component.empty());
                case String str ->loreBuilder.addLine(MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, italic));
                case Component component -> loreBuilder.addLine(component.decoration(TextDecoration.ITALIC, italic));
                default -> {
                }
            }
        });
        item.setData(DataComponentTypes.LORE, loreBuilder.build());
        return getThis();
    }

    /**
     * Function that is run when item(s) is clicked.
     *
     * @param slotFn {@link itemClickFunction}
     * @return builder
     */
    public A function(@Nullable itemClickFunction slotFn) {
        this.slotFn = slotFn;
        return getThis();
    }

    /**
     * Set data component to item(s).
     * 
     * @param type component type
     * @param value component value
     * @return builder
     */
    @SuppressWarnings("UnstableApiUsage")
    public <R> A setData(@NotNull DataComponentType.Valued<R> type, @NotNull R value) {
        item.setData(type, value);
        return getThis();
    }

    /**
     * Set slot type.
     * 
     * types:
     * - BUTTON: only clicking allowed, all modifications disabled
     * - CONTAINER: placing and taking is allowed
     * - CRAFTING: same as container, except that the slot is returned back to player inventory on inventory close.
     * - BORDER: everything disabled
     * - RESULT: only taking from slot is allowed. Is placed to player inventory if inventory is closed.
     * - CUSTOM: custom behavior (default)
     * 
     * @param type type of slot
     * @return builder
     */
    public A slotType(ItemSlotType type) {
        this.slotType = type;
        return getThis();
    }

    /**
     * Limit items that can be placed to this slot.
     * 
     * @param fn requirement function (true if item can be placed to this slot)
     * @return builder
     */
    public A require(itemRequirementFunction fn) {
        this.requirementFn = fn;
        return getThis();
    }

}
