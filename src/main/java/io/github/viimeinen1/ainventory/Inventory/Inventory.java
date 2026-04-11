package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class Inventory extends AbstractInventory {
    public final HashMap<UUID, View> views = new HashMap<>();

    public Inventory(AbstractInventory.Builder builder) {
        super(builder);
    }

    public static Inventory.Builder builder() {
        return new Inventory.Builder();
    }

    /**
     * get view of player.
     *
     * @throws IllegalArgumentException if player is null
     *
     * @param player player
     * @return view linked to the player
     */
    @Override
    public @NotNull View getView(@Nullable HumanEntity player) {
        if (player == null) throw new IllegalArgumentException("uuid cannot be null");
        var view = views.get(player.getUniqueId());
        if (view == null) {
            view = new View(
                this.inventoryBuilder.size,
                this.inventoryBuilder.content,
                this.inventoryBuilder.onClick,
                this.inventoryBuilder.title,
                this.inventoryBuilder.requirement,
                this.inventoryBuilder.open,
                this.inventoryBuilder.close,
                this.inventoryBuilder.whitelisted,
                this.inventoryBuilder.whitelist,
                player
            );
            this.views.put(player.getUniqueId(), view);
        }
        return view;
    }

}
