package dev.doublekekse.area_lib.client;

import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

public class AreaLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket::handle);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            var gameMode = Minecraft.getInstance().gameMode;

            if (gameMode == null) {
                return;
            }

            if (gameMode.getPlayerMode().isSurvival()) {
                return;
            }

            var poseStack = context.matrixStack();

            if (poseStack == null) {
                return;
            }

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
    }
}
