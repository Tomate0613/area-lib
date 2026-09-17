package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.Area;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface DerivedArea {
    Iterable<Area> getDependencies();

    void invalidateDependency(Identifier id);

    default boolean hasDependency(Area area) {
        for (var dependency : getDependencies()) {
            if (dependency.getId().equals(area.getId())) {
                return true;
            }
        }

        return false;
    }
}
