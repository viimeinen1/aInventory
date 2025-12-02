package io.github.viimeinen1.ainventory.InventoryView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;
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
    public static interface requirementFunction {
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
    protected final Map<Integer, inventoryFunction<A, C>> pageInits;
    protected final Map<Integer, itemClickFunction> clickFns = new HashMap<>();
    protected final Map<Integer, itemReloadFunction<A, C>> itemReloads = new HashMap<>();
    public final Optional<itemClickFunction> defaultClickFn;
    public final Optional<requirementFunction> requirementFn;
    public final Optional<inventoryOpenFunction> openFn;
    public final Optional<inventoryCloseFunction> closeFn;
    public final Optional<UUID> owner;
    public final boolean disableDrag;

    private int page = 0;

    public Map<Integer, itemClickFunction> clickFns() {return clickFns;}
    public Map<Integer, itemReloadFunction<A, C>> itemReloads() {return itemReloads;}

    abstract A ItemBuilder(Integer slot);
    abstract A ItemBuilder(Collection<Integer> slots);
    abstract <T> void PagedItemList(Collection<Integer> slots, Collection<T> values, @Nullable HumanEntity player, valuedItemFunction<T, A, C> fn);
    protected abstract void initPage(Integer page, HumanEntity player);

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
     */
    public AbstractInventoryView(
        @NotNull INVENTORY_SIZE size,
        @Nullable Component title,
        @Nullable inventoryFunction<A, C> initFn,
        @Nullable inventoryOpenFunction openFn,
        @Nullable inventoryCloseFunction closeFn,
        @Nullable requirementFunction requirementFn,
        @Nullable itemClickFunction defaultClickFn,
        @Nullable UUID owner,
        @Nullable Map<Integer, inventoryFunction<A, C>> pageInits,
        @NotNull boolean disableDrag
    ) {

        if (title != null) {
            this.inventory = Bukkit.createInventory(this, size.size, title);
        } else {
            this.inventory = Bukkit.createInventory(this, size.size);
        }

        this.initFn = Optional.of(initFn);
        this.openFn = Optional.ofNullable(openFn);
        this.closeFn = Optional.ofNullable(closeFn);
        this.requirementFn = Optional.ofNullable(requirementFn);
        this.defaultClickFn = Optional.ofNullable(defaultClickFn);
        this.owner = Optional.ofNullable(owner);
        this.disableDrag = disableDrag;

        if (pageInits == null) {
            this.pageInits = new HashMap<>();
        } else {
            this.pageInits = pageInits;
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
        return;
    }

    public void reload() {
        itemReloads.forEach((slot, fn) -> {
            fn.run(ItemBuilder(slot), Optional.empty());
        });
        update();
    }

    public void reload(@Nullable HumanEntity player) {
        itemReloads.forEach((slot, fn) -> {
            fn.run(ItemBuilder(slot), Optional.ofNullable(player));
        });
        update();
    }

    public void reload(@NotNull Integer... slots) {
        reload(null, slots);
    }

    public void reload(@Nullable HumanEntity player, @NotNull Integer... slots) {
        for (Integer slot : slots) {
            if (!itemReloads.containsKey(slot)) {
                continue;
            }
            itemReloads.get(slot).run(ItemBuilder(slot), Optional.ofNullable(player));
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
        clickFns.clear();
        itemReloads.clear();
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(@Nullable HumanEntity player) {
        page(this.page, player);
    }

    public int page() {return this.page;}

    public void page(Integer page, @Nullable HumanEntity player) {
        initPage(page, player);
        this.page = page;
    }

    public void nextPage(@Nullable HumanEntity player) {
        if (!pageInits.containsKey(this.page + 1)) {
            return;
        }
        this.page++;
        initPage(this.page, player);
    }

    public void prevPage(@Nullable HumanEntity player) {
        if (!pageInits.containsKey(this.page - 1)) {
            return;
        }
        this.page--;
        initPage(this.page, player);
    }

    @Internal
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        defaultClickFn.ifPresent(fn -> fn.run(event));

        if (clickFns.containsKey(event.getSlot())) {
            clickFns.get(event.getSlot()).run(event);
        }
    }

    @Internal
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (requirementFn.isPresent() && !requirementFn.get().run(event.getWhoClicked())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Messages.NO_USE_PERMISSION);
            return;
        }

        if (disableDrag) {event.setCancelled(true);}
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
        closeFn.ifPresent(fn -> fn.run(event));
    }
}
