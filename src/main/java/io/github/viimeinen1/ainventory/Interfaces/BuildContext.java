package io.github.viimeinen1.ainventory.Interfaces;

import io.github.viimeinen1.ainventory.Slot.Slot;

/**
 * Context for building slots in a lambda.
 */
@FunctionalInterface
public interface BuildContext {
    void run(Slot.SubSlot.Builder builder);
}
