package io.github.viimeinen1.ainventory.Common;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * Create paged map for list of values.
 */
public class ValuedSlotMap <T> {
    
    public static record SlotCoordinate(int value, int slot) {};

    public final Map<SlotCoordinate, T> values = new HashMap<>();

    public ValuedSlotMap(@NotNull Collection<Integer> slots, @NotNull Collection<T> values) {
        List<Integer> slotList = slots.stream().collect(Collectors.toList());
        IndexStream.toStream(values).forEach(val -> {
            int page = val.i / slots.size();
            int slot = slotList.get(val.i % slots.size());
            this.values.put(new SlotCoordinate(page, slot), val.value);
        });
    }

}
