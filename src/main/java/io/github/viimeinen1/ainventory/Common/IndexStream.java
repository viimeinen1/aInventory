package io.github.viimeinen1.ainventory.Common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Helper to make streaming values to inventory easier.
 */
public class IndexStream {

    /**
     * Convert collection to stream with {@link StreamValue}s.
     * Order might be random.
     * Indexes will start at 0. Use {@link Stream#limit(long)} to limit the amount of values in the stream.
     * Usage:
     * aValueStream.toStream(collection).limit(limit).forEach(val -> {val.index(); val.value()});
     * 
     * @param <T> Collection value type
     * @param collection Collection
     * @return Stream of {@link StreamValue} with index and value
     */
    public static <T> Stream<StreamValue<T>> toStream(Collection<T> collection) {
        IndexedCollection<T> streamable = new IndexedCollection<>();
        collection.forEach(streamable::add);
        return streamable.values.stream();
    }

    /**
     * Helper class to index values.
     */
    private static class IndexedCollection<T> {
        private int i = 0;
        public final ArrayList<StreamValue<T>> values = new ArrayList<>();

        /***
         * Add value to collection.
         * 
         * Will assign value an index in order.
         * 
         * @param value add value to the IndexStream
         */
        public void add(T value) {
            values.add(new StreamValue<>(i, value));
            i++;
        }
    }

    /**
     * Stream value with index and value.
     */
    public record StreamValue<T>(int i, T value) {}
}
