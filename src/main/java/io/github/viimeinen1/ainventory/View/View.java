package io.github.viimeinen1.ainventory.View;

import io.github.viimeinen1.ainventory.Animation.Animation;
import io.github.viimeinen1.ainventory.Interfaces.*;
import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;
import io.github.viimeinen1.ainventory.Slot.Slot;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Custom view of an inventory
 */
public class View implements InventoryHolder {

    /**
     * Group of slots with a context.
     */
    public static class SlotGroup {

        /**
         * Context of this group
         */
        public final String context;

        /**
         * Value of the context
         */
        public int value;

        /**
         * Slots in this group, and their position in the view.
         */
        public final HashMap<Integer, Slot> slots = new HashMap<>();

        /**
         * Create new SlotGroup. Default context value is 0.
         *
         * @param context context of this group
         */
        public SlotGroup(String context) {
            this.context = context;
        }

        /**
         * If this slot exists in this group.
         *
         * @param slot slot to compare to
         * @return if this slot is in this group
         */
        public boolean hasSlot(int slot) {
            return this.slots.containsKey(slot);
        }

        /**
         * Get the SubSlot that is associated with this context, and context value.
         *
         * @param slot slot
         * @return SubSlot
         */
        public @Nullable Slot.SubSlot getCurrent(int slot) {
            var s = this.slots.get(slot);
            if (s == null) return null;
            return s.slotMap.get(this.value);
        }
    }

    private final Set<UUID> viewers = new HashSet<>();

    private final org.bukkit.inventory.Inventory inventory;
    private final HashMap<String, SlotGroup> slotGroups;
    private final SlotGroup globalGroup = new SlotGroup(ContentBuilder.CONTEXT.GLOBAL);

    private final InventoryContent content;
    private final ItemClick onClick;
    private final InventoryRequirement requirement;
    private final InventoryOpen open;
    private final InventoryClose close;
    private final boolean whitelisted;
    private final Set<UUID> whitelist;
    private final HumanEntity player;

    private final HashMap<String, Animation> animations = new HashMap<>();

    private boolean reloadOnNextOpen = false;

    /**
     * Create new view
     *
     * @param size size of the view
     * @param content content of the view
     * @param onClick click action
     * @param title title of the inventory that is opened
     * @param requirement requirement for opening this view
     * @param open open action
     * @param close close action
     * @param whitelisted if this view is whitelisted
     * @param whitelist whitelisted players
     * @param player player associated with this view (the view will be built using this player)
     */
    public View(
        @NotNull AbstractInventory.SIZE size,
        @NotNull InventoryContent content,
        @Nullable ItemClick onClick,
        @NotNull Component title,
        @Nullable InventoryRequirement requirement,
        @Nullable InventoryOpen open,
        @Nullable InventoryClose close,
        boolean whitelisted,
        Set<UUID> whitelist,
        @Nullable HumanEntity player
    ) {
        this.content = content;
        this.onClick = onClick;
        this.requirement = requirement;
        this.open = open;
        this.close = close;
        this.whitelisted = whitelisted;
        this.whitelist = whitelist;
        this.slotGroups = new HashMap<>();
        this.player = player;

        this.inventory = Bukkit.createInventory(this, size.size(), title);

        reload();
    }

    /**
     * Get the inventory associated with this view.
     *
     * @return inventory
     */
    @Override
    public @NotNull org.bukkit.inventory.Inventory getInventory() {
        return inventory;
    }

    public void addAnimation(String id, Animation animation) {
        animations.put(id, animation);
    }

    public void runAnimation(String id) {
        var animation = animations.get(id);
        if (animation == null) return;
        animation.run(this.inventory, this::write);
    }

    /**
     * Get group with this identifier.
     * If no group exists, new group with this identifier is created.
     *
     * @param id identifier of the group
     * @return SlotGroup
     */
    private SlotGroup getGroup(String id) {
        if (ContentBuilder.CONTEXT.GLOBAL.equals(id)) return globalGroup;

        var group = this.slotGroups.get(id);
        if (group == null) {
            group = new SlotGroup(id);
            this.slotGroups.put(id, group);
        }
        return group;
    }

    /**
     * Get the SubSlot that is currently being displayed in this slot.
     *
     * @param slot slot
     * @return SubSlot, null if no SubSlot is displayed here
     */
    private @Nullable Slot.SubSlot getCurrent(int slot) {
        if (this.globalGroup.slots.containsKey(slot) && this.globalGroup.slots.get(slot).slotMap.containsKey(0)) {
            return globalGroup.slots.get(slot).slotMap.get(0);
        }

        for (SlotGroup group : this.slotGroups.values()) {
            if (group.hasSlot(slot)) {
                return group.getCurrent(slot);
            }
        }
        return null;
    }

    /**
     * Apply slots from SubSlot builder to this view.
     *
     * @param builder SubSlot builder
     */
    public void applySlots(Slot.SubSlot.Builder builder) {
        for (int slot : builder.slots) {
            if (slot < 0 || slot >= inventory.getSize()) continue;
            var group = this.getGroup(builder.context);
            if (!group.slots.containsKey(slot)) group.slots.put(slot, new Slot());
            group.slots.get(slot).slotMap.put(builder.contextValue, new Slot.SubSlot(builder));
        }
    }

    /**
     * Write current state to inventory
     */
    public void write() {
        write(this.inventory);
    }

    /**
     * Write current state to inventory.
     * Will call {@link View#update()} to update view to its viewers.
     *
     * @param inventory inventory to write to
     */
    public void write(org.bukkit.inventory.Inventory inventory) {
        for (var entry : this.globalGroup.slots.entrySet()) {
            entry.getValue().write(inventory, entry.getKey(), 0);
        }

        for (var group : this.slotGroups.values()) {
            for (var entry : group.slots.entrySet()) {
                entry.getValue().write(inventory, entry.getKey(), group.value);
            }
        }
        update();
    }

    /**
     * Update view to it's viewers
     */
    public void update() {
        this.inventory.getViewers().forEach(viewer -> {
            if (viewer instanceof Player pl) {
                pl.updateInventory();
            }
        });
    }

    /**
     * Open this view for player.
     * Will also trigger {@link View#write()}.
     *
     * @param player player
     */
    public void open(HumanEntity player) {
        if (reloadOnNextOpen) applyReload();
        else write();
        player.openInventory(this.inventory);
    }

    /**
     * Reload inventory completely.
     * Will delete all contents in the inventory that is player placed,
     * and re-run all builder functions.
     * <br><br>
     * Useful for updating inventory if builder functions give different results depending on a state.
     * <br><br>
     * If nobody is viewing the inventory, the reload will happen the next time someone opens the inventory.
     */
    public void reload() {
        if (this.inventory.getViewers().isEmpty()) {
            this.reloadOnNextOpen = true;
            return;
        }
        applyReload();
    }

    /**
     * Reload functionality
     */
    private void applyReload() {
        this.getInventory().clear();
        this.slotGroups.clear();
        this.reloadOnNextOpen = false;

        content.run(new View.ContentBuilder(
            this,
            ContentBuilder.CONTEXT.DEFAULT,
            0,
            this.player
        ));

        write();
    }

    /**
     * Set item to storage like player would have placed it.
     * Will display on top of specified item in the slot, and can be returned with returnOnClose().
     * The item will only be returned to the player with same uuid.
     *
     * @param slot slot to place item to
     * @param item item to place
     * @param uuid uuid of player who the item will be returned to
     */
    public void setStorage(int slot, @NotNull ItemStack item, @NotNull UUID uuid) {
        var subSlot = getCurrent(slot);
        if (subSlot == null) return;
        this.inventory.setItem(slot, item);
        subSlot.storage = uuid;
    }

    /**
     * Remove itemStack in storage from slot.
     *
     * @param slot slot
     */
    public void removeStorage(int slot) {
        var subSlot = getCurrent(slot);
        if (subSlot == null) return;
        subSlot.storage = null;
    }

    /**
     * Get context value. Default context is {@link ContentBuilder.CONTEXT#DEFAULT}.
     *
     * @param context context
     * @return value of the context
     */
    public int context(String context) {
        var group = this.slotGroups.get(context);
        return group.value;
    }

    /**
     * Set context value. Will also call {@link View#write()} to update the new state to the inventory.
     *
     * @param context context
     * @param contextValue new context value
     */
    public void context(String context, int contextValue) {
        var group = this.slotGroups.get(context);
        group.value = contextValue;
        write();
    }

    /**
     * Set next context value. Will also call {@link View#write()} to update the new state to the inventory.
     *
     * @param context context
     */
    public void next(String context) {
        var group = this.slotGroups.get(context);
        group.value++;
        write();
    }

    public void prev(String context) {
        var group = this.slotGroups.get(context);
        group.value--;
        write();
    }

    public boolean hasNext(String context) {
        var group = this.slotGroups.get(context);
        for (var slot : group.slots.values()) {
            if (slot.slotMap.containsKey(group.value + 1)) return true;
        }
        return false;
    }

    public boolean hasPrev(String context) {
        var group = this.slotGroups.get(context);
        for (var slot : group.slots.values()) {
            if (slot.slotMap.containsKey(group.value - 1)) return true;
        }
        return false;
    }

    /**
     * Handling opening this view. This should never be manually run.
     *
     * @param event InventoryOpenEvent
     */
    @ApiStatus.Internal
    public void onOpen(InventoryOpenEvent event) {
        if (whitelisted && whitelist.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
        else if (!(event.getPlayer() instanceof Player pl)) event.setCancelled(true);
        else if (requirement != null && !requirement.run(pl)) event.setCancelled(true);
        else if (this.viewers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true); // prevent reopening inventory if it wasn't closed
        else {
            this.viewers.add(event.getPlayer().getUniqueId());
            if (open == null) return;
            open.run(event);
        }
    }

    /**
     * Handling closing this view. This should never be manually run.
     *
     * @param event InventoryOpenEvent
     */
    @ApiStatus.Internal
    public void onClose(InventoryCloseEvent event) {
        // return slots with storage and return flag
        for (int i = 0; i < this.inventory.getSize(); i++) {
            var slot = getCurrent(i);
            if (slot == null) continue;
            if (!slot.returnOnClose) continue;
            if (slot.storage != null && !slot.storage.equals(event.getPlayer().getUniqueId())) continue;

            var itemInSlot = event.getInventory().getItem(i);
            if (itemInSlot == null) continue;

            slot.storage = null;
            this.inventory.clear(i);
            event.getPlayer().getInventory().addItem(itemInSlot)
                .values().forEach(leftover -> event.getPlayer().dropItem(leftover));
        }

        if (this.close != null) this.close.run(event);
        this.viewers.remove(event.getPlayer().getUniqueId());
    }

    /*
        checks:
        (0. whitelist -> no actions)
        1. requirement -> no actions
        2. prevent place -> no actions
        3. prevent take -> no actions
        4. prevent modification -> all actions
        5. all checks passed -> all actions
     */

    /**
     * Handling a click in this view. This should never be manually run.
     * @param event InventoryClickEvent
     */
    @ApiStatus.Internal
    public void onClick(InventoryClickEvent event) {

        // stop if event was canceled by some other plugin
        if (event.isCancelled()) return;

        // whitelist
        if (whitelisted && !whitelist.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(View.class), () -> event.getWhoClicked().closeInventory());
            JavaPlugin.getProvidingPlugin(View.class);
            return;
        }

        // default action
        if (this.onClick != null) this.onClick.run(event);

        var action = event.getAction();

        // unknown action
        if (action == InventoryAction.UNKNOWN) {
            event.setCancelled(true);
            return;
        }

        var slot = getCurrent(event.getSlot());

        // if click happened on the bottom inventory, not on the top one
        if (!Objects.equals(event.getClickedInventory(), event.getView().getTopInventory())) {
            onOtherInventoryClick(event);
            return;
        }

        // if there is no slot specified in the inventory
        if (slot == null) return;

        // handling actions
        switch (action) {

            // click on empty slot
            case NOTHING -> {
                if (slot.action != null) slot.action.run(event);
            }

            // placing
            case PLACE_ALL, PLACE_ONE, PLACE_SOME -> {
                // wrong kind of item
                if (slot.requirement != null && !slot.requirement.isAllowed(event.getCursor())) event.setCancelled(true);

                // no placing
                else if (slot.preventPlace) event.setCancelled(true);

                // no prevents
                else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    // if we placed an item, we set storage to true
                    if (!event.isCancelled()) slot.storage = event.getWhoClicked().getUniqueId();
                }
            }

            // picking up
            case PICKUP_ALL, PICKUP_ONE, PICKUP_SOME, PICKUP_HALF -> {
                // no taking
                if (slot.preventTake) event.setCancelled(true);

                // no prevents
                else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    // if we took an item, storage has to be false
                    if (!event.isCancelled()) slot.storage = null;
                }
            }

            // swapping (both place and pick up)
            case SWAP_WITH_CURSOR -> {

                if (slot.requirement != null && !slot.requirement.isAllowed(event.getCursor())) event.setCancelled(true);
                else if (slot.preventPlace) event.setCancelled(true);
                else if (slot.preventTake && slot.storage != null) event.setCancelled(true);

                    // phantom take; remove from cursor on next tick (if the event goes through)
                else if (slot.preventTake) {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    if (!event.isCancelled()) {
                        slot.storage = event.getWhoClicked().getUniqueId();
                        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(View.class), () -> event.getWhoClicked().setItemOnCursor(ItemStack.empty()));
                    }
                } else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    if (!event.isCancelled()) slot.storage = event.getWhoClicked().getUniqueId();
                }

            }

            // hotbar swap
            case HOTBAR_SWAP -> {
                if (slot.requirement != null) {
                    var replacement = event.getHotbarButton() == -1
                        ? event.getWhoClicked().getInventory().getItemInMainHand()
                        : event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                    if (replacement != null && !replacement.isEmpty() && !slot.requirement.isAllowed(replacement)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (slot.preventPlace) event.setCancelled(true);
                else if (slot.preventTake && slot.storage != null) event.setCancelled(true);

                // phantom remove; remove from slot on next tick (if the event goes through)
                else if (slot.preventTake) {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    if (!event.isCancelled()) {
                        slot.storage = event.getWhoClicked().getUniqueId();
                        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(View.class), () -> {
                            if (event.getHotbarButton() == -1) event.getWhoClicked().getInventory().setItemInOffHand(ItemStack.empty());
                            else event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), ItemStack.empty());
                        });
                    }
                } else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);
                    if (!event.isCancelled()) slot.storage = event.getWhoClicked().getUniqueId();
                }
            }

            // collect to cursor
            case COLLECT_TO_CURSOR -> onCollectToCursor(event, true);

            // drop
            case DROP_ALL_SLOT, DROP_ONE_SLOT -> {
                // no taking
                if (slot.preventTake) event.setCancelled(true);

                // no prevents
                else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    // if we took an item, storage has to be false
                    if (!event.isCancelled()) slot.storage = null;
                }
            }

            // clone
            case CLONE_STACK -> {
                if (slot.preventTake) event.setCancelled(true);
                else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);
                }
            }

            // move
            case MOVE_TO_OTHER_INVENTORY -> {
                // no taking
                if (slot.preventTake) event.setCancelled(true);

                    // no prevents
                else {
                    if (slot.preventModification) event.setCancelled(true);
                    if (slot.action != null) slot.action.run(event);

                    // if we took an item, storage has to be false
                    if (!event.isCancelled()) slot.storage = null;
                }
            }

            // bundle
            case PLACE_FROM_BUNDLE -> event.getWhoClicked().sendMessage("TODO: place from bundle (plugin inventory)");
            case PLACE_SOME_INTO_BUNDLE, PLACE_ALL_INTO_BUNDLE -> event.getWhoClicked().sendMessage("TODO: place into bundle (plugin inventory)");
            case PICKUP_FROM_BUNDLE -> event.getWhoClicked().sendMessage("TODO: pickup from bundle (plugin inventory)");
            case PICKUP_ALL_INTO_BUNDLE, PICKUP_SOME_INTO_BUNDLE -> event.getWhoClicked().sendMessage("TODO: pickup into bundle (plugin inventory)");

            // drop cursor
            case DROP_ALL_CURSOR, DROP_ONE_CURSOR -> {
                // just drop them if you want
            }
        }
    }

    private void onOtherInventoryClick(InventoryClickEvent event) {
        // check if we affect the other inventory in any way
        switch (event.getAction()) {
            case MOVE_TO_OTHER_INVENTORY -> onPluginInventoryItemTransfer(event);
            case COLLECT_TO_CURSOR -> onCollectToCursor(event, false);
            default -> {}
        }
    }

    /**
     * Recreates cursor collect
     * @param event click Event
     */
    private void onCollectToCursor(InventoryClickEvent event, boolean pluginInventory) {
        if (event.isCancelled()) return;
        if (event.getClickedInventory() == null) return;

        var collectItem = event.getCursor();
        if (collectItem.isEmpty()) return;
        if (collectItem.getAmount() == collectItem.getMaxStackSize()) return;

        var itemInSlot = event.getClickedInventory().getItem(event.getSlot());
        if (itemInSlot != null && !collectItem.isSimilar(itemInSlot)) return;
        if (itemInSlot != null && itemInSlot.getAmount() == itemInSlot.getMaxStackSize()) return;

        // onClick action
        if (pluginInventory) {
            if (this.onClick != null) this.onClick.run(event);
            if (event.isCancelled()) return;
        }

        // future cursor
        var cursorItem = collectItem.clone();

        // current slot
        cursorItem.setAmount(collectSlotAndGetAmount(cursorItem, itemInSlot, event, pluginInventory));

        if (cursorItem.getAmount() < cursorItem.getMaxStackSize()) {
            var itemList = new HashMap<Integer, ItemStack>();
            var slotList = new HashMap<Integer, Slot.SubSlot>();

            // get all slots
            var pluginInventoryContent = this.inventory.getContents();
            for (int i = 0; i < pluginInventoryContent.length; i++) {
                if (pluginInventory && i == event.getSlot()) continue;
                if (pluginInventoryContent[i] == null) continue;
                if (!collectItem.isSimilar(pluginInventoryContent[i])) continue;

                // custom slot functionality
                var slot = getCurrent(i);
                if (slot != null) {
                    if (slot.preventTake || slot.preventModification) continue;
                    slotList.put(i, slot);
                }

                itemList.put(i, pluginInventoryContent[i]);
            }
            var playerInventoryContent = event.getView().getBottomInventory().getContents();
            for (int i = 0; i < playerInventoryContent.length; i++) {
                if (playerInventoryContent[i] == null) continue;
                if (!collectItem.isSimilar(playerInventoryContent[i])) continue;

                if (i >= 9) itemList.put(i + 100, playerInventoryContent[i]); // 100 for normal slots
                else itemList.put(i + 200, playerInventoryContent[i]); // 200 for hotbar
            }

            int[] sortedSlots = itemList.keySet().stream().mapToInt(i -> i).sorted().toArray();

            // non-max-stack items
            for (int i : sortedSlots) {
                if (cursorItem.getAmount() >= cursorItem.getMaxStackSize()) {break;}
                var item = itemList.get(i);
                if (item.getAmount() == item.getMaxStackSize()) continue;

                var slot = slotList.get(i);
                if (slot != null) {
                    if (slot.action != null) {
                        slot.action.run(event);
                        if (event.isCancelled()) {
                            event.setCancelled(false);
                            continue;
                        }
                    }
                }

                int amount = item.getAmount() + cursorItem.getAmount();

                // partial take
                if (amount > cursorItem.getMaxStackSize()) {
                    cursorItem.setAmount(cursorItem.getMaxStackSize());
                    amount = amount - cursorItem.getMaxStackSize();

                // full take
                } else {
                    cursorItem.setAmount(cursorItem.getAmount() + item.getAmount());
                    amount = 0;
                }

                // hotbar
                if (i >= 200) {
                    if (amount == 0) event.getView().getBottomInventory().clear(i - 200);
                    else event.getView().getBottomInventory().setItem(i - 200, itemList.get(i).asQuantity(amount));
                }

                // player inventory
                else if (i >= 100) {
                    if (amount == 0) event.getView().getBottomInventory().clear(i - 100);
                    else event.getView().getBottomInventory().setItem(i - 100, itemList.get(i).asQuantity(amount));
                }

                // plugin inventory
                else {
                    if (slot != null && amount == 0) slot.storage = null;
                    if (amount == 0) this.inventory.clear(i);
                    else this.inventory.setItem(i, itemList.get(i).asQuantity(amount));
                }
            }

            // repeat for max-stacks
            if (cursorItem.getAmount() < cursorItem.getMaxStackSize()) {
                for (int i : sortedSlots) {
                    if (cursorItem.getAmount() >= cursorItem.getMaxStackSize()) {break;}
                    var item = itemList.get(i);
                    if (item.getAmount() != item.getMaxStackSize()) continue;

                    var slot = slotList.get(i);
                    if (slot != null) {
                        if (slot.action != null) {
                            slot.action.run(event);
                            if (event.isCancelled()) {
                                event.setCancelled(false);
                                continue;
                            }
                        }
                    }

                    int amount = item.getAmount() + cursorItem.getAmount();

                    // partial take
                    if (amount > cursorItem.getMaxStackSize()) {
                        cursorItem.setAmount(cursorItem.getMaxStackSize());
                        amount = amount - cursorItem.getMaxStackSize();

                        // full take
                    } else {
                        cursorItem.setAmount(cursorItem.getAmount() + item.getAmount());
                        amount = 0;
                    }

                    // hotbar
                    if (i >= 200) {
                        if (amount == 0) event.getView().getBottomInventory().clear(i - 200);
                        else event.getView().getBottomInventory().setItem(i - 200, itemList.get(i).asQuantity(amount));
                    }

                    // player inventory
                    else if (i >= 100) {
                        if (amount == 0) event.getView().getBottomInventory().clear(i - 100);
                        else event.getView().getBottomInventory().setItem(i - 100, itemList.get(i).asQuantity(amount));
                    }

                    // plugin inventory
                    else {
                        if (slot != null && amount == 0) slot.storage = null;
                        if (amount == 0) this.inventory.clear(i);
                        else this.inventory.setItem(i, itemList.get(i).asQuantity(amount));
                    }
                }
            }
        }

        event.getWhoClicked().setItemOnCursor(cursorItem);
        event.setCancelled(true);
        update();
    }

    private int collectSlotAndGetAmount(ItemStack cursorItem, ItemStack itemInSlot, InventoryClickEvent event, boolean pluginInventory) {
        if (itemInSlot == null) return cursorItem.getAmount();
        if (event.getClickedInventory() == null) return cursorItem.getAmount();

        // check if action is canceled (for this slot)
        if (pluginInventory) {
            var slot = getCurrent(event.getSlot());
            if (slot != null) {
                if (slot.preventTake || slot.preventModification) return cursorItem.getAmount();
                if (slot.action != null) {
                    slot.action.run(event);
                    if (event.isCancelled()) {
                        event.setCancelled(false);
                        return cursorItem.getAmount();
                    }
                }
            }
        }

        // apply modifications to slot
        int amount = cursorItem.getAmount() + itemInSlot.getAmount();
        if (amount > cursorItem.getMaxStackSize()) {
            int remain = Math.min(itemInSlot.getMaxStackSize(), amount - cursorItem.getMaxStackSize());
            event.getClickedInventory().setItem(event.getSlot(), itemInSlot.asQuantity(remain));
        } else {
            event.getClickedInventory().clear(event.getSlot());
            if (pluginInventory) {
                var slot = getCurrent(event.getSlot());
                if (slot != null) slot.storage = null;
            }
        }

        // return size of cursor
        return Math.min(amount, cursorItem.getMaxStackSize());
    }

    /**
     * Recreates moving items to plugin inventory for better control
     * @param event click event
     */
    private void onPluginInventoryItemTransfer(InventoryClickEvent event) {
        // return if something else canceled event
        if (event.isCancelled()) return;

        // isAllowed default click action
        if (this.onClick != null) this.onClick.run(event);
        if (event.isCancelled()) return;

        // recreate functionality
        ItemStack transferStack = event.getCurrentItem();
        if (transferStack == null) return;

        Inventory destinationInventory = event.getView().getTopInventory();

        var slotMap = new HashMap<Integer, Slot.SubSlot>();
        for (int i = 0; i < destinationInventory.getSize(); i++) {
            slotMap.put(i, getCurrent(i));
        }

        var applicableSlots = slotMap.entrySet().stream().filter(e -> {
            var slot = e.getValue();
            if (slot.requirement != null && !slot.requirement.isAllowed(transferStack)) return false;
            if (slot.preventPlace || slot.preventModification) return false;
            if (slot.action != null) {
                slot.action.run(event);
                if (event.isCancelled()) {
                    event.setCancelled(false);
                    return false;
                }
            }
            if (slot.storage != null) {
                var currentItem = destinationInventory.getItem(e.getKey());
                return currentItem != null && transferStack.isSimilar(currentItem) && currentItem.getAmount() < currentItem.getMaxStackSize();
            }
            return true;
        }).map(Map.Entry::getKey).toList();

        // move items
        int remain = transferStack.getAmount();
        for (var slot : applicableSlots) {
            if (remain <= 0) {break;}
            ItemStack curr = inventory.getItem(slot);
            int addition;
            if (curr == null || curr.isEmpty()) {
                addition = Math.min(inventory.getMaxStackSize(), remain);
                inventory.setItem(slot, transferStack.asQuantity(addition));
                if (slotMap.containsKey(slot)) slotMap.get(slot).storage = event.getWhoClicked().getUniqueId();
            } else {
                addition = Math.min(curr.getMaxStackSize() - curr.getAmount(), remain);
                curr.add(addition);
            }
            remain -= addition;
        }

        // remove item
        if (remain < 1) event.getView().getBottomInventory().clear(event.getSlot());
        else event.getView().getBottomInventory().setItem(event.getSlot(), transferStack.asQuantity(remain));

        event.setCancelled(true); // finally cancel event fo good
        update(); // update for viewers
    }

    /**
     * Handle dragging inside views. This should never be manually run.
     * @param event drag event
     */
    @ApiStatus.Internal
    public void onDrag(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);

        var slots = event.getRawSlots();
        var actualSlots = new HashSet<Integer>();

        // remove slots that can't be placed to
        for (var slot : slots) {
            var slotInv = event.getView().getInventory(slot);
            if (event.getView().getTopInventory().equals(slotInv)) {
                var subSlot = getCurrent(event.getView().convertSlot(slot));
                if (subSlot != null) {
                    if (subSlot.requirement != null && !subSlot.requirement.isAllowed(event.getOldCursor())) continue;
                    if (subSlot.preventPlace || subSlot.preventModification) continue;
                }
            }
            actualSlots.add(slot);
        }
        if (actualSlots.isEmpty()) return;

        var oldCursor = event.getOldCursor();
        int amountPerSlot = event.getType().equals(DragType.EVEN) ? oldCursor.getAmount() / actualSlots.size() : 1;
        int remain = oldCursor.getAmount() - amountPerSlot * actualSlots.size();
        for (var slot : actualSlots) {
            var slotInv = event.getView().getInventory(slot);
            if (slotInv != null) {
                int convertedSlot = event.getView().convertSlot(slot);
                var oldSlot = slotInv.getItem(convertedSlot);
                if (oldSlot != null && !oldSlot.isEmpty()) {
                    int amount = oldSlot.getAmount() + amountPerSlot;
                    if (amount > oldSlot.getMaxStackSize()) {
                        remain += amount - oldSlot.getMaxStackSize();
                        amount = oldSlot.getMaxStackSize();
                    }
                    slotInv.setItem(convertedSlot, oldSlot.asQuantity(amount));
                } else {
                    slotInv.setItem(event.getView().convertSlot(slot), oldCursor.asQuantity(amountPerSlot));
                }
            }
        }

        int finalRemain = remain;
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(View.class), () -> {
            if (finalRemain <= 0) event.getWhoClicked().setItemOnCursor(null);
            else event.getWhoClicked().setItemOnCursor(oldCursor.asQuantity(finalRemain));
        });
        update();
    }

    /**
     * Create new ContentBuilder.
     *
     * @param view view to place content to
     * @param context context to place the content to
     * @param contextValue value of the context to place the content to
     * @param player player
     */
    public record ContentBuilder(View view, String context, int contextValue, HumanEntity player) {

        public static class CONTEXT {

            /**
             * Default context when creating new inventory.
             */
            public static final String DEFAULT = "default";

            /**
             * Global context.
             * Will always be written from contextValue 0,
             * so items in this context will always be displayed.
             */
            public static final String GLOBAL = "global";
        }

        /**
         * Create new animation.
         *
         * @param id id of animation
         * @return animation builder
         */
        public Animation.Builder animation(@NotNull String id) {
            return new Animation.Builder(id, view);
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param context context builder for this list
         * @param pattern pattern of the slots
         * @param <T> type of values in this list
         */
        public <T> void patternList(@NotNull List<T> items, @NotNull ListBuildContext<T> context, String... pattern) {
            patternList(items, null, context, pattern);
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param comparator comparator for sorting the list
         * @param context context builder for this list
         * @param pattern pattern of the slots
         * @param <T> type of values in this list
         */
        public <T> void patternList(@NotNull List<T> items, @Nullable Comparator<T> comparator, @NotNull ListBuildContext<T> context, String... pattern) {
            list(items, comparator, context, getSlotsFromPattern(pattern));
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param context context builder for this list
         * @param row of slot
         * @param column of slot
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, int row, int column, @NotNull ListBuildContext<T> context) {
            list(items, context, new SlotCoordinate(row, column));
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param comparator comparator for sorting the list
         * @param context context builder for this list
         * @param row of slot
         * @param column of slot
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, @Nullable Comparator<T> comparator, int row, int column, @NotNull ListBuildContext<T> context) {
            list(items, comparator, context, new SlotCoordinate(row, column));
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param context context builder for this list
         * @param coordinate coordinate of slot
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, ListBuildContext<T> context, @NotNull SlotCoordinate coordinate) {
            list(items, null, context, coordinate.asSlot());
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param comparator comparator for sorting the list
         * @param context context builder for this list
         * @param coordinate coordinate of slot
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, @Nullable Comparator<T> comparator, ListBuildContext<T> context, @NotNull SlotCoordinate coordinate) {
            list(items, comparator, context, coordinate.asSlot());
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param context context builder for this list
         * @param slots slots to place this list to
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, @NotNull ListBuildContext<T> context, int... slots) {
            list(items, null, context, slots);
        }

        /**
         * Create new list.
         * @param items items in this list
         * @param comparator comparator for sorting the list
         * @param context context builder for this list
         * @param slots slots to place this list to
         * @param <T> type of values in this list
         */
        public <T> void list(@NotNull List<T> items, @Nullable Comparator<T> comparator, @NotNull ListBuildContext<T> context, int... slots) {
            if (slots.length == 0) return;

            // sort items
            var sorted = comparator == null ? items : items.stream().sorted(comparator).toList();

            int index = 0;
            int slotVal = 0;
            int contextVal = this.contextValue;
            for (T item : sorted) {
                if (slotVal > slots.length) {
                    slotVal = 0;
                    contextVal++;
                }
                int slot = slots[slotVal];
                slotVal++;

                context.run(new Slot.SubSlot.ValuedBuilder<>(this.view, item, this.context, contextVal, index, slot));
                index++;
            }
        }

        /**
         * create builder for slots in pattern.
         * <br><br>
         * For example:<br><code>
         * "#########"<br>
         * "#.......#"<br>
         * "#########"<br></code>
         * would create a border to a 3x9 inventory
         * <br><br>
         * if inventory is not correct size, the items will not get applied,
         * and an error will be displayed in console.
         * <br><br>
         * All other strings except for the item string will be empty slots.
         *
         * @param pattern pattern for placing items
         * @return builder
         */
        public Slot.SubSlot.Builder pattern(String... pattern) {
            return new Slot.SubSlot.Builder(this.view, this.context, this.contextValue, getSlotsFromPattern(pattern));
        }

        private int[] getSlotsFromPattern(String... pattern) {
            if (pattern.length * 9 != view.inventory.getSize()) return new int[0];
            for (String s : pattern) {
                if (s.length() != 9) return new int[0];
            }

            List<Integer> slotList = new ArrayList<>();
            int row = 0;
            for (String t : pattern) {
                int column = 0;
                for (String s : t.split("")) {
                    if (s.equals("#")) slotList.add(row * 9 + column);
                    column++;
                }
                row++;
            }

            int[] slots = new int[slotList.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = slotList.get(i);
            }

            return slots;
        }

        /**
         * Slot coordinate
         *
         * @param row row
         * @param column column
         */
        public record SlotCoordinate(int row, int column) {
            /**
             * Get this coordinate as a slot.
             * @return slot
             */
            public int asSlot() {
                return row * 9 + column;
            }

            /**
             * Create new coordinate from row and column
             * @param row row
             * @param column column
             * @return new SlotCoordinate
             */
            public static SlotCoordinate of(int row, int column) {
                return new SlotCoordinate(row, column);
            }
        }

        /**
         * Create a builder for slot in coordinates
         *
         * @param row row
         * @param column column
         * @return new SubSlot builder
         */
        public Slot.SubSlot.Builder setCoordinate(int row, int column) {
            return set(new SlotCoordinate(row, column));
        }

        /**
         * Create a builder for slot in coordinates
         *
         * @param coordinate coordinates of a slot
         * @return new SubSlot builder
         */
        public Slot.SubSlot.Builder set(@NotNull SlotCoordinate coordinate) {
                return set(coordinate.asSlot());
            }

        /**
         * Create a builder for slots.
         *
         * @param slots slots
         * @return new SubSlot builder
         */
        public Slot.SubSlot.Builder set(int... slots) {
            return new Slot.SubSlot.Builder(this.view, this.context, this.contextValue, slots);
        }

        /**
         * Create new ContentBuilder with different context value
         * @param contextValue new context value
         * @param content content
         */
        public void context(int contextValue, @NotNull InventoryContent content) {
            content.run(new ContentBuilder(view, this.context, contextValue, this.player));
        }

        /**
         * Create new ContentBuilder with different context and context value
         * @param context new context
         * @param contextValue new context value
         * @param content content
         */
        public void context(@NotNull String context, int contextValue, @NotNull InventoryContent content) {
            content.run(new ContentBuilder(view, context, contextValue, this.player));
        }


        /*
            Modules
         */

        /**
         * Create a border to slots.
         * <br><br>
         * Is shortened version of:
         * <pre>
         *  context.set(slots)
         *      .material(material)
         *      .setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build())
         *      .preventModification()
         *      .build();
         * </pre>
         *
         * @param material material of border
         * @param slots slots to place border in
         */
        @SuppressWarnings("UnstableApiUsage")
        public void border(@NotNull Material material, int... slots) {
            set(slots)
                .material(material)
                .setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build())
                .preventModification()
                .build();
        }

        /**
         * Create a border to a pattern.
         * <br><br>
         * use "#" to signal item, and any other character to signal no item.
         * <br><br>
         * Is shortened version of:
         * <pre>
         *  context.pattern(pattern)
         *      .material(material)
         *      .setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build())
         *      .preventModification()
         *      .build();
         * </pre>
         *
         * @param material material of border
         * @param pattern pattern the border is placed to
         */
        public void border(@NotNull Material material, String... pattern) {
            border(material, getSlotsFromPattern(pattern));
        }

        /**
         * Will display item created with nextPageContext if pageContext has items in next contextValue. If not, noNextPageContext will be displayed.
         * <br><br>
         * Item will be displayed in {@link CONTEXT#GLOBAL}, so it will be always visible.
         * <br><br>
         * Action <code>view.next(pageContext)</code> will be automatically added. If you want to create your own action, please remember to use {@link View#next(String)} again to make sure page is turned correctly.
         *
         * @param pageContext context to read from
         * @param nextPageContext next page item
         * @param noNextPageContext no next page item
         * @param slots slot(s) to place to
         */
        public void nextPage(@NotNull String pageContext, @NotNull BuildContext nextPageContext, @NotNull BuildContext noNextPageContext, int... slots) {
            if (this.view().hasNext(pageContext)) {
                nextPageContext.run(new Slot.SubSlot.Builder(this.view, CONTEXT.GLOBAL, 0, slots).action(_ -> this.view.next(pageContext)));
            } else {
                noNextPageContext.run(new Slot.SubSlot.Builder(this.view, CONTEXT.GLOBAL, 0, slots));
            }
        }

        /**
         * Will display item created with prevPageContext if pageContext has items in previous contextValue. If not, noPrevPageContext will be displayed.
         * <br><br>
         * Item will be displayed in {@link CONTEXT#GLOBAL}, so it will be always visible.
         * <br><br>
         * Action <code>view.prev(pageContext)</code> will be automatically added. If you want to create your own action, please remember to use {@link View#prev(String)} again to make sure page is turned correctly.
         *
         * @param pageContext context to read from
         * @param prevPageContext next page item
         * @param noPrevPageContext no next page item
         * @param slots slot(s) to place to
         */
        public void prevPage(@NotNull String pageContext, @NotNull BuildContext prevPageContext, @NotNull BuildContext noPrevPageContext, int... slots) {
            if (this.view().hasPrev(pageContext)) {
                prevPageContext.run(new Slot.SubSlot.Builder(this.view, CONTEXT.GLOBAL, 0, slots).action(_ -> this.view.prev(pageContext)));
            } else {
                noPrevPageContext.run(new Slot.SubSlot.Builder(this.view, CONTEXT.GLOBAL, 0, slots));
            }
        }

    }

}
