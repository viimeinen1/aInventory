package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.Slot.Slot;

@FunctionalInterface
public interface ListBuildContext <T> {
    void run(Slot.SubSlot.ValuedBuilder<T> listBuilder);
}
