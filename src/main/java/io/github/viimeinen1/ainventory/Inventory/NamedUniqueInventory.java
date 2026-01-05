package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.GUI.AbstractGUI;
import io.github.viimeinen1.ainventory.InventoryBuilder.NamedUniqueInventoryBuilder;

public final class NamedUniqueInventory <T extends Enum<T>, K extends AbstractGUI<T,?,?,?,?>> extends AbstractUniqueInventory<NamedUniqueInventoryBuilder<T, K>, NamedUniqueInventory<T, K>> implements Named<T, K> {
    
    private final T name;
    private final K provider;

    @Override public T name() {return name;}
    @Override public K provider() {return provider;}

    public NamedUniqueInventory(NamedUniqueInventoryBuilder<T, K> builder) {
        super(builder);
        this.name = builder.name();
        this.provider = builder.provider();
    }

    @Override
    public NamedUniqueInventory<T, K> getThis() {return this;}

    public static <T extends Enum<T>, K extends AbstractGUI<T,?,?,?,?>> NamedUniqueInventoryBuilder<T, K> builder(T name, K provider) {
        return new NamedUniqueInventoryBuilder<>(name,provider);
    }

}
