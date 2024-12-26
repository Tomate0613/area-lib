package dev.doublekekse.area_lib.areas;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class UnionArea extends CompositeArea {
    @Override
    public boolean contains(Level level, Vec3 position) {
        loadAreas(level);

        for (var area : areas) {
            if (area.contains(level, position)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "UnionArea " + super.toString();
    }
}
