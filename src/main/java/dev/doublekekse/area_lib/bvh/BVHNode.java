package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.util.AABBUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BVHNode<T extends BVHItem> {
    private final AABB boundingBox;
    private BVHNode<T> left;
    private BVHNode<T> right;
    private final List<T> leafItems;

    public BVHNode(AreaSavedData savedData, List<T> items) {
        if (items.size() <= 2) {
            this.leafItems = items;
            this.boundingBox = items.stream()
                .map((item) -> item.getBoundingBox(savedData))
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();
        } else {
            this.leafItems = null;
            this.boundingBox = items.stream()
                .map((item) -> item.getBoundingBox(savedData))
                .filter(Objects::nonNull)
                .reduce(AABBUtils::encapsulate)
                .orElseThrow();

            var longestAxis = AABBUtils.longestAxis(boundingBox);
            items.sort(Comparator.comparingDouble(a -> Objects.requireNonNull(a.getBoundingBox(savedData)).getCenter().get(longestAxis)));
            int mid = items.size() / 2;
            this.left = new BVHNode<>(savedData, items.subList(0, mid));
            this.right = new BVHNode<>(savedData, items.subList(mid, items.size()));
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

    public BVHNode<T> with(AreaSavedData savedData, T item) {
        var items = listAllAreas();
        items.add(item);

        return new BVHNode<>(savedData, items);
    }

    public BVHNode<T> without(AreaSavedData savedData, T item) {
        var items = listAllAreas();
        items.remove(item);

        return new BVHNode<>(savedData, items);
    }
}
