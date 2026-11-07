package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.Slot.Slot;

/**
 * Context for creating dynamic slots.
 */
@FunctionalInterface
public interface DynamicBuildContext {
    /**
     * Run dynamic slot builder
     * @param builder dynamic slot builder
     */
    void run(Slot.SubSlot.DynamicBuilder builder);
}

