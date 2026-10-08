package io.github.viimeinen1.ainventory.Slot;

import io.github.viimeinen1.ainventory.Interfaces.DynamicBuildContext;
import io.github.viimeinen1.ainventory.Interfaces.ItemClick;
import io.github.viimeinen1.ainventory.Interfaces.ItemRequirement;
import io.github.viimeinen1.ainventory.View.View;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Complete slot of View, including all SubSlots
 */
public class Slot {

    /**
     * List of SubSlots in this slot
     */
    public final Map<Integer, SubSlot> slotMap = new HashMap<>();

    /**
     * Write an item to a slot in an inventory.
     *
     * @param inventory inventory to write to
     * @param slot slot to write to
     * @param value context value of what to write from
     */
    public void write(Inventory inventory, int slot, int value) {
        var subSlot = slotMap.get(value);
        if (subSlot == null) inventory.clear(slot);
        else subSlot.write(inventory, slot);
    }

    /**
     * SubSlot of a slot
     */
    public static class SubSlot {

        /**
         * UUID of player who has stored something into this slot
         * If nothing is stored, this is <code>null</code>
         */
        public UUID storage; // if content shouldn't be cleared (something was placed to the slot)

        /**
         * Item in this slot. This item is created with the builder, and written to the inventory.
         * This is different from storage item, which is displayed on top of this item if it exists.
         */
        public ItemStack item;

        /**
         * Action that is run when this slot is clicked
         */
        public final ItemClick action;

        /**
         * Requirement for storing items to this slot.
         */
        public final ItemRequirement requirement;

        /**
         * If this slot shouldn't be modified.
         */
        public final boolean preventModification;

        /**
         * If content in this slot should be returned to the player.
         * Only affects items placed by player, and items that are placed using {@link View#setStorage(int, ItemStack, UUID)}.
         */
        public final boolean returnOnClose;

        /**
         * If placing an item to this slot should be prevented.
         * Taking an item from this slot will still be permitted.
         */
        public final boolean preventPlace;

        /**
         * If taking an item from this slot should be prevented.
         * Placing an item to this slot will still be permitted.
         */
        public final boolean preventTake;

        /**
         * Should this slot be cleared on next write
         */
        public final boolean clearOnWrite;


        /**
         * Create a new SubSlot
         *
         * @param builder SubSlot builder
         */
        public SubSlot(Builder builder) {
            this.item = builder.item;
            this.action = builder.action;
            this.requirement = builder.requirement;
            this.preventModification = builder.preventModification;
            this.returnOnClose = builder.returnOnClose;
            this.preventPlace = builder.preventPlace;
            this.preventTake = builder.preventTake;
            this.clearOnWrite = false;
        }

        public SubSlot(DynamicBuilder dynamicBuilder) {
            this.item = dynamicBuilder.item;
            this.action = dynamicBuilder.action;
            this.requirement = dynamicBuilder.requirement;
            this.preventModification = dynamicBuilder.preventModification;
            this.returnOnClose = dynamicBuilder.returnOnClose;
            this.preventPlace = dynamicBuilder.preventPlace;
            this.preventTake = dynamicBuilder.preventTake;
            this.clearOnWrite = dynamicBuilder.clearOnWrite;
        }

        /**
         * Clear this slot if it doesn't have storage in it.
         * Slot will not get cleared if there is storage in it.
         *
         * @param inventory inventory to clear from
         * @param slot slot to clear
         */
        public void clear(Inventory inventory, int slot) {
            if (storage != null) return;
            inventory.clear(slot);
        }

        /**
         * Write item to this inventory.
         * If there is player placed content in this slot, nothing will happen.
         *
         * @param inventory inventory to write to
         * @param slot slot to write to
         */
        public void write(Inventory inventory, int slot) {
            if (storage == null) inventory.setItem(slot, this.item);
        }

        /**
         * SubSlot builder
         */
        public static abstract class AbstractBuilder <T extends AbstractBuilder<T>> {
            protected final View view;

            /**
             * Context this builder will be applied to.
             */
            public final String context;

            /**
             * Context value this builder will be applied to.
             */
            public final int contextValue;

            /**
             * Slots this builder will be applied to.
             */
            public final int[] slots;
            protected ItemStack item = ItemStack.empty();
            protected ItemClick action = null; // click action
            protected ItemRequirement requirement = null; // requirement
            protected boolean preventModification = false; // should modification be prevented (button)
            protected boolean returnOnClose = false; // should the stored item be returned to player on inventory close
            protected boolean preventPlace = false; // should placing items to slot be prevented
            protected boolean preventTake = false; // should taking items from slot be prevented

            protected abstract T self();

            /**
             * New SubSlot builder
             *
             * @param view view to apply this builder to
             * @param context context to apply this builder to
             * @param contextValue context value to apply this builder to
             * @param slots slots to apply this builder to
             */
            public AbstractBuilder(View view, String context, int contextValue, int... slots) {
                this.view = view;
                this.context = context;
                this.contextValue = contextValue;
                this.slots = slots;
            }

            /**
             * Set item to this slot.
             *
             * @param item item
             * @return this builder
             */
            public T setItem(@NotNull ItemStack item) {
                this.item = item.clone();
                return self();
            }

            /**
             * Set material of the item in this slot
             *
             * @param material material
             * @return this builder
             */
            public T material(@NotNull Material material) {
                if (this.item.getAmount() == 0 || this.item.getType().equals(Material.AIR)) this.item = ItemStack.of(material);
                else this.item = this.item.withType(material);
                return self();
            }

            /**
             * Set the amount of the item in this slot.
             * Default is 1.
             *
             * @param amount amount
             * @return this builder
             */
            public T amount(int amount) {
                this.item.setAmount(amount);
                return self();
            }

            /**
             * Set name of the item in this slot.
             * @param name name as component.
             * @return this builder
             */
            public T name(@NotNull Component name) {
                this.item.setData(DataComponentTypes.ITEM_NAME, name);
                return self();
            }

            /**
             * Set the name of the item in this slot.
             * Name will be parsed as <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage.</a>
             *
             * @param name name as text
             * @return this builder
             */
            public T name(@NotNull String name) {
                this.item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(name));
                return self();
            }

            /**
             * Set lore of the item in this slot
             *
             * @param lore lore as components
             * @return this builder
             */
            public T lore(ComponentLike... lore) {
                var loreBuilder = ItemLore.lore();
                for (var line : lore) {
                    loreBuilder.addLine(Objects.requireNonNullElseGet(line, Component::empty));
                }
                item.setData(DataComponentTypes.LORE, loreBuilder.build());
                return self();
            }

            /**
             * Remove lore of the item.
             *
             * @return this builder
             */
            public T lore() {
                item.setData(DataComponentTypes.LORE, ItemLore.lore().build());
                return self();
            }

            /**
             * Set lore of the item as text.
             * Text will be parsed using <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage.</a>
             *
             * @param lore lore as text
             * @return this builder
             */
            public T lore(String... lore) {
                var loreBuilder = ItemLore.lore();
                for (var line : lore) {
                    if (line == null) loreBuilder.addLine(Component.empty());
                    else loreBuilder.addLine(MiniMessage.miniMessage().deserialize(line));
                }
                item.setData(DataComponentTypes.LORE, loreBuilder.build());
                return self();
            }

            /**
             * Set lore of the item.
             * Will accept both {@link ComponentLike} and {@link String}. If other objects are given, they will be parsed using {@link Object#toString()}.
             * All non-components be parsed using <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage.</a>
             *
             * @param lore lore as text
             * @return this builder
             */
            public T lore(Object... lore) {
                var loreBuilder = ItemLore.lore();
                for (var line : lore) {
                    switch (line) {
                        case String str -> loreBuilder.addLine(MiniMessage.miniMessage().deserialize(str));
                        case ComponentLike com -> loreBuilder.addLine(com);
                        default -> loreBuilder.addLine(MiniMessage.miniMessage().deserialize(line.toString()));
                    }
                }
                item.setData(DataComponentTypes.LORE, loreBuilder.build());
                return self();
            }

            /**
             * Helper method for hiding the tooltip if the item.<br>
             * <br>
             * Executes <code>Builder#setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build())</code>.
             * @return this builder
             */
            public T hideTooltip() {
                return hideTooltip(true);
            }

            /**
             * Helper method for hiding the tooltip of the item.<br>
             * <br>
             * Executes <code>Builder#setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(boolean).build())</code>.
             *
             * @param hide if tooltip should be hidden
             * @return this builder
             */
            public T hideTooltip(boolean hide) {
                this.item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(hide).build());
                return self();
            }

            /**
             * Set data to item in this slot.
             * Works exactly like {@link ItemStack#setData(DataComponentType.Valued, Object)}
             * 
             * @param type type
             * @param value value
             * @return this builder
             * @param <K> type of data
             */
            public <K> T setData(@NotNull DataComponentType.Valued<K> type, @NotNull K value) {
                this.item.setData(type, value);
                return self();
            }

            /**
             * Action run when this slot is clicked
             *
             * @param action click action
             * @return this builder
             */
            public T action(@NotNull ItemClick action) {
                this.action = action;
                return self();
            }

            /**
             * If item should be allowed to be placed to this slot.
             * If false is returned, the action will be canceled.
             *
             * @param requirement requirement function
             * @return this builder
             */
            public T require(ItemRequirement requirement) {
                this.requirement = requirement;
                return self();
            }

            /**
             * Prevent modification of this slot.
             * Players will not be able to take or place items to this slot.
             *
             * @return this builder
             */
            public T preventModification() {
                this.preventModification = true;
                return self();
            }

            /**
             * If all modifications to this slot should be canceled.
             * Will still run click action if slot is clicked.
             *
             * @param preventModification boolean
             * @return this builder
             */
            public T preventModification(boolean preventModification) {
                this.preventModification = preventModification;
                return self();
            }

            /**
             * make player placed content be returned to the player.
             * Only affects items placed by player, and items that are placed using {@link View#setStorage(int, ItemStack, UUID)}.
             *
             * @return this builder
             */
            public T returnOnClose() {
                this.returnOnClose = true;
                return self();
            }

            /**
             * If content of this slot should be returned to the player on inventory close.
             * All items that have been placed by the player in question will be returned.
             *
             * @param returnOnClose boolean
             * @return builder
             */
            public T returnOnClose(boolean returnOnClose) {
                this.returnOnClose = returnOnClose;
                return self();
            }

            /**
             * Cancel all actions that try to place items to this slot.
             * Will still allow taking items from this slot.
             * <br><br>
             * Click action won't be run when trying to take items from this slot.
             *
             * @return builder
             */
            public T preventPlace() {
                this.preventPlace = true;
                return self();
            }

            /**
             * If placing item to this slot should be canceled.
             * Will still allow taking items from this slot.
             * <br><br>
             * if this is true, click action won't be run when trying to take items from this slot.
             *
             * @param preventPlace boolean
             * @return builder
             */
            public T preventPlace(boolean preventPlace) {
                this.preventPlace = preventPlace;
                return self();
            }

            /**
             * Cancel all actions that would result in taking an item out of this slot.
             * Will still allow placing items to this slot.
             * <br><br>
             * click actions won't be run when trying to take an item from the slot.
             *
             * @return builder
             */
            public T preventTake() {
                this.preventTake = true;
                return self();
            }

            /**
             * If taking item from this slot should be canceled.
             * Will still allow placing items to this slot.
             * <br><br>
             * if true, click actions won't be run when trying to take an item from the slot.
             *
             * @param preventTake boolean
             * @return builder
             */
            public T preventTake(boolean preventTake) {
                this.preventTake = preventTake;
                return self();
            }

            /**
             * Apply the slot to view.
             * <br><br>
             * If this is not called, this slot will not get added.
             */
            abstract void build();

        }

        public static class Builder extends AbstractBuilder<Builder> {

            @Override
            protected Builder self() {
                return this;
            }

            /**
             * New SubSlot builder
             *
             * @param view         view to apply this builder to
             * @param context      context to apply this builder to
             * @param contextValue context value to apply this builder to
             * @param slots        slots to apply this builder to
             */
            public Builder(View view, String context, int contextValue, int... slots) {
                super(view, context, contextValue, slots);
            }

            @Override
            public void build() {
                this.view.applySlots(this);
            }
        }

        /**
         * Valued slot builder
         *
         * @param <T> type of the value
         */
        public static class ValuedBuilder<T> extends Builder {

            /**
             * Value associated with this slot.
             */
            public final T value;
            public final int index;

            /**
             * Create new valued slot builder.
             *
             * @param view view to apply this builder to
             * @param value value this builder is associated with
             * @param context context this builder will be applied to
             * @param contextValue context value this builder will be applied to
             * @param slots slots this builder will be applied to
             */
            public ValuedBuilder(View view, T value, String context, int contextValue, int index, int... slots) {
                super(view, context, contextValue, slots);
                this.value = value;
                this.index = index;
            }
        }

        public static class DynamicBuilder extends AbstractBuilder<DynamicBuilder> {

            @Override
            protected DynamicBuilder self() {
                return this;
            }

            private boolean clearOnWrite = true;

            /**
             * New dynamic SubSlot builder
             *
             * @param view         view to apply this builder to
             * @param context      context to apply this builder to
             * @param contextValue context value to apply this builder to
             * @param slots        slots to apply this builder to
             */
            public DynamicBuilder(View view, String context, int contextValue, int... slots) {
                super(view, context, contextValue, slots);
            }

            /**
             * If this item should be cleared on next write.<br>
             * Default is true
             * @param clearOnWrite true if slot should be wiped on next clear.
             * @return this builder
             */
            public DynamicBuilder clearOnWrite(boolean clearOnWrite) {
                this.clearOnWrite = clearOnWrite;
                return this;
            }

            /**
             * Apply the slot to the view if condition is met. If not, run the other builder.
             *
             * @param condition condition.
             * @param orElse builder to use if condition is not met.
             */
            public void buildIfOrElse(boolean condition, DynamicBuildContext orElse) {
                if (!condition) orElse.run(new DynamicBuilder(this.view, this.context, this.contextValue, this.slots));
                else this.view.applySlots(this);
            }

            @Override
            public void build() {
                this.view.applySlots(this);
            }

        }
    }
}
