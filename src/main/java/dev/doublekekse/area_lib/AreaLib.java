package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.areas.BlockArea;
import dev.doublekekse.area_lib.command.AreaCommand;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public class AreaLib implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket.STREAM_CODEC);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            var gameMode = Minecraft.getInstance().gameMode;

            if (gameMode == null) {
                return;
            }

            if (gameMode.getPlayerMode().isSurvival()) {
                return;
            }

            var poseStack = context.matrixStack();
            poseStack.pushPose();

            var cPos = context.camera().getPosition();
            poseStack.translate(-cPos.x, -cPos.y, -cPos.z);


            var savedData = AreaClientData.getClientLevelData();

            if (savedData != null) {
                savedData.getAreas().forEach(area -> {
                    area.render(context, poseStack);
                });
            }

            poseStack.popPose();
        });

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
}
