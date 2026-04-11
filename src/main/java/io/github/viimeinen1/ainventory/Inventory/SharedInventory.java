package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.View.View;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SharedInventory extends AbstractInventory {
    private View view;

    public SharedInventory(Builder builder) {
        super(builder);
    }

    public static SharedInventory.Builder builder() {
        return new SharedInventory.Builder();
    }

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
