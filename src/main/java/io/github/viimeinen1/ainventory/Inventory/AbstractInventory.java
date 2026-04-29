package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.Interfaces.*;
import io.github.viimeinen1.ainventory.Listeners.InventoryListener;
import io.github.viimeinen1.ainventory.View.View;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *  Is Basic inventory, creates Views when opening inventory
 */
public abstract class AbstractInventory {

    /**
     * Inventory size
     */
    public enum SIZE {
        CHEST_9x1(9),
        CHEST_9x2(18),
        CHEST_9x3(27),
        CHEST_9x4(36),
        CHEST_9x5(45),
        CHEST_9x6(54);

        private final int size;
        SIZE(int size) {
            this.size = size;
        }

        /**
         * @return size of this type of inventory
         */
        public int size() {
            return this.size;
        }
    }

    /**
     * Builder for this inventory
     */
    protected final Builder inventoryBuilder;

    /**
     * Creates new abstract inventory
     *
     * @param inventoryBuilder inventory builder
     */
    public AbstractInventory(Builder inventoryBuilder) {
        var plugin = JavaPlugin.getProvidingPlugin(AbstractInventory.class);
        InventoryListener.initializeListener(plugin);
        this.inventoryBuilder = inventoryBuilder;
    }

    /**
     * @param player player (or null)
     * @return view associated with this player, or default inventory if null
     */
    public abstract @NotNull View getView(@Nullable HumanEntity player);

    /**
     * Open a view to this inventory for player.
     *
     * @param player player
     */
    public void open(@NotNull HumanEntity player) {
        getView(player).open(player);
    }

    /**
     * Get new builder for inventory
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     *  Inventory builder.
     */
    public static class Builder {
        protected SIZE size; // size of inventory
        protected InventoryContent content; // content creation function
        protected ItemClick onClick; // default action
        protected Component title; // title of inventory
        protected InventoryRequirement requirement; // requirement to open
        protected InventoryOpen open; // open action
        protected InventoryClose close; // close action
        protected boolean whitelisted = false; // if only whitelisted players can access inventory
        protected Set<UUID> whitelist = new HashSet<>(); // whitelisted players

        /**
         * What happens when any slot in inventory is clicked, or any slot is modified.
         * <br><br>
         * If you want to block all modifications to inventory, it's recommended to use <code>builder.onClick(event -> event.setCancelled(true));</code>
         *
         * @param onClick action that happens when inventory is modified by player
         * @return this builder
         */
        public Builder onClick(@Nullable ItemClick onClick) {
            this.onClick = onClick;
            return this;
        }

        /**
         * @param fn action that is executed when inventory is opened
         * @return this builder
         */
        public Builder open(@Nullable InventoryOpen fn) {
            this.open = fn;
            return this;
        }

        /**
         * @param fn action that is executed when inventory is closed
         * @return this builder
         */
        public Builder close(@Nullable InventoryClose fn) {
            this.close = fn;
            return this;
        }

        /**
         * Set the size of this inventory
         *
         * @param size {@link SIZE} of inventory
         * @return this builder
         */
        public Builder size(@Nullable SIZE size) {
            this.size = size;
            return this;
        }

        /**
         * Set the title of this inventory
         *
         * @param title title as component
         * @return this builder
         */
        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        /**
         * Set the title of this inventory. The text will be deserialized as <a href="https://docs.papermc.io/adventure/minimessage/format/">MiniMessage.</a>
         *
         * @param title title as text
         * @return this builder
         */
        public Builder title(String title) {
            this.title = MiniMessage.miniMessage().deserialize(title);
            return this;
        }

        /**
         * If this inventory should be whitelisted, and only players in whitelist are allowed to access (and use) the inventory.
         *
         * @param whitelisted true if inventory should be whitelisted
         * @return this builder
         */
        public Builder whitelisted(boolean whitelisted) {
            this.whitelisted = whitelisted;
            return this;
        }

        /**
         * Add players to whitelist. Whitelist uses UUIDs to track players.
         *
         * @param uuid UUID(s) of players to add to whitelist
         * @return this builder
         */
        public Builder whitelist(UUID... uuid) {
            this.whitelist.addAll(Arrays.asList(uuid));
            return this;
        }

        /**
         * Requirement to use this inventory.
         * <br><br>
         * Players will be able to access this inventory only if they pass this requirement. If not set, the inventory will let everyone access it.
         *
         * @param requirement true if player should be able to access this inventory
         * @return this builder
         */
        public Builder require(InventoryRequirement requirement) {
            this.requirement = requirement;
            return this;
        }

        /**
         * Set the content of the inventory.
         * <br><br>
         * Default context is <code>default</code> and value <code>0</code>.
         * <br><br>
         * Change contexts with {@link io.github.viimeinen1.ainventory.View.View.ContentBuilder#context(int, InventoryContent)} or {@link io.github.viimeinen1.ainventory.View.View.ContentBuilder#context(String, int, InventoryContent)}.
         *
         * @param content content to be added to the inventory
         * @return this builder
         */
        public Builder content(InventoryContent content) {
            this.content = content;
            return this;
        }

        /**
         * Build inventory that shares it's view between players.
         * <br><br>
         * Useful for inventories that require dynamic updating; the inventory will update to other players just like a normal chest.
         * <br><br>
         * WARNING: context is also shared, so if you want to for example make multiple pages, the page change will be updated to all players.
         *
         * @return Shared inventory
         */
        public SharedInventory buildShared() {
            return new SharedInventory(this);
        }

        /**
         * Build inventory where all players have their own unique view of the inventory.
         * Changes in one player's inventory will not affect other player's views.
         *
         * @return new Inventory
         */
        public Inventory build() {
            return new Inventory(this);
        }
    }
}
