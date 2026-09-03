package dev.doublekekse.area_lib.exception;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.areas.DerivedArea;
import org.jetbrains.annotations.ApiStatus;

public class AreaInUseException extends RuntimeException {
    @ApiStatus.Experimental
    public DerivedArea other;

    public AreaInUseException(Area area, DerivedArea other) {
        super("Area " + area.toString() + " is dependency of " + other.toString());

        this.other = other;
    }
}
