package io.github.viimeinen1.ainventory.InventoryBuilder;

import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.GUI.AbstractGUI;
import io.github.viimeinen1.ainventory.Inventory.NamedUniqueInventory;

public class NamedUniqueInventoryBuilder <T extends Enum<T>, K extends AbstractGUI<T,?,?,?,?>> extends AbstractUniqueInventoryBuilder<NamedUniqueInventoryBuilder<T, K>, NamedUniqueInventory<T, K>> implements Named<T, K> {
    
    private final T name;
    private final K provider;

    @Override public T name() {return name;}
    @Override public K provider() {return provider;}

    public NamedUniqueInventoryBuilder(T name, K provider) {
        super();
        this.name = name;
        this.provider = provider;
    }

    @Override
    public NamedUniqueInventoryBuilder<T, K> getThis() {return this;}

    @Override
    public NamedUniqueInventory<T, K> build() {
        return new NamedUniqueInventory<T, K>(getThis());
    }

}
