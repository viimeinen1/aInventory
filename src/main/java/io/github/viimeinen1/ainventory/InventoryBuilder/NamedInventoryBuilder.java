package io.github.viimeinen1.ainventory.InventoryBuilder;

import io.github.viimeinen1.ainventory.Common.InventoryProvider;
import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.Inventory.NamedInventory;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

// public class NamedInventoryBuilder <
//         T extends Enum<T>, 
//         K extends AbstractGUI<
//             T, 
//             DefaultItemBuilder<DefaultInventoryView>, 
//             DefaultInventoryView, 
//             NamedInventoryBuilder<T, K>, 
//             NamedInventory<T, K>
//         >
//     > extends AbstractInventoryBuilder<
//         DefaultItemBuilder<DefaultInventoryView>, 
//         DefaultInventoryView, 
//         NamedInventoryBuilder<T, K>, 
//         NamedInventory<T, K>
//     > implements Named<T, K> {

public class NamedInventoryBuilder <
        T extends Enum<T>, 
        K extends InventoryProvider<NamedInventory<T, K>>
    > extends AbstractInventoryBuilder<
        DefaultItemBuilder<DefaultInventoryView>, 
        DefaultInventoryView, 
        NamedInventoryBuilder<T, K>, 
        NamedInventory<T, K>
    > implements Named<T, K> {

    private final T name;
    private final K provider;

    @Override public T name() {return name;}
    @Override public K provider() {return provider;}

    public NamedInventoryBuilder(T name, K provider) {
        this.name = name;
        this.provider = provider;
    }

    @Override
    public NamedInventoryBuilder<T, K> getThis() {return this;}

    @Override
    public NamedInventory<T, K> build() {
        var inv = new NamedInventory<>(getThis());
        this.provider().put(inv);
        return inv;
    }

}
