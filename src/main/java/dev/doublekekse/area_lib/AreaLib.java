package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.SphereArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.command.AreaCommand;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.duck.MinecraftServerDuck;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import dev.doublekekse.area_lib.registry.AreaTypeRegistry;
import dev.doublekekse.area_lib.registry.BuiltInAreaComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class AreaLib implements ModInitializer {
    @ApiStatus.Internal
    public static final String MOD_ID = "area_lib";
    @ApiStatus.Internal
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket.STREAM_CODEC);

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, _, _) -> {
                AreaCommand.register(dispatcher);
            }
        );

        ServerConfigurationConnectionEvents.CONFIGURE.register(((listener, server) -> {
            var savedData = getSavedData(server);
            ServerConfigurationNetworking.send(listener, new ClientboundAreaSyncPacket(savedData));
        }));

        AreaTypeRegistry.register(BoxArea::new, id("box"));
        AreaTypeRegistry.register(UnionArea::new, id("union"));
        AreaTypeRegistry.register(SphereArea::new, id("sphere"));

        BuiltInAreaComponents.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("area_lib", path);
    }

    public static Area getServerArea(MinecraftServer server, Identifier id) {
        return getSavedData(server).get(id);
    }

    public static Area getClientArea(Identifier id) {
        return AreaClientData.INSTANCE.get(id);
    }

    public static AreaSavedData getSavedData(Level level) {
        if (level.isClientSide()) {
            return AreaClientData.INSTANCE;
        } else {
            return getSavedData(level.getServer());
        }
    }

    public static AreaSavedData getSavedData(MinecraftServer server) {
        return ((MinecraftServerDuck) server).area_lib$getAreaSavedData();
    }

    @ApiStatus.Experimental
    public static void addListener(Consumer<AreaSavedData> listener) {
        AreaListeners.add(listener);
    }
}
