package dev.doublekekse.area_lib.collection;

import dev.doublekekse.area_lib.Area;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface AreaCollection {
    void add(Identifier areaId);
    void remove(Identifier areaId);
    void invalidate(Identifier areaId);

    Collection<Area> listAllAreas();

    boolean contains(Level level, Vec3 position);
    @Nullable AABB getBoundingBox();
}
