package io.github.viimeinen1.ainventory.Examples;

import io.github.viimeinen1.ainventory.GUI.Gui;
import io.github.viimeinen1.ainventory.Inventory.AbstractInventory;
import io.github.viimeinen1.ainventory.Inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class README_examples {

    /*
        Basic inventory usage
     */
    Inventory basicInventory = Inventory.builder()

        // set size of the inventory
        .size(AbstractInventory.SIZE.CHEST_9x3)

        // set title for the inventory
        .title("<gold>Title")

        // create onClick function that is ran every time someone clicks inventory (or tries to modify it with dragging or moving items to it)
        .onClick(event -> event.getWhoClicked().sendMessage("You clicked inventory!"))

        // create custom functionality on open
        .open(event -> event.getPlayer().sendMessage("You opened inventory!"))

        // create custom functionality on close
        .close(event -> event.getPlayer().sendMessage("You closed inventory!"))


        // create requirements for the inventory
        // only players who pass this check will be able to use this inventory
        .require(pl -> pl.isOp() || pl.hasPermission("example.admin"))

        // instead of checking for a permission, we can also just whitelist only specific players
        .whitelisted(true)

        // setting whitelisted UUIDs
        .whitelist(UUID.randomUUID(), UUID.randomUUID())
        .whitelist(UUID.randomUUID()) // you can add more in another call

        // creating inventory content
        .content(c -> {

            // set inventory content here

        })

        // build inventory
        .build();

    /*
        All options are optional, even this is a valid inventory (tough with no content)
     */
    Inventory emptyInventory = Inventory.builder().build();

    // then we can open inventories with just calling Inventory#open(HumanEntity)
    public void openForPlayer(Player player) {
        basicInventory.open(player);
    }


    /*
        Adding content
     */
    Inventory contentExample = Inventory.builder()
        .size(AbstractInventory.SIZE.CHEST_9x3)
        .content(c -> {

            // add new item to slot 10
            c.set(10)
                .build();

            // create border
            c.pattern(
                "#########",
                "#.......#",
                "#########"
            ).material(Material.BLACK_STAINED_GLASS_PANE)
                .build();

        }).build();
}
