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
    private final List<Area> nodeAreas;

    public BVHNode(List<Area> areas) {
        this.boundingBox = areas.stream()
            .map(Area::getBoundingBox)
            .filter(Objects::nonNull)
            .reduce(AABBUtils::encapsulate)
            .orElseThrow();

        if (areas.size() <= 2) {
            this.nodeAreas = areas;
        } else {
            var boundingBoxVolume = AABBUtils.volumeOf(boundingBox);

            var dominant = new ArrayList<Area>();
            var normal = new ArrayList<Area>();
            for (var a : areas) {
                var box = a.getBoundingBox();
                if (box == null) continue;

                var volumeFraction = AABBUtils.volumeOf(box) / boundingBoxVolume;

                if (volumeFraction > 0.9) {
                    dominant.add(a);
                } else {
                    normal.add(a);
                }
            }


            if (normal.size() <= 2) {
                this.nodeAreas = areas;
                return;
            }

            var longestAxis = AABBUtils.longestAxis(boundingBox);
            var sorted = normal.stream().sorted(Comparator.comparingDouble(a -> Objects.requireNonNull(a.getBoundingBox()).getCenter().get(longestAxis))).toList();
            int mid = normal.size() / 2;

            this.nodeAreas = dominant.isEmpty() ? null : dominant;

            this.left = new BVHNode(sorted.subList(0, mid));
            this.right = new BVHNode(sorted.subList(mid, sorted.size()));
        }
    }

    public boolean contains(Level level, Vec3 position) {
        if (!boundingBox.contains(position)) {
            return false;
        }

        if (nodeAreas != null) {
            for (var area : nodeAreas) {
                if (area.contains(level, position)) {
                    return true;
                }
            }
        }

        return (left != null && left.contains(level, position)) ||
            (right != null && right.contains(level, position));
    }

    private void findAreasContaining(Collection<Area> collection, Level level, Vec3 position) {
        if (!boundingBox.contains(position)) {
            return;
        }

        if (nodeAreas != null) {
            for (final var area : nodeAreas) {
                if (area.contains(level, position)) {
                    collection.add(area);
                }
            }
        }

        if (left != null) {
            left.findAreasContaining(collection, level, position);
        }
        if (right != null) {
            right.findAreasContaining(collection, level, position);
        }
    }

    private void findAreasContaining(Collection<Area> collection, Level level, Vec3 position, Predicate<Area> predicate) {
        if (!boundingBox.contains(position)) {
            return;
        }

        if (nodeAreas != null) {
            for (final var area : nodeAreas) {
                if (area.contains(level, position) && predicate.test(area)) {
                    collection.add(area);
                }
            }
        }

        if (left != null) {
            left.findAreasContaining(collection, level, position, predicate);
        }
        if (right != null) {
            right.findAreasContaining(collection, level, position, predicate);
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

        if (nodeAreas != null) {
            allAreas.addAll(nodeAreas);
        }

        if (left != null) {
            allAreas.addAll(left.listAllAreas());
        }
        if (right != null) {
            allAreas.addAll(right.listAllAreas());
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
