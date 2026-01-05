package io.github.viimeinen1.ainventory.Inventory;

import io.github.viimeinen1.ainventory.Common.InventoryProvider;
import io.github.viimeinen1.ainventory.Common.Named;
import io.github.viimeinen1.ainventory.InventoryBuilder.NamedInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

/**
 * Inventory that has 'name' for identifying.
 */
// public final class NamedInventory <
//         T extends Enum<T>, 
//         K extends AbstractGUI<
//             T, 
//             DefaultItemBuilder<DefaultInventoryView>, 
//             DefaultInventoryView, 
//             NamedInventoryBuilder<T, K>, 
//             NamedInventory<T, K>
//         >
//     > extends AbstractInventory<
//         DefaultItemBuilder<DefaultInventoryView>, 
//         DefaultInventoryView, 
//         NamedInventoryBuilder<T, K>, 
//         NamedInventory<T, K>
//     > implements Named<T, K> {

public final class NamedInventory <
        T extends Enum<T>,
        K extends InventoryProvider<NamedInventory<T, K>>
    > extends AbstractInventory<
        DefaultItemBuilder<DefaultInventoryView>, 
        DefaultInventoryView, 
        NamedInventoryBuilder<T, K>, 
        NamedInventory<T, K>
    > implements Named<T, K> {

    private final T name;
    private final K provider;

    @Override public T name() {return name;}
    @Override public K provider() {return provider;}

    /**
     * Create new {@link AbstractInventory}.
     * 
     * Prefer {@link AbstractInventory.Builder#build()} to build new inventory.
     * 
     * @param builder extends {@link AbstractInventory.Builder}
     */
    public NamedInventory(NamedInventoryBuilder<T, K> builder) {
        super(builder);
        this.name = builder.name();
        this.provider = builder.provider();
    }

    @Override
    public DefaultInventoryView createView() {
        return new DefaultInventoryView(
            builder.size,
            builder.title,
            builder.initialization,
            builder.openFunction,
            builder.closeFunction,
            builder.requirementFunction,
            builder.defaultClickAction,
            builder.owner,
            builder.values,
            builder.disableDrag
        );
    }

    @Override
    public NamedInventory<T, K> getThis() {return this;}

    // public static <
    //     T extends Enum<T>, 
    //     K extends AbstractGUI<
    //         T, 
    //         DefaultItemBuilder<DefaultInventoryView>, 
    //         DefaultInventoryView, 
    //         NamedInventoryBuilder<T, ? extends AbstractGUI<T, DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, NamedInventoryBuilder<T, K>, NamedInventory<T, K>>>, 
    //         NamedInventory<T, ? extends AbstractGUI<T, DefaultItemBuilder<DefaultInventoryView>, DefaultInventoryView, NamedInventoryBuilder<T, K>, NamedInventory<T, K>>>
    //     >
    // > NamedInventoryBuilder<T, K> builder(T name, K provider) {
    //     return new NamedInventoryBuilder<>(name, provider);
    // }

    public static <
        T extends Enum<T>,
        K extends InventoryProvider<NamedInventory<T, K>>
    > NamedInventoryBuilder<T, K> builder(T name, K provider) {
        return new NamedInventoryBuilder<>(name, provider);
    }
}
