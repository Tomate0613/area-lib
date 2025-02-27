package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.util.AABBUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BVHNode<T extends BVHItem> {
    private final AABB boundingBox;
    private BVHNode<T> left;
    private BVHNode<T> right;
    private final List<T> leafItems;

    public BVHNode(List<T> items) {
        if (items.size() <= 2) {
            this.leafItems = items;
            this.boundingBox = items.stream()
                .map(BVHItem::getBoundingBox)
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();
        } else {
            this.leafItems = null;
            this.boundingBox = items.stream()
                .map(BVHItem::getBoundingBox)
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();

            var longestAxis = AABBUtils.longestAxis(boundingBox);
            var sorted = items.stream().sorted(Comparator.comparingDouble(a -> Objects.requireNonNull(a.getBoundingBox()).getCenter().get(longestAxis))).toList();
            int mid = items.size() / 2;
            this.left = new BVHNode<>(sorted.subList(0, mid));
            this.right = new BVHNode<>(sorted.subList(mid, sorted.size()));
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

    public List<T> findAreasContaining(Level level, Vec3 position) {
        List<T> result = new ArrayList<>();

        if (!boundingBox.contains(position)) {
            return result;
        }

        if (leafItems != null) {
            for (final var item : leafItems) {
                if (item.contains(level, position)) {
                    result.add(item);
                }
            }
        } else {
            if (left != null) {
                result.addAll(left.findAreasContaining(level, position));
            }
            if (right != null) {
                result.addAll(right.findAreasContaining(level, position));
            }
        }

        return result;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public List<T> listAllAreas() {
        List<T> allAreas = new ArrayList<>();

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

    public BVHNode<T> with(T item) {
        var items = listAllAreas();
        items.add(item);

        return new BVHNode<>(items);
    }

    public BVHNode<T> withAll(Collection<T> item) {
        var items = listAllAreas();
        items.addAll(item);

        return new BVHNode<>(items);
    }

    public @Nullable BVHNode<T> without(T item) {
        var items = listAllAreas();
        items.remove(item);

        if(items.isEmpty()) {
            return null;
        }

        return new BVHNode<>(items);
    }

    public @Nullable BVHNode<T> withoutAll(Collection<T> item) {
        var items = listAllAreas();
        items.removeAll(item);

        if(items.isEmpty()) {
            return null;
        }

        return new BVHNode<>(items);
    }
}
