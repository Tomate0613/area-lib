package dev.doublekekse.area_lib.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AreaLibClient implements ClientModInitializer {
    static boolean renderAreas = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket::handle);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            if (!renderAreas) {
                return;
            }

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

        var keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "area_lib.key.toggle_areas",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "area_lib.category.area_lib"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.consumeClick()) {
                renderAreas = !renderAreas;
            }
        });
    }
}
