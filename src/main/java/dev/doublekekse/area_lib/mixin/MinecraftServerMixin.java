package dev.doublekekse.area_lib.mixin;

import com.mojang.datafixers.DataFixer;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.duck.MinecraftServerDuck;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerDuck {
    @Shadow
    public abstract SavedDataStorage getDataStorage();

    @Unique
    AreaSavedData areaSavedData;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(method = "<init>", at = @At("RETURN"))
    void init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, NotificationManager notificationManager, CallbackInfo ci) {
        var storage = getDataStorage();

        areaSavedData = storage.computeIfAbsent(AreaSavedData.TYPE);
        areaSavedData.setDirty();
    }

    @Override
    public AreaSavedData area_lib$getAreaSavedData() {
        return areaSavedData;
    }
}
