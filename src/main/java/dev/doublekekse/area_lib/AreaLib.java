package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.SphereArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.command.AreaCommand;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.command.argument.CompositeAreaArgument;
import dev.doublekekse.area_lib.command.argument.ListArgument;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import dev.doublekekse.area_lib.registry.AreaTypeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class AreaLib implements ModInitializer {
    public static final ListArgument<ResourceLocation> AREA_LIST_ARGUMENT = ListArgument.create(AreaArgument.area());

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

        AreaTypeRegistry.register(BoxArea::new, id("box"));
        AreaTypeRegistry.register(UnionArea::new, id("union"));
        AreaTypeRegistry.register(SphereArea::new, id("sphere"));

        ArgumentTypeRegistry.registerArgumentType(id("area"), AreaArgument.class, SingletonArgumentInfo.contextFree(AreaArgument::area));
        ArgumentTypeRegistry.registerArgumentType(id("composite_area"), CompositeAreaArgument.class, SingletonArgumentInfo.contextFree(CompositeAreaArgument::area));
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
        if (level.isClientSide) {
            return AreaClientData.getClientLevelData();
        } else {
            return AreaSavedData.getServerData(Objects.requireNonNull(level.getServer()));
        }
    }
}
