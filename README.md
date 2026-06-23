# aInventory
Inventory GUI library with focus on having the whole configuration on one file (instead of needing to move listeners to another method or spot).

The library was made for usage in my own plugins, so some features may be missing.

Library does not currently support animations (easily), or other inventory sizes than 9x1 to 9x6

## features

- All functionality in one place
- Clean item and inventory creation with builders
- custom drag, double click and shift click recreation for better content managing.
- Context system that allows creating multiple pages per inventory
- Lists that wrap to next page 
- Patterns
- GUIs with custom keys for better multi-view GUI support

## Add as dependency

To use the library in your project, add it as a dependency.

The dependency can be seen at https://central.sonatype.com/artifact/io.github.viimeinen1.ainventory/aInventory

build.gradle.kts
```kotlin
dependencies (
    implementation("io.github.viimeinen1.ainventory:aInventory:3.1.0")
)
```

## Usage

<details>
<summary>Creating basic inventory</summary>

```java
/*
        Basic inventory usage
     */
Inventory inventory = Inventory.builder()

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
    All options are optional, instead defaults are used. Even this is a valid inventory (though with no content)
 */
Inventory emptyInventory = Inventory.builder().build();
```

</details>