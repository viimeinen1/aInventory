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
        public int size() {
            return this.size;
        }
    }

    protected final Builder inventoryBuilder;

    public AbstractInventory(Builder inventoryBuilder) {
        var plugin = JavaPlugin.getProvidingPlugin(AbstractInventory.class);
        InventoryListener.initializeListener(plugin);
        this.inventoryBuilder = inventoryBuilder;
    }

    public abstract @NotNull View getView(@Nullable HumanEntity player);

    public void open(@NotNull HumanEntity player) {
        getView(player).open(player);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected SIZE size; // size of inventory
        protected boolean shared; // if inventory should be shared
        protected InventoryContent content; // content creation function
        protected ItemClick onClick; // default action
        protected Component title; // title of inventory
        protected InventoryRequirement requirement; // requirement to open
        protected InventoryOpen open; // open action
        protected InventoryClose close; // close action
        protected boolean whitelisted = false; // if only whitelisted players can access inventory
        protected Set<UUID> whitelist = new HashSet<>(); // whitelisted players

        public Builder onClick(@Nullable ItemClick onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder openFunction(@Nullable InventoryOpen openFunction) {
            this.open = openFunction;
            return this;
        }

        public Builder closeFunction(@Nullable InventoryClose closeFunction) {
            this.close = closeFunction;
            return this;
        }

        public Builder size(@Nullable SIZE size) {
            this.size = size;
            return this;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder title(String title) {
            this.title = MiniMessage.miniMessage().deserialize(title);
            return this;
        }

        public Builder whitelisted(boolean whitelisted) {
            this.whitelisted = whitelisted;
            return this;
        }

        public Builder whitelist(UUID... uuid) {
            this.whitelist.addAll(Arrays.asList(uuid));
            return this;
        }

        public Builder require(InventoryRequirement requirement) {
            this.requirement = requirement;
            return this;
        }

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

        public Inventory build() {
            return new Inventory(this);
        }
    }
}
