package io.github.viimeinen1.ainventory.InventoryView;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.viimeinen1.ainventory.Common.DataValue;
import io.github.viimeinen1.ainventory.Common.ValuedSlotMap;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.valuedItemFunction.ValuedItemBuilder;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;
import net.kyori.adventure.text.Component;

/**
 * Default inventory view.
 * 
 * Everything possible is final.
 * Only reload and update is permitted.
 */
public final class DefaultInventoryView extends AbstractInventoryView<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView> {

    /**
     * Create new aInventoryView with all parameters.
     *
     * All parameters are final, if change is required, create new view.
     *
     * @param size size of view
     * @param title title of view
     * @param initFn initialization function of view
     * @param openFn open function of view
     * @param closeFn close function of view
     * @param requirementFn requirement function of view
     * @param defaultClickFn default click function of view
     * @param owner owner of view
     * @param values values (pages)
     * @param disableDrag if drag placing should be disabled
     */
    public DefaultInventoryView(
        @NotNull INVENTORY_SIZE size,
        @Nullable Component title,
        @Nullable inventoryFunction<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView> initFn,
        @Nullable inventoryOpenFunction openFn,
        @Nullable inventoryCloseFunction closeFn,
        @Nullable inventoryRequirementFunction requirementFn,
        @Nullable itemClickFunction defaultClickFn,
        @Nullable UUID owner,
        @Nullable Map<String, DataValue> values,
        boolean disableDrag
    ) {
        super(
            size,
            title,
            initFn,
            openFn,
            closeFn,
            requirementFn,
            defaultClickFn,
            owner,
            values,
            disableDrag
        );
    }

    @Override
    protected void initView(@NotNull Map<String, DataValue> data, @Nullable HumanEntity player) {
        clear();
        initFn.ifPresent(fn -> fn.run(this, Optional.ofNullable(player)));
        update();
    }

    @Override
    public @NotNull Inventory getInventory() {return this.inventory;}

    @Override
    public DefaultItemBuilder<DefaultInventoryView> ItemBuilder(Integer slot) {
        return new DefaultItemBuilder<>(this, slot);
    }

    @Override
    public DefaultItemBuilder<DefaultInventoryView> ItemBuilder(Collection<Integer> slots) {
        return new DefaultItemBuilder<>(this, slots);
    }

    @Override
    public <T> void ValuedItemList(Collection<Integer> slots, Collection<T> values, String key, @Nullable HumanEntity player, valuedItemFunction<T, DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView> fn) {
        int value = this.value(key);
        ValuedSlotMap<T> map = new ValuedSlotMap<>(slots, values);
        map.values.forEach((key1, value1) -> {
            if (key1.value() != value) {
                return;
            }

            fn.run(new ValuedItemBuilder<>(value1, new DefaultItemBuilder<>(this, key1.slot()), key1.slot(), Optional.ofNullable(player)));
        });
    }
    
}
