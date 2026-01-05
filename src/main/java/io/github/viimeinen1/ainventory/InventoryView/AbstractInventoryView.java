package io.github.viimeinen1.ainventory.InventoryView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.Common.DataValue;
import io.github.viimeinen1.ainventory.Common.IndexStream;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder.ItemSlotType;
import io.github.viimeinen1.ainventory.ItemBuilder.ItemBuildable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Built inventory that can't be modified anymore.
 * Can only be opened and reloaded.
 */
public abstract class AbstractInventoryView <A extends AbstractItemBuilder<A, C>, C extends AbstractInventoryView<A, C>> implements ItemBuildable<A, C>, InventoryHolder {

    /**
     * click function
     */
    @FunctionalInterface
    public static interface itemClickFunction {
        void run(InventoryClickEvent event);
    }

    /**
     * Inventory use requirement function
     */
    @FunctionalInterface
    public static interface inventoryRequirementFunction {
        boolean run(HumanEntity player);
    }

    /**
     * Inventory open function
     */
    @FunctionalInterface
    public static interface inventoryOpenFunction {
        void run(InventoryOpenEvent event);
    }

    /**
     * Inventory close function
     */
    @FunctionalInterface
    public static interface inventoryCloseFunction {
        void run(InventoryCloseEvent event);
    }

    /**
     * Single item reload function
     */
    @FunctionalInterface
    public static interface itemReloadFunction <A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {
        void run(A builder, Optional<HumanEntity> player);
    }

    @FunctionalInterface
    public static interface valuedItemFunction <T, A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {
        void run(ValuedItemBuilder<T, A, B> builder);

        public static class ValuedItemBuilder <T, A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {
            public final T value;
            public final A builder;
            public final int slot;
            public final Optional<HumanEntity> player;

            public ValuedItemBuilder(T value, A builder, int slot, Optional<HumanEntity> player) {
                this.value = value;
                this.builder = builder;
                this.slot = slot;
                this.player = player;
            }
        }
    }

    @FunctionalInterface
    public static interface itemRequirementFunction {
        public boolean run(ItemStack item);
    }

    /**
     * Generic inventory function.
     */
    @FunctionalInterface
    public static interface inventoryFunction <A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {
        void run(B aInventory, Optional<HumanEntity> player);
    }

    /**
     * Possible inventory sizes
     */
    public static enum INVENTORY_SIZE {
        CHEST_9x1(9),
        CHEST_9x2(18),
        CHEST_9x3(27),
        CHEST_9x4(36),
        CHEST_9x5(45),
        CHEST_9x6(54);

        private final int size;
        private INVENTORY_SIZE(int size) {
            this.size = size;
        }
        public int size() {return this.size;}
    }

    /**
     * Messages of inventory.
     */
    public final class Messages {
        public static final Component NO_OPEN_PERMISSION = MiniMessage.miniMessage().deserialize("<red>You don't have permission to open this inventory!");
        public static final Component NO_USE_PERMISSION = MiniMessage.miniMessage().deserialize("<red>You don't have permission to use this inventory!");
    }

    protected final Inventory inventory;
    public final Optional<inventoryFunction<A, C>> initFn;
    // protected final Map<Integer, itemClickFunction> clickFns = new HashMap<>();
    // protected final Map<Integer, itemReloadFunction<A, C>> itemReloads = new HashMap<>();
    protected final Map<Integer, ItemData<A, C>> itemData = new HashMap<>();
    public final Optional<itemClickFunction> defaultClickFn;
    public final Optional<inventoryRequirementFunction> requirementFn;
    public final Optional<inventoryOpenFunction> openFn;
    public final Optional<inventoryCloseFunction> closeFn;
    public final Optional<UUID> owner;
    public final boolean disableDrag;

    private final Map<String, DataValue> data = new HashMap<>();

    @Override
    public Map<Integer, ItemData<A, C>> itemData() {return itemData;}

    abstract A ItemBuilder(Integer slot);
    abstract A ItemBuilder(Collection<Integer> slots);
    abstract <T> void ValuedItemList(Collection<Integer> slots, Collection<T> values, String key, @Nullable HumanEntity player, valuedItemFunction<T, A, C> fn);
    protected abstract void initView(@NotNull Map<String, DataValue> data, @Nullable HumanEntity player);

    /**
     * Create new aInventoryView with all parameters.
     * 
     * All parameters are final, if change is required, create new view.
     * 
     * @param size
     * @param title
     * @param openFn
     * @param closeFn
     * @param requirementFn
     * @param defaultClickFn
     * @param owner
     * @param values
     * @param disableDrag
     */
    public AbstractInventoryView(
        @NotNull INVENTORY_SIZE size,
        @Nullable Component title,
        @Nullable inventoryFunction<A, C> initFn,
        @Nullable inventoryOpenFunction openFn,
        @Nullable inventoryCloseFunction closeFn,
        @Nullable inventoryRequirementFunction requirementFn,
        @Nullable itemClickFunction defaultClickFn,
        @Nullable UUID owner,
        @Nullable Map<String, DataValue> values,
        @NotNull boolean disableDrag
    ) {

        if (title != null) {
            this.inventory = Bukkit.createInventory(this, size.size, title);
        } else {
            this.inventory = Bukkit.createInventory(this, size.size);
        }

        this.initFn = Optional.ofNullable(initFn);
        this.openFn = Optional.ofNullable(openFn);
        this.closeFn = Optional.ofNullable(closeFn);
        this.requirementFn = Optional.ofNullable(requirementFn);
        this.defaultClickFn = Optional.ofNullable(defaultClickFn);
        this.owner = Optional.ofNullable(owner);
        this.disableDrag = disableDrag;

        if (values != null) {
            values.entrySet().forEach(entry -> {
                this.createValue(entry.getKey(), entry.getValue());
            });
        }
    }

    public void open(@NotNull HumanEntity player) {
        if (owner.isPresent() && !owner.get().equals(player.getUniqueId())) {
            player.sendMessage(Messages.NO_OPEN_PERMISSION);
            return;
        }
        if (requirementFn.isPresent() && !requirementFn.get().run(player)) {
            player.sendMessage(Messages.NO_OPEN_PERMISSION);
            return;
        }
        reload(player); // reload inventory
        player.openInventory(inventory);
    }

    public void reload() {
        itemData.forEach((slot, dt) -> {
            dt.reloadFn.ifPresent(fn -> fn.run(ItemBuilder(slot), Optional.empty()));
        });
        update();
    }

    public void reload(@Nullable HumanEntity player) {
        itemData.forEach((slot, dt) -> {
            dt.reloadFn.ifPresent(fn -> fn.run(ItemBuilder(slot), Optional.ofNullable(player)));
        });
        update();
    }

    public void reload(@NotNull Integer... slots) {
        reload(null, slots);
    }

    public void reload(@Nullable HumanEntity player, @NotNull Integer... slots) {
        for (Integer slot : slots) {
            if (!itemData.containsKey(slot)) {
                continue;
            }
            itemData.get(slot).reloadFn.ifPresent(fn -> fn.run(ItemBuilder(slot), Optional.ofNullable(player)));
        }
        update();
    }

    public void update() {
        inventory.getViewers().forEach(entity -> {
            if (entity instanceof Player pl) {
                pl.updateInventory();
            }
        });
    }

    public void clear() {
        inventory.clear();
        itemData.clear();
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(@Nullable HumanEntity player) {
        initView(data, player);
    }

    /**
     * Create new value with maximum value.
     * Will override previous value.
     * New value starts from 0.
     * 
     * Value has minimum value of 0 (inclusive), and maximum value of max (inclusive)
     * 
     * @param key key of value.
     * @param max maximum value of key (inclusive).
     */
    public void createValue(String key, int max) {
        this.data.put(key, new DataValue(max));
    }

    /**
     * Create new data value.
     * 
     * Will override previous value.
     * 
     * @param key key of value.
     * @param value {@link DataValue}
     */
    public void createValue(String key, DataValue value) {
        this.data.put(key, value);
    }

    /**
     * Get value with key.
     * 
     * Defaults to 0.
     * 
     * @param key key of value.
     * @return value.
     */
    public int value(String key) {
        return this.data.containsKey(key) ? this.data.get(key).value : 0;
    }

    /**
     * Get maximum value of key.
     * 
     * Defaults to 0.
     * 
     * @param key key of maximum value.
     * @return maximum value.
     */
    public int maxValue(String key) {
        return this.data.containsKey(key) ? this.data.get(key).max : 0;
    }

    /**
     * Set value associated with key. 
     * If key does not exist, or value is too small or too big, value will not be changed.
     * 
     * View will be initialized if value was set succesfully.
     * 
     * @param key
     * @param value
     * @param player
     */
    public void value(String key, Integer value, @Nullable HumanEntity player) {
        if (!this.data.containsKey(key)) {return;}
        this.data.get(key).set(value);
        initView(data, player);
    }

    /**
     * add 1 to value.
     * 
     * Will not go over max.
     * 
     * Inventory will be initialized if key exists.
     * 
     * @param key key of value.
     * @param player player for initializing inventory (can be null)
     */
    public void next(String key, @Nullable HumanEntity player) {
        if (!this.data.containsKey(key)) {return;}
        this.data.get(key).next();
        initView(data, player);
    }

    /**
     * Remove 1 from value.
     * 
     * Will not go under 0.
     * 
     * Inventory will be initialized if key exists.
     * 
     * @param key key of value.
     * @param player player for initializing inventory (can be null)
     */
    public void prev(String key, @Nullable HumanEntity player) {
        if (!this.data.containsKey(key)) {return;}
        this.data.get(key).prev();
        initView(data, player);
    }

    private static final Set<InventoryAction> PLACE_ACTIONS = Set.of(
        InventoryAction.HOTBAR_SWAP,
        InventoryAction.NOTHING,
        InventoryAction.PLACE_ALL,
        InventoryAction.PLACE_ALL_INTO_BUNDLE,
        InventoryAction.PLACE_FROM_BUNDLE,
        InventoryAction.PLACE_ONE,
        InventoryAction.PLACE_SOME,
        InventoryAction.PLACE_SOME_INTO_BUNDLE,
        InventoryAction.SWAP_WITH_CURSOR
    );

    @Internal
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // requirements
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        if (itemData.containsKey(event.getSlot())) {

            if (itemData.get(event.getSlot()).requirementFn.isPresent()) {

                if (
                    !event.getCursor().isEmpty() && 
                    !itemData.get(event.getSlot()).requirementFn.get().run(event.getCursor()) 
                ) {
                    event.setCancelled(true);
                    return;
                }
            }

            switch (itemData.get(event.getSlot()).slotType) {
                case BUTTON, BORDER -> {
                    event.setCancelled(true);
                }
                case RESULT -> {
                    if (PLACE_ACTIONS.contains(event.getAction())) {
                        event.setCancelled(true);
                        return;
                    }
                }
                case CONTAINER, CRAFTING, CUSTOM -> {}
            }

            defaultClickFn.ifPresent(fn -> fn.run(event));
            itemData.get(event.getSlot()).clickFn.ifPresent(fn -> fn.run(event));
            return;
        }

        // default action
        defaultClickFn.ifPresent(fn -> fn.run(event));
    }

    @Internal
    public void onInventoryTransfer(@NotNull InventoryClickEvent event) {
        // check for requirements
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        // cancel event for recreation
        event.setCancelled(true);

        // recreate functionality
        ItemStack item = event.getCurrentItem();
        Inventory dest = event.getView().getTopInventory();
        List<Integer> applicableSlots = new ArrayList<>();

        ItemStack[] contents = dest.getContents();
        List<ItemStack> containerItems = new ArrayList<>();

        // for some reason container is empty
        if (contents == null || item == null) {return;}

        for (ItemStack c : contents) {
            if (c == null) {
                containerItems.add(ItemStack.empty());
            } else {
                containerItems.add(c);
            }
        }

        var containerSlots = IndexStream.toStream(containerItems)
            .filter(val -> 
                itemData.containsKey(val.i) &&
                ( 
                    itemData.get(val.i).slotType.equals(ItemSlotType.CONTAINER) ||
                    itemData.get(val.i).slotType.equals(ItemSlotType.CRAFTING)
                ) &&
                (
                    itemData.get(val.i).requirementFn.isEmpty() ||
                    itemData.get(val.i).requirementFn.get().run(item) 
                )
            )
            .toList();

        // partial slots
        applicableSlots.addAll(containerSlots.stream()
            .filter(val -> 
                val.value.isSimilar(item) &&
                val.value.getAmount() < val.value.getMaxStackSize()
            )
            .map(val -> val.i)
            .toList());

        // empty slots
        applicableSlots.addAll(containerSlots.stream()
            .filter(val -> val.value.isEmpty())
            .map(val -> val.i)
            .toList());

        // move items
        int remain = item.getAmount();
        for (int slot : applicableSlots) {
            if (remain <= 0) {break;}

            ItemStack curr = inventory.getItem(slot);
            if (curr == null) {
                int addition = Math.min(inventory.getMaxStackSize(), remain);
                inventory.setItem(slot, item.asQuantity(addition));
                remain -= addition;
            } else {
                int addition = Math.min(curr.getMaxStackSize() - curr.getAmount(), remain);
                curr.add(addition);
                remain -= addition;
            }
        }

        // remove item
        if (remain < 1) {
            event.getView().getBottomInventory().clear(event.getSlot());
        } else {
            event.getView().getBottomInventory().setItem(event.getSlot(), item.asQuantity(remain));
        }

        update();
    }

    @Internal
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        if (this.disableDrag) {
            event.setCancelled(true);
        }
    }

    @Internal
    public void onInvenoryDoubleClick(@NotNull InventoryClickEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        event.setCancelled(true);
    }

    @Internal
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }
        openFn.ifPresent(fn -> fn.run(event));
    }

    @Internal
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getPlayer())) {return;} // run nothing if player doesn't have permission.

        // remove crafting slots
        ItemStack[] contents = event.getInventory().getContents();

        if (contents == null) {return;}

        List<ItemStack> containerItems = new ArrayList<>();
        for (ItemStack c : contents) {
            if (c == null) {
                containerItems.add(ItemStack.empty());
            } else {
                containerItems.add(c);
            }
        }

        IndexStream.toStream(containerItems)
            .filter(val -> 
                itemData.containsKey(val.i) &&
                (
                    itemData.get(val.i).slotType.equals(ItemSlotType.CRAFTING) ||
                    itemData.get(val.i).slotType.equals(ItemSlotType.RESULT)
                ) &&
                (
                    itemData.get(val.i).requirementFn.isEmpty() ||
                    itemData.get(val.i).requirementFn.get().run(val.value) 
                )
            )
            .forEach(val -> {
                event.getPlayer().getInventory().addItem(val.value)
                    .values().forEach(item -> event.getPlayer().dropItem(item));
                event.getInventory().clear(val.i);
            });

        closeFn.ifPresent(fn -> fn.run(event));
    }
}
