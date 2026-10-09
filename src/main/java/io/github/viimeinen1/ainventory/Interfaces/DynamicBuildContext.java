package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.Slot.Slot;

/**
 * Context for creating dynamic slots.
 */
@FunctionalInterface
public interface DynamicBuildContext {
    void run(Slot.SubSlot.DynamicBuilder builder);
}

