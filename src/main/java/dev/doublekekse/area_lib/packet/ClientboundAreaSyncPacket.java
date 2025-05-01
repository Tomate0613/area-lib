package dev.doublekekse.area_lib.packet;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.*;

public record ClientboundAreaSyncPacket(AreaSavedData areaSavedData) implements FabricPacket {
    public static final PacketType<ClientboundAreaSyncPacket> TYPE = PacketType.create(AreaLib.id("clientbound_area_sync_packet"), ClientboundAreaSyncPacket::new);

    private ClientboundAreaSyncPacket(FriendlyByteBuf buf) {
        this(AreaSavedData.load(Objects.requireNonNull(buf.readNbt())));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        var tag = new CompoundTag();

        areaSavedData.save(tag);

        buf.writeNbt(tag);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(ClientboundAreaSyncPacket packet, LocalPlayer player, PacketSender responseSender) {
        AreaClientData.setInstance(packet.areaSavedData);
    }
}
