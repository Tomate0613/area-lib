package dev.doublekekse.area_lib.packet;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ApiStatus.Internal
public record ClientboundAreaSyncPacket(AreaSavedData areaSavedData) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ClientboundAreaSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundAreaSyncPacket::write, ClientboundAreaSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundAreaSyncPacket> TYPE = new CustomPacketPayload.Type<>(AreaLib.id("clientbound_area_sync_packet"));

    private ClientboundAreaSyncPacket(FriendlyByteBuf buf) {
        this(AreaSavedData.load(Objects.requireNonNull(buf.readNbt())));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(areaSavedData.save());
    }

    @Environment(EnvType.CLIENT)
    private void handle() {
        AreaClientData.setInstance(areaSavedData);
    }

    @Environment(EnvType.CLIENT)
    public static void handlePlay(ClientboundAreaSyncPacket packet, ClientPlayNetworking.Context context) {
        packet.handle();
    }

    @Environment(EnvType.CLIENT)
    public static void handleConfigure(ClientboundAreaSyncPacket packet, ClientConfigurationNetworking.Context context) {
        packet.handle();
    }
}
