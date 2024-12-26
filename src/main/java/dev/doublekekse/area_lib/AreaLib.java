package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.areas.BlockArea;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.command.AreaCommand;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class AreaLib implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket.STREAM_CODEC);

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                AreaCommand.register(dispatcher);
            }
        );

        ServerPlayConnectionEvents.JOIN.register((listener, packetSender, server) -> {
            var savedData = AreaSavedData.getServerData(server);
            packetSender.sendPacket(new ClientboundAreaSyncPacket(savedData));
        });

        AreaTypeRegistry.register(BlockArea.class, id("block"));
        AreaTypeRegistry.register(BoxArea.class, id("box"));
        AreaTypeRegistry.register(UnionArea.class, id("union"));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("area_lib", path);
    }

    public static Area getServerArea(MinecraftServer server, ResourceLocation id) {
        return AreaSavedData.getServerData(server).get(id);
    }

    public static Area getClientArea(ResourceLocation id) {
        return AreaClientData.getClientLevelData().get(id);
    }

    public static AreaSavedData getSavedData(Level level) {
        if(level.isClientSide) {
            return AreaClientData.getClientLevelData();
        } else {
            return AreaSavedData.getServerData(Objects.requireNonNull(level.getServer()));
        }
    }
}
