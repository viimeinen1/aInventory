package io.github.viimeinen1.ainventory.Examples;

import io.github.viimeinen1.ainventory.Inventory.Inventory;

import java.util.List;

public class example1 {

    public static List<String> list = List.of("t1", "t2", "t3", "t4", "t5", "t6", "t7");

    public static Inventory inventory = Inventory.builder()
        .content(c -> {
            c.list(list, b -> {
                b.name(b.value)
                    .preventModification(true)
                    .build();
            }, 1, 2);
        })
        .build();

}
