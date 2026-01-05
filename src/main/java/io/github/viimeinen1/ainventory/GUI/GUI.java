package io.github.viimeinen1.ainventory.GUI;

import org.jetbrains.annotations.NotNull;

import io.github.viimeinen1.ainventory.Inventory.NamedInventory;
import io.github.viimeinen1.ainventory.InventoryBuilder.NamedInventoryBuilder;
import io.github.viimeinen1.ainventory.InventoryView.DefaultInventoryView;
import io.github.viimeinen1.ainventory.ItemBuilder.DefaultItemBuilder;

/**
 * a GUI that stores multiple inventories.
 */
public class GUI <
        T extends Enum<T>
    > extends AbstractGUI<
        T, 
        DefaultItemBuilder<DefaultInventoryView>, 
        DefaultInventoryView, 
        NamedInventoryBuilder<T, GUI<T>>, 
        NamedInventory<T, GUI<T>>
    > {

    /**
     * create new GUI.
     * 
     * @param enumClass enum with all possible inventories in the GUI
     */
    public GUI(@NotNull Class<T> enumClass) {
        super(enumClass);
    }

    /**
     * Inventory builder.
     */
    @Override
    public NamedInventoryBuilder<T, GUI<T>> builder(T name) {return new NamedInventoryBuilder<>(name, this);}

}
