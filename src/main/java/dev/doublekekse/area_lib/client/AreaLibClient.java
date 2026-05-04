package dev.doublekekse.area_lib.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

@ApiStatus.Internal
public class AreaLibClient implements ClientModInitializer {
    static boolean renderAreas = false;
    public static final GizmoStyle DEFAULT_GIZMO_STYLE = GizmoStyle.stroke(0xffffffff);

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket::handlePlay);
        ClientConfigurationNetworking.registerGlobalReceiver(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket::handleConfigure);

        LevelRenderEvents.BEFORE_GIZMOS.register((context) -> {
            if (!renderAreas) {
                return;
            }

            var level = Minecraft.getInstance().level;

            if (level == null) {
                return;
            }

            var dimension = level.dimension().identifier();

            var gameMode = Minecraft.getInstance().gameMode;

            if (gameMode == null) {
                return;
            }

            if (gameMode.getPlayerMode().isSurvival()) {
                return;
            }

            var poseStack = context.poseStack();

            if (poseStack == null) {
                return;
            }

            poseStack.pushPose();

            var cPos = context.levelState().cameraRenderState.pos;
            poseStack.translate(-cPos.x, -cPos.y, -cPos.z);


            var savedData = AreaClientData.getClientLevelData();

            if (savedData != null) {
                savedData.getAreas().forEach(area -> {
                    area.render(context, poseStack, dimension);
                });
            }

            poseStack.popPose();
        });

        // TODO: Figure out how to not place it at the very top
        var keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "area_lib.key.toggle_areas",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            new KeyMapping.Category(AreaLib.id("area_lib"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.consumeClick()) {
                renderAreas = !renderAreas;

                client.getChatListener().handleOverlay(Component.translatable("area_lib.key.toggle_areas." + (renderAreas ? "on" : "off")));
            }
        });
    }
}
