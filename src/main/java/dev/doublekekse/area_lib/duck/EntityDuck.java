package dev.doublekekse.area_lib.duck;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface EntityDuck {
    Collection<Area> area_lib$getAreas(AreaSavedData savedData);
}
