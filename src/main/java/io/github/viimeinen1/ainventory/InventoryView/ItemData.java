package io.github.viimeinen1.ainventory.InventoryView;

import java.util.Optional;

import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemClickFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemReloadFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemRequirementFunction;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;
import io.github.viimeinen1.ainventory.ItemBuilder.ItemBuildable;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder.ItemSlotType;

public record ItemData<A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>>(
    Optional<itemClickFunction> clickFn, Optional<itemReloadFunction<A, B>> reloadFn,
    Optional<itemRequirementFunction> requirementFn, ItemSlotType slotType) {

    public ItemData(itemClickFunction clickFn, itemReloadFunction<A, B> reloadFn, itemRequirementFunction requirementFn, ItemSlotType slotType) {
        this(
            Optional.ofNullable(clickFn),
            Optional.ofNullable(reloadFn),
            Optional.ofNullable(requirementFn),
            slotType
        );
    }

}
