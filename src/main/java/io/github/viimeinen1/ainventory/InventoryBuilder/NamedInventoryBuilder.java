package io.github.viimeinen1.ainventory.InventoryBuilder;

import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.GUI.AbstractGUI;
import io.github.viimeinen1.ainventory.Inventory.NamedInventory;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

public class NamedInventoryBuilder <T extends Enum<T>, K extends AbstractGUI<T, ?, ?, ?, ?>> extends AbstractInventoryBuilder<DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, NamedInventoryBuilder<T, K>, NamedInventory<T, K>> implements Named<T, K> {
    
    private final T name;
    private final K provider;

    @Override public T name() {return name;}
    @Override public K provider() {return provider;}

    public NamedInventoryBuilder(T name, K provider) {
        this.name = name;
        this.provider = provider;
    }

    public NamedInventoryBuilder<T, K> getThis() {return this;}

    public NamedInventory<T, K> build() {
        var inv = new NamedInventory<>(getThis());
        this.provider.put(inv);
        return new NamedInventory<T, K>(getThis());
    }

}
