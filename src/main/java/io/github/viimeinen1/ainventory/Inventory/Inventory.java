package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class Inventory extends AbstractInventory {
    public final HashMap<UUID, View> views = new HashMap<>();

    /**
     * Create new inventory
     *
     * @param builder inventory builder
     */
    public Inventory(AbstractInventory.Builder builder) {
        super(builder);
    }

    /**
     * Get new inventory builder.
     *
     * @return new inventory builder
     */
    public static Inventory.Builder builder() {
        return new Inventory.Builder();
    }

    /**
     * Reload this inventory.
     * Will delete all contents in the inventory that is player placed,
     * and re-run all builder functions.
     * <br><br>
     * Useful for updating inventory if builder functions give different results depending on a state.
     * <br><br>
     * If nobody is viewing the inventory, the reload will happen the next time someone opens the inventory.
     * <br><br>
     * To reload only single player's view, use {@link Inventory#getView(HumanEntity)} and reload the view itself.
     */
    @Override
    public void reload() {
        this.views.values().forEach(View::reload);
    }

    /**
     * get view associated with this player. As this inventory type is not shared, it always excepts a player. Giving <code>null</code> as a parameter will throw an error.
     *
     * @throws IllegalArgumentException if player is null, since this type of inventory always excepts player.
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
