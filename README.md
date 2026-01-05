# aInventory
Inventory GUI library with focus on having the whole configuration on one file (instead of multiple).

The library was made for usage in my own plugins, so some features may be missing.

Plan is to add more functions as I requre them (like animations, pages, or other stuff).

## Add as dependency

To use the library in your project, add it as a dependency.

The dependency can be seen at https://central.sonatype.com/artifact/io.github.viimeinen1.ainventory/aInventory

build.gradle.kts
```kotlin
dependencies (
    implementation("io.github.viimeinen1.ainventory:aInventory:2.3.0")
)
```

## Usage

<details>
<summary>Creating basic inventory</summary>

```java
/*
 * Basic usage showing all methods in builders
*/

DefaultInventory inventory = DefaultInventory.builder()
    .size(INVENTORY_SIZE.CHEST_9x3)
    .title("<gold>Title")
    .require(pl -> pl.isOp() || pl.hasPermission("example.admin"))
    // .owner(player.getUniqueId()) // works similarly to .require()
    .defaultAction(event -> event.getWhoClicked().sendMessage("You clicked inventory!"))
    .openFunction(event -> event.getPlayer().sendMessage("You opened inventory!"))
    .closeFunction(event -> event.getPlayer().sendMessage("You closed inventory!"))
    .disableDrag(true) // disabled by default, can cause issues with ItemSlotTypes

    // init function of inventory
    .initialization((view, player) -> {

        // add items here with ItemBuilder
        view.ItemBuilder(0)
            .material(Material.IRON_DOOR)
            .amount(1) // default is 1
            .name("<red>Close", false)
            .slotType(AbstractItemBuilder.ItemSlotType.BUTTON) // handles blocking taking item
            .lore(List.of(
                "",
                "<red>Close inventory", // minimessage format
                Component.empty() // accepts components as well (you can mix them)
            ), false)
            .function(event -> Bukkit.getScheduler().runTask(plugin, () -> event.getWhoClicked().closeInventory()))
            .build(); // apply item to view

        ItemStack borderItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).asQuantity(2);

        view.ItemBuilder(List.of(1,2,3)) // set multiple slots
            .addSlot(4) // add even more slots
            .setItem(borderItem) // setting item
            .setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay().hideTooltip(true).build()
            )
            .slotType(AbstractItemBuilder.ItemSlotType.BORDER) // prevent any modification
            .build();
    })
    .build();

/*
    To make the inventory unique to everyone, use
 */
UniqueInventory inventory = UniqueInventory.builder()
    // inventory setup
    .build();

// Open inventory to players with
inventory.open(player);

// initialize again with
inventory.initialize(null);

```

</details>

<details>
<summary>GUIs</summary>

```java
/*
    Basic GUIs
 */

// inventories of gui
enum INVENTORY {
    MAIN,
    QUIT
}

// create gui
var gui = new GUI<>(INVENTORY.class);

// add inventories to gui
gui.builder(INVENTORY.MAIN)
    // inventory setup
    .build();

gui.builder(INVENTORY.QUIT)
    .build();

/*
    Unique GUI works the same, but the inventories are not shared.
 */

var uniqueGUI = new UniqueGUI<>(INVENTORY.class);

```

</details>

</details>

<details>
<summary>Reloading</summary>

Basic reload usage

Instead of recreating the whole view again with `.initialize()`,
we can reload the inventory, and specify what we want to reload.

Inventory is reloaded every time someone opens the inventory.
```java
// let's say we have value
Integer counter = 0;

// we can display inventory with
var inv = DefaultInventory.builder()
    .openFunction(event -> counter++) // add 1 to counter every time inventory is opened
    .initialization((view, player) -> {
        view.ItemBuilder(0)
            .reload((builder, pl) ->
                builder.name(counter, false).build() // display counter on reload.
            )
            .build();
    })
    .build();
```
now the item name should always display the counter, as the view is reloaded every time someone opens it.

Because only the opened view is reloaded, we need to reload other views as well. This can be done with
DefaultInventory#reload(null)

</details>

<details>
<summary>Player specific information</summary>

aInventory supports player specific information with passing player instance when initializing or reloading.

Because player can be set to null on manual reload, player is wrapped into Optional.

```java
// simple inventory that displays player's name in item name
var inventory = UniqueInventory.builder()
    .size(INVENTORY_SIZE.CHEST_9x1)
    .defaultAction(event -> event.setCancelled(true))
    .initialization((view, player) -> {
        player.ifPresent(pl -> {
            view.ItemBuilder(0)
                .material(Material.STONE)
                .name(pl.name(), false)
                .build();
        });
    })
    .build();
```

When using player specific information, remember to specify player when manually reloading or initializing

```java
view.reload(player);
```

</details>

<details>
<summary>Slot types</summary>

Slot types are optional, tough they will make life a lot easier when used correctly.
Slot types were created for containers and crafting GUIs, that require players to place items inside the inventory.

All slots are type CUSTOM as default, and they won't inherit any custom features.

Slot types:
- CUSTOM: no preset behaviour (default)
- BUTTON: no taking or placing allowed, only clicking.
- CONTAINER: everything allowed. Supports shift clicks.
- CRAFTING: same as container, except items are returned to inventory after view is closed.
- RESULT: only taking is allowed, contents will be placed to inventory after view is closed.
- BORDER: everything is disabled (.function() will not run at all)

</details>

<details>
<summary>Values (Pages)</summary>

aInventory supports multiple page values with value system.

The system works as follows:
```java

// in setup, create values
DefaultInventory.builder()
    // other setup

    // create page values with keys and maximum values
    // all pages start from 0
    .value("pageValue1", 2)
    .value("pageValue2", 10)
    
    // you can refer to the values in initialization
    .initialization((view, player) -> {
        
        int value = view.value("pageValue1");
        
        // create item that only shows when page is 1
        if (value < 1) {
            view.ItemBuilder(0)
                // item setup
                .build();
            
            // add 1 to value
            // will initialize view again to rebuild it with the new values.
            view.next("pageValue2", null);
        }
        
        int value = view.value("does-not-exist"); // will return 0, because value doesn't exist
    })
    
    .build();

```

</details>

<details>
<summary>IndexStream</summary>

IndexStream was created for more ease of use in adding collections to inventories.

```java
Collection<? extends Player> players = Bukkit.getOnlinePlayers();

// inside inventory setup
IndexStream.toStream(players).limit(9).forEach(entry -> { // create stream with entries [i=index; value=Player]
    inv.ItemBuilder(entry.index) // Create player heads starting from index 0, up to index 8.
        .material(Material.PLAYER_HEAD)
        .name(entry.value.name(), false)
        .build();
    }
);
```

</details>

<details>
<summary>ValuedItemList</summary>

With ValuedItemList it's possible to display lists of values more easily without a lot of switch cases etc.

The method will create a list that only occupies preset slots, and will fill the slots on multiple views,
so when value is changed, the list will change as well to next "page".

```java

// example usage of ValuedItemList

// list
var players = Bukkit.getOnlinePlayers().stream().toList();

DefaultInventory.builder()
    .size(INVENTORY_SIZE.CHEST_9x3)
    .value("pageValue1", 2) // creating value
    .initialization((view, player) -> {
        view.ValuedItemList(
            List.of(0,1,2,3,4,5,6,7,8), // what slots
            players, // value list
            "pageValue1", // value's key
            null, // player for reloads and initializations
            itemBuilder -> { // building the item
                itemBuilder.builder()
                    .material(Material.PLAYER_HEAD)
                    .name(itemBuilder.value().name(), false) // set item's name to player's name
                .build();
            }
        );
    })
    .build();
```

</details>