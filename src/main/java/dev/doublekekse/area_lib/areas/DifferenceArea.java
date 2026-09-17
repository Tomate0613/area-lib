package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DifferenceArea extends Area implements DerivedArea {
    @ApiStatus.Internal
    public static final Identifier IDENTIFIER = AreaLib.id("difference");

    private Identifier minuendId;
    private Identifier subtrahendId;

    private Area minuend;
    private Area subtrahend;

    public DifferenceArea(AreaSavedData savedData, Identifier id, Identifier minuendId, Identifier subtrahendId) {
        super(savedData, id);

        this.minuendId = minuendId;
        this.subtrahendId = subtrahendId;
    }

    public DifferenceArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    private Area minuend() {
        if (this.minuend == null) {
            this.minuend = this.savedData.get(this.minuendId);
        }

        return this.minuend;
    }

    private Area subtrahend() {
        if (this.subtrahend == null) {
            this.subtrahend = this.savedData.get(this.subtrahendId);
        }

        return this.subtrahend;
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        return minuend().contains(level, position) && !subtrahend().contains(level, position);
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        return minuend().getBoundingBox();
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        minuendId = Identifier.parse(compoundTag.getString("minuend").get());
        subtrahendId = Identifier.parse(compoundTag.getString("subtrahend").get());
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        compoundTag.putString("minuend", minuendId.toString());
        compoundTag.putString("subtrahend", subtrahendId.toString());

        return compoundTag;
    }

    @Override
    public Identifier getType() {
        return IDENTIFIER;
    }

    @Override
    public Iterable<Area> getDependencies() {
        return List.of(minuend(), subtrahend());
    }

    @Override
    public void invalidateDependency(Identifier id) {
        if (id.equals(minuendId)) {
            this.minuend = null;
        }

        if (id.equals(subtrahendId)) {
            this.subtrahend = null;
        }
    }
}
