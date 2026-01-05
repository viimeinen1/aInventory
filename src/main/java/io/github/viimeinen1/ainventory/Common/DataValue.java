package io.github.viimeinen1.ainventory.Common;

public class DataValue {
    public final int max;
    public int value = 0;

    /**
     * Create new DataValue.
     *
     * value will start at 0, and it can't get over max value (inclusive).
     * 
     * @param max maximum value (inclusive).
     */
    public DataValue(int max) {
        this.max = max;
    }

    /**
     * Add 1 to value.
     *
     * Will stop at max value.
     * 
     * @return current value.
     */
    public int next() {
        if (value < max) {
            value++;
        }
        return value;
    }

    /**
     * remove 1 from value. Will stop at 0.
     * 
     * @return current value.
     */
    public int prev() {
        if (value > 0) {
            value--;
        }
        return value;
    }

    /**
     * Set value. Will not change value if it's not between 0 and max value.
     *
     * @param value value to set to.
     * @return the current value.
     */
    public int set(int value) {
        if (value >= 0 && value <= max) {
            this.value = value;
        }
        return value;
    }
}
