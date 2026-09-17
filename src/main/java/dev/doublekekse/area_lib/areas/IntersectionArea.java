package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.collection.AbstractAreaCollection;
import dev.doublekekse.area_lib.collection.AreaIntersection;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

public class IntersectionArea extends CompositeArea {
    @ApiStatus.Internal
    public static final Identifier IDENTIFIER = AreaLib.id("intersection");

    public IntersectionArea(AreaSavedData savedData, Identifier id, Collection<Identifier> areas) {
        super(savedData, id, new AreaIntersection(savedData, areas));
    }

    public IntersectionArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    @Override
    AbstractAreaCollection newCollection(AreaSavedData savedData) {
        return new AreaIntersection(savedData);
    }

    @Override
    public Identifier getType() {
        return IDENTIFIER;
    }
}
