package dev.doublekekse.area_lib.client;

import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AreaLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAreaSyncPacket.TYPE, ClientboundAreaSyncPacket::handle);
    }
}
