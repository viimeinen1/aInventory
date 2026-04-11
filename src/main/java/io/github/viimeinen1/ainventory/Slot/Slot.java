package io.github.viimeinen1.ainventory.Slot;

import io.github.viimeinen1.ainventory.Interfaces.ItemClick;
import io.github.viimeinen1.ainventory.Interfaces.ItemRequirement;
import io.github.viimeinen1.ainventory.View.View;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * Complete slot of View, including all subslots
 */
public class Slot {
    public final Map<Integer, SubSlot> slotMap = new HashMap<>();

    public void write(Inventory inventory, int slot, int value) {
        var subSlot = slotMap.get(value);
        if (subSlot == null) inventory.clear(slot);
        else subSlot.write(inventory, slot);
    }

    public static class SubSlot {
        public UUID storage; // if content shouldn't be cleared (something was placed to the slot)
        public ItemStack item;
        public final ItemClick action;
        public final ItemRequirement requirement;
        public final boolean preventModification;
        public final boolean returnOnClose;
        public final boolean preventPlace;
        public final boolean preventTake;

        public SubSlot(Builder builder) {
            this.item = builder.item;
            this.action = builder.action;
            this.requirement = builder.requirement;
            this.preventModification = builder.preventModification;
            this.returnOnClose = builder.returnOnClose;
            this.preventPlace = builder.preventPlace;
            this.preventTake = builder.preventTake;
        }

        public void clear(Inventory inventory, int slot) {
            if (storage != null) return;
            inventory.clear(slot);
        }

        public void write(Inventory inventory, int slot) {
            if (storage == null) inventory.setItem(slot, this.item);
        }

        public static class Builder {
            private final View view;
            public final String context;
            public final int value;
            public final int[] slots;
            private ItemStack item = ItemStack.empty();
            private ItemClick action = null; // click action
            private ItemRequirement requirement = null; // requirement
            private boolean preventModification = false; // should modification be prevented (button)
            private boolean returnOnClose = false; // should the stored item be returned to player on inventory close
            private boolean preventPlace = false; // should placing items to slot be prevented
            private boolean preventTake = false; // should taking items from slot be prevented

            public Builder(View view, String context, int value, int... slots) {
                this.view = view;
                this.context = context;
                this.value = value;
                this.slots = slots;
            }

            public Builder setItem(@NotNull ItemStack item) {
                this.item = item.clone();
                return this;
            }

            public Builder material(@NotNull Material material) {
                if (this.item.getAmount() == 0 || this.item.getType().equals(Material.AIR)) this.item = ItemStack.of(material);
                else this.item = this.item.withType(material);
                return this;
            }

            public Builder amount(int amount) {
                this.item.setAmount(amount);
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public Builder name(@NotNull Component name) {
                this.item.setData(DataComponentTypes.ITEM_NAME, name);
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public Builder name(@NotNull String name) {
                this.item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(name));
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public Builder lore(Component... lore) {
                var loreBuilder = ItemLore.lore();
                for (var line : lore) {
                    loreBuilder.addLine(Objects.requireNonNullElseGet(line, Component::empty));
                }
                item.setData(DataComponentTypes.LORE, loreBuilder.build());
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public Builder lore() {
                item.setData(DataComponentTypes.LORE, ItemLore.lore().build());
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public Builder lore(String... lore) {
                var loreBuilder = ItemLore.lore();
                for (var line : lore) {
                    if (line == null) loreBuilder.addLine(Component.empty());
                    else loreBuilder.addLine(MiniMessage.miniMessage().deserialize(line));
                }
                item.setData(DataComponentTypes.LORE, loreBuilder.build());
                return this;
            }

            @SuppressWarnings("UnstableApiUsage")
            public <T> Builder setData(@NotNull DataComponentType.Valued<T> type, @NotNull T value) {
                this.item.setData(type, value);
                return this;
            }

            public Builder action(@NotNull ItemClick action) {
                this.action = action;
                return this;
            }

            /**
             * Requirement function for this slot.
             * If false is returned, the action is canceled,
             * and click action won't be run.
             *
             * @param requirement requirement function
             * @return builder
             */
            public Builder require(ItemRequirement requirement) {
                this.requirement = requirement;
                return this;
            }

            public Builder preventModification() {
                this.preventModification = true;
                return this;
            }

            /**
             * If all modifications to this slot should be canceled.
             * Will still run click actions when clicked.
             *
             * @param preventModification boolean
             * @return builder
             */
            public Builder preventModification(boolean preventModification) {
                this.preventModification = preventModification;
                return this;
            }

            public Builder returnOnClose() {
                this.returnOnClose = true;
                return this;
            }

            /**
             * If content of this slot should be returned to the player on inventory close.
             * All items that have been placed by the player in question will be returned.
             *
             * @param returnOnClose boolean
             * @return builder
             */
            public Builder returnOnClose(boolean returnOnClose) {
                this.returnOnClose = returnOnClose;
                return this;
            }

            public Builder preventPlace() {
                this.preventPlace = true;
                return this;
            }

            /**
             * If placing item to this slot should be canceled.
             * Will still allow taking items from this slot!
             * <br><br>
             * click actions won't be run when trying to place an item to the slot.
             *
             * @param preventPlace boolean
             * @return builder
             */
            public Builder preventPlace(boolean preventPlace) {
                this.preventPlace = preventPlace;
                return this;
            }

            public Builder preventTake() {
                this.preventTake = true;
                return this;
            }

            /**
             * If taking item from this slot should be canceled.
             * Will still allow placing items to this slot!
             * <br><br>
             * click actions won't be run when trying to take an item from the slot.
             *
             * @param preventTake boolean
             * @return builder
             */
            public Builder preventTake(boolean preventTake) {
                this.preventTake = preventTake;
                return this;
            }

            public void build() {
                view.applySlots(this);
            }

        }

        public static class ValuedBuilder<T> extends Builder {
            public final T value;

            public ValuedBuilder(View view, T value, String context, int contextValue, int... slots) {
                super(view, context, contextValue, slots);
                this.value = value;
            }
        }
    }
}
