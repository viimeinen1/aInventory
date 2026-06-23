package io.github.viimeinen1.ainventory.Animation;

import io.github.viimeinen1.ainventory.View.View;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Animation {

    public final HashMap<String, ItemStack> items = new HashMap<>();
    public final List<Frame> frames = new ArrayList<>();
    public final boolean allowClickPassthrough;
    public final Runnable callback;

    public record Frame(long displayTime, String[] pattern) {}

    public ItemStack[] constructFrame(String[] pattern, HashMap<String, ItemStack> items) {
        ArrayList<ItemStack> frame = new ArrayList<>();
        for (String line : pattern) {
            for (String str : line.split("")) {
                frame.add(items.get(str));
            }
        }
        return frame.toArray(new ItemStack[0]);
    }

    public long ticksUntilNextFrame = 0;
    public ItemStack[] currentFrame = null;

    /**
     * Run animation in inventory. After animation, the callback is run.
     *
     * @param inventory display inventory
     * @param callback callback
     */
    public void run(Inventory inventory, Runnable callback) {
        if (frames.isEmpty()) return;
        var queue = new ArrayDeque<>(this.frames);
        var plugin = JavaPlugin.getProvidingPlugin(Animation.class);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            // if not yet time for next frame
            if (ticksUntilNextFrame >= 1) {
                ticksUntilNextFrame--;
                return;
            }

            // if animation ended
            if (queue.isEmpty()) {
                this.ticksUntilNextFrame = 0;
                this.currentFrame = null;
                if (this.callback != null) this.callback.run();
                callback.run();
                task.cancel();
                return;
            }

            // advance to next frame
            var nextFrame = queue.poll();
            this.currentFrame = constructFrame(nextFrame.pattern, this.items);
            this.ticksUntilNextFrame = nextFrame.displayTime;

            // display frame if it's the correct size
            if (this.currentFrame.length == 0 || inventory.getSize() > this.currentFrame.length) return;
            inventory.setContents(this.currentFrame);

        }, 1L, 1L);
    }

    public Animation(Builder builder) {
        this.items.putAll(builder.items);
        this.frames.addAll(builder.frames);
        this.allowClickPassthrough = builder.allowClickPassthrough;
        this.callback = builder.callback;

        if (!builder.displayTooltip) {
            this.items.values().forEach(t -> t.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build()));
        }
    }

    public static class Builder {

        public Builder(String id, View view) {
            this.view = view;
            this.id = id;
        }

        private final String id;
        private final View view;
        private final HashMap<String, ItemStack> items = new HashMap<>();
        private final List<Frame> frames = new ArrayList<>();
        private long displayTime = 20; // in ticks
        private boolean displayTooltip = false;
        private boolean allowClickPassthrough = false;
        private Runnable callback = null;

        /**
         * Create callback that gets executed when the animation finishes
         *
         * @param callback callback
         * @return builder
         */
        public Builder callback(Runnable callback) {
            this.callback = callback;
            return this;
        }

        /**
         * How long should frames after this setting is used be displayed for? Default is 20 ticks. (1 frame per second)
         *
         * @param displayTime display time of frame in ticks
         * @return builder
         */
        public Builder displayTime(long displayTime) {
            this.displayTime = displayTime;
            return this;
        }

        /**
         * If items in frames should display tooltip. Default is false.
         *
         * @param displayTooltip if tooltip should be displayed
         * @return builder
         */
        public Builder displayTooltip(boolean displayTooltip) {
            this.displayTooltip = displayTooltip;
            return this;
        }

        /**
         * If animation is part of a View, should clicks made to the view be applied normally, or should they be blocked?
         *
         * @param allowClickPassthrough if clicking during animation should be allowed
         * @return builder
         */
        public Builder allowClickPassthrough(boolean allowClickPassthrough) {
            this.allowClickPassthrough = allowClickPassthrough;
            return this;
        }

        /**
         * Assign an item to a character in pattern. Constructing patterns happens after building, so when you assign the items to the builder doesn't matter.
         * <br><br>
         * All characters must be only 1 long, as they are compared against single characters in text.
         *
         * @param character character that is assigned to item.
         * @param item item to assign to this character
         * @return builder
         */
        public Builder item(String character, ItemStack item) {
            items.put(character, item);
            return this;
        }

        /**
         * Add frame to this animation. The frames will be displayed in the order they are added to the builder.
         * The pattern must be the same size as the inventory it's displayed in (or smaller).
         * <br><br>
         * The display time of this frame can be set with {@link Builder#displayTime(long)}, or it will be the default 20 ticks.
         * <br><br>
         * example frame pattern for 9x4 inventory:<br><code>
         * #########<br>
         * #.......#<br>
         * #.......#<br>
         * #########<br>
         * </code>
         *
         * @param pattern pattern of the frame
         * @return builder
         */
        public Builder frame(String... pattern) {
            return frame(displayTime, pattern);
        }

        /**
         * Add frame to this animation. The frames will be displayed in the order they are added to the builder.
         * The pattern must be the same size as the inventory it's displayed in (or smaller).
         * <br><br>
         * example frame pattern for 9x4 inventory:<br><code>
         * #########<br>
         * #.......#<br>
         * #.......#<br>
         * #########<br>
         * </code>
         *
         * @param displayTime display time of the frame.
         * @param pattern pattern of the frame
         * @return builder
         */
        public Builder frame(long displayTime, String... pattern) {
            return frame(new Frame(displayTime, pattern));
        }

        /**
         * Add frame to this animation. The frames will be displayed in the order they are added to the builder.
         * The pattern must be the same size as the inventory it's displayed in (or smaller).
         * <br><br>
         * example frame pattern for 9x4 inventory:<br><code>
         * #########<br>
         * #.......#<br>
         * #.......#<br>
         * #########<br>
         * </code>
         *
         * @param frame new frame
         * @return builder
         */
        public Builder frame(Frame frame) {
            frames.add(frame);
            return this;
        }

        /**
         * Build the animation and add it to view.
         *
         * @return new animation
         */
        public Animation build() {
            var animation = new Animation(this);
            this.view.addAnimation(this.id, animation);
            return animation;
        }

    }

}
