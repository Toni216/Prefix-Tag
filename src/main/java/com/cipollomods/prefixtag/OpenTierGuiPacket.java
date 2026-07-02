package com.cipollomods.prefixtag;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
/***
 * Esto indica al cliente que debe abrir la pantalla
 * en la selección de tier
 *
 * Se envía cuando el jugador entra al servidor sin tener ambos tiers asignados.
 */
public class OpenTierGuiPacket {
    // Constructor vacío porqque no necesita datos
    public OpenTierGuiPacket() {}

    // No se escriben datos en el buffer
    public void encode(FriendlyByteBuf buf) {}

   // Devuelve una instancia vacía
    public static OpenTierGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenTierGuiPacket();
    }

    // Se ejecuta en el hilo principal del cliente al recibir el paquete
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandler::openTierGui)
        );
        ctx.setPacketHandled(true);
    }
}