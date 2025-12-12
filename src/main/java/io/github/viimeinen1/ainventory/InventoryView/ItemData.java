package io.github.viimeinen1.ainventory.InventoryView;

import java.util.Optional;

import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemClickFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemReloadFunction;
import io.github.viimeinen1.ainventory.InventoryView.AbstractInventoryView.itemRequirementFunction;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder;
import io.github.viimeinen1.ainventory.ItemBuilder.ItemBuildable;
import io.github.viimeinen1.ainventory.ItemBuilder.AbstractItemBuilder.ItemSlotType;

public class ItemData <A extends AbstractItemBuilder<A, B>, B extends ItemBuildable<A, B>> {

    public final Optional<itemClickFunction> clickFn;
    public final Optional<itemReloadFunction<A, B>> reloadFn;
    public final Optional<itemRequirementFunction> requirementFn;
    public final ItemSlotType slotType;

    public ItemData(itemClickFunction clickFn, itemReloadFunction<A, B> reloadFn, itemRequirementFunction requirementFn, ItemSlotType slotType) {
        this.clickFn = Optional.ofNullable(clickFn);
        this.reloadFn = Optional.ofNullable(reloadFn);
        this.requirementFn = Optional.ofNullable(requirementFn);
        this.slotType = slotType;
    }

}
