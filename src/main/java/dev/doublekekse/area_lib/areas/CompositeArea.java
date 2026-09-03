package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.collection.AbstractAreaCollection;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Experimental
public abstract class CompositeArea extends Area implements DerivedArea {
    private AbstractAreaCollection areas;

    public CompositeArea(AreaSavedData savedData, Identifier id, AbstractAreaCollection areas) {
        super(savedData, id);

        this.areas = areas;
    }

    public CompositeArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        return areas.contains(level, position);
    }

    public void addDependency(MinecraftServer server, Area area) {
        if (area instanceof DerivedArea) {
            throw new IllegalArgumentException("Sub areas may not be derived areas");
        }

        areas.add(area.getId());
        invalidate(server);
    }

    public void removeDependency(MinecraftServer server, Area area) {
        areas.remove(area.getId());
        invalidate(server);
    }

    @Override
    public Iterable<Area> getDependencies() {
        return areas.listAllAreas();
    }

    @Override
    public void invalidateDependency(Identifier id) {
        areas.invalidate(id);
    }

    abstract AbstractAreaCollection newCollection(AreaSavedData savedData);

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        areas = newCollection(savedData);
        areas.load(compoundTag.getCompound("areas").get());
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        compoundTag.put("areas", areas.save());

        return compoundTag;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        return areas.getBoundingBox();
    }
}
