package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.component.AreaComponentType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Feel free to use these, however they could be moved or changed at any point
 */
@ApiStatus.Experimental
public class ExperimentalAreaUtils {
    public static <T> @Nullable T componentFor(AreaComponentType<T> type, Collection<Area> areas) {
        int priority = Integer.MIN_VALUE;
        var smallest = Double.MAX_VALUE;
        T component = null;

        for (var area : areas) {
            var prio = area.getPriority();
            if (prio < priority) {
                continue;
            }

            var bb = area.getBoundingBox();
            if (bb == null) {
                continue;
            }

            var size = bb.getSize();

            if (prio == priority && size >= smallest) {
                continue;
            }

            var comp = area.get(type);
            if (comp == null) {
                continue;
            }

            component = comp;
            smallest = size;
            priority = prio;
        }

        return component;
    }

    public static @Nullable Area areaFor(AreaComponentType<?> type, Collection<Area> areas) {
        int priority = Integer.MIN_VALUE;
        var smallest = Double.MAX_VALUE;
        Area bestArea = null;

        for (var area : areas) {
            var prio = area.getPriority();
            if (prio < priority) {
                continue;
            }

            var bb = area.getBoundingBox();
            if (bb == null) {
                continue;
            }

            var size = bb.getSize();

            if (prio == priority && size >= smallest) {
                continue;
            }

            if (!area.has(type)) {
                continue;
            }

            bestArea = area;
            smallest = size;
            priority = prio;
        }

        return bestArea;
    }
}
