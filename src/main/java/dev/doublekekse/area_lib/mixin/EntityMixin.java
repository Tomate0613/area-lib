package dev.doublekekse.area_lib.mixin;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.duck.EntityDuck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuck {
    @Shadow
    private Level level;
    @Shadow
    private Vec3 position;

    @Unique
    private List<Area> areas;

    @Override
    public Collection<Area> area_lib$getAreas(AreaSavedData savedData) {
        if (areas == null) {
            areas = savedData.getEntityTrackedAreas(this.level, this.position);
        }

        return areas;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        areas = null;
    }
}
