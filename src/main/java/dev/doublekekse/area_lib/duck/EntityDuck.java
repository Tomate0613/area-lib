package dev.doublekekse.area_lib.duck;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;

import java.util.Collection;

public interface EntityDuck {
    Collection<Area> area_lib$getAreas(AreaSavedData savedData);
}
