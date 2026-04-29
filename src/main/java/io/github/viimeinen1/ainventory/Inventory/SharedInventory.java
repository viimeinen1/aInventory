package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SharedInventory extends AbstractInventory {
    private View view;

    /**
     * Create new shared inventory
     *
     * @param builder new builder
     */
    public SharedInventory(Builder builder) {
        super(builder);
    }

    /**
     * Get new inventory builder.
     * <br><br>
     * Use {@link Builder#buildShared()} to create new shared inventory. Otherwise, a normal inventory is created.
     *
     * @return new inventory builder
     */
    public static SharedInventory.Builder builder() {
        return new SharedInventory.Builder();
    }

    /**
     * Reload this inventory.
     * Will delete all contents in the inventory that is player placed,
     * and re-run all builder functions.
     * <br><br>
     * Useful for updating inventory if builder functions give different results depending on a state.
     * <br><br>
     * If nobody is viewing the inventory, the reload will happen the next time someone opens the inventory.
     */
    @Override
    public void reload() {
        this.view.reload();
    }

    /**
     * Get view associated with this inventory.
     * As this inventory is shared, will always return the same view, even if player is null.
     *
     * @param player player (or null)
     * @return view associated with this inventory
     */
    @Override
    public @NotNull View getView(@Nullable HumanEntity player) {
        if (view == null) {
            this.view = new View(
                this.inventoryBuilder.size,
                this.inventoryBuilder.content,
                this.inventoryBuilder.onClick,
                this.inventoryBuilder.title,
                this.inventoryBuilder.requirement,
                this.inventoryBuilder.open,
                this.inventoryBuilder.close,
                this.inventoryBuilder.whitelisted,
                this.inventoryBuilder.whitelist,
                null
            );
        }
        return view;
    }
}
