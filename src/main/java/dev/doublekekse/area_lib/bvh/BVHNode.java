package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.util.AABBUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
public class BVHNode {
    private final AABB boundingBox;
    private BVHNode left;
    private BVHNode right;
    private final List<Area> leafItems;

    public BVHNode(List<Area> items) {
        if (items.size() <= 2) {
            this.leafItems = items;
            this.boundingBox = items.stream()
                .map(Area::getBoundingBox)
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();
        } else {
            this.leafItems = null;
            this.boundingBox = items.stream()
                .map(Area::getBoundingBox)
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();

            var longestAxis = AABBUtils.longestAxis(boundingBox);
            var sorted = items.stream().sorted(Comparator.comparingDouble(a -> Objects.requireNonNull(a.getBoundingBox()).getCenter().get(longestAxis))).toList();
            int mid = items.size() / 2;

            this.left = new BVHNode(sorted.subList(0, mid));
            this.right = new BVHNode(sorted.subList(mid, sorted.size()));
        }
    }

    public boolean contains(Level level, Vec3 position) {
        if (!boundingBox.contains(position)) {
            return false;
        }
        if (leafItems != null) {
            return leafItems.stream().anyMatch(item -> item.contains(level, position));
        }

        return (left != null && left.contains(level, position)) ||
            (right != null && right.contains(level, position));
    }

    private void findAreasContaining(Collection<Area> collection, Level level, Vec3 position) {
        if (!boundingBox.contains(position)) {
            return;
        }

        if (leafItems != null) {
            for (final var item : leafItems) {
                if (item.contains(level, position)) {
                    collection.add(item);
                }
            }
        } else {
            if (left != null) {
                left.findAreasContaining(collection, level, position);
            }
            if (right != null) {
                right.findAreasContaining(collection, level, position);
            }
        }
    }

    private void findAreasContaining(Collection<Area> collection, Level level, Vec3 position, Predicate<Area> predicate) {
        if (!boundingBox.contains(position)) {
            return;
        }

        if (leafItems != null) {
            for (final var item : leafItems) {
                if (item.contains(level, position)) {
                    collection.add(item);
                }
            }
        } else {
            if (left != null) {
                left.findAreasContaining(collection, level, position, predicate);
            }
            if (right != null) {
                right.findAreasContaining(collection, level, position, predicate);
            }
        }
    }

    public List<Area> findAreasContaining(Level level, Vec3 position) {
        var result = new ArrayList<Area>();
        findAreasContaining(result, level, position);

        return result;
    }

    public List<Area> findAreasContaining(Level level, Vec3 position, Predicate<Area> predicate) {
        var result = new ArrayList<Area>();
        findAreasContaining(result, level, position, predicate);

        return result;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public List<Area> listAllAreas() {
        var allAreas = new ArrayList<Area>();

        if (leafItems != null) {
            allAreas.addAll(leafItems);
        } else {
            if (left != null) {
                allAreas.addAll(left.listAllAreas());
            }
            if (right != null) {
                allAreas.addAll(right.listAllAreas());
            }
        }

        return allAreas;
    }

    public BVHNode with(Area item) {
        var items = listAllAreas();
        items.add(item);

        return new BVHNode(items);
    }

    public BVHNode withAll(Collection<Area> item) {
        var items = listAllAreas();
        items.addAll(item);

        return new BVHNode(items);
    }

    public @Nullable BVHNode without(Area item) {
        var items = listAllAreas();
        items.remove(item);

        if (items.isEmpty()) {
            return null;
        }

        return new BVHNode(items);
    }

    public @Nullable BVHNode withoutAll(Collection<Area> item) {
        var items = listAllAreas();
        items.removeAll(item);

        if (items.isEmpty()) {
            return null;
        }

        return new BVHNode(items);
    }
}
