package com.cipollomods.prefixtag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Gestiona el canal de red del mod para la comunicación servidor > cliente.
 * Todos los packets del mod se registran aquí durante el FMLCommonSetupEvent.
 */
public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    /** Canal de red del mod. Todos los packets pasan por aquí. */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PrefixTag.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // Contador incremental de IDs — cada tipo de packet necesita un ID único
    private static int packetId = 0;

    /**
     * Registra todos los packets del mod en el canal.
     */
    public static void register() {
        CHANNEL.messageBuilder(OpenTierGuiPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenTierGuiPacket::decode)
                .encoder(OpenTierGuiPacket::encode)
                .consumerMainThread(OpenTierGuiPacket::handle)
                .add();
    }

    /**
     * Envía un packet a un jugador concreto en el servidor.
     *
     * @param packet El packet a enviar
     * @param player El jugador destinatario
     */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}