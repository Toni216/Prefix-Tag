package com.cipollomods.prefixtag;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet servidor > cliente que indica al cliente que debe abrir
 * la pantalla de selección de tier ({@link TierSelectionScreen}).
 *
 * Se envía desde {@link TierEventHandler#onPlayerLogin} cuando el jugador
 * entra al servidor sin tener ambos tiers asignados.
 *
 * No transporta datos — es una señal pura. El cliente siempre
 * abre la pantalla de Rol ({@link TierSelectionScreen.Mode#ROL}) al recibirlo,
 * y desde ahí el flujo continúa a PvP automáticamente.
 */
public class OpenTierGuiPacket {

    /** Constructor vacío — el packet no necesita datos. */
    public OpenTierGuiPacket() {}

    /** No hay datos que escribir en el buffer. */
    public void encode(FriendlyByteBuf buf) {}

    /** No hay datos que leer del buffer. Devuelve una instancia vacía. */
    public static OpenTierGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenTierGuiPacket();
    }

    /**
     * Se ejecuta en el hilo principal del cliente al recibir el packet.
     * Abre la pantalla de selección de Rol.
     *
     * {@link DistExecutor#unsafeRunWhenOn} garantiza que el código de GUI
     * nunca se ejecuta en el servidor, evitando crashes en entornos dedicados.
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.setScreen(new TierSelectionScreen(TierSelectionScreen.Mode.ROL));
                    }
                })
        );
        ctx.setPacketHandled(true);
    }
}