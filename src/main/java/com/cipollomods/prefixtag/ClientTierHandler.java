package com.cipollomods.prefixtag;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

/**
 * ClientTierHandler
 *
 * Gestiona los eventos exclusivos del lado cliente:
 *   - Lanzar la GUI de selección de tier al entrar por primera vez
 *   - Modificar la nametag encima de la cabeza de cada jugador
 *
 * Esta clase está anotada con @OnlyIn(Dist.CLIENT) para garantizar
 * que el servidor nunca intente cargar código de renderizado,
 * lo que causaría un crash en servidores dedicados.
 */

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID, value = Dist.CLIENT)
public class ClientTierHandler {

    // ─ Login en cliente: lanzar GUI si es necesario -------------------------

    /**
     * Se ejecuta cuando el cliente se conecta a un servidor.
     * Espera 2 segundos para asegurarse de que el servidor haya terminado
     * de cargar los datos NBT del jugador antes de consultarlos.
     * Si el jugador no tiene ambos tiers asignados, abre la pantalla de selección.
     */

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        // Pequeño delay para que el servidor cargue los datos primero
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.execute(() -> {
                        // Abrir pantalla de selección si el jugador no tiene tiers
                        PlayerTierData data = TierEventHandler.getPlayerData(mc.player.getUUID());
                        if (data != null && !data.isFullyAssigned()) {
                            mc.setScreen(new TierSelectionScreen(TierSelectionScreen.Mode.ROL));
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // ─ Nametag: mostrar prefijo encima de la cabeza ---------------------

    /**
     * Se ejecuta cada vez que Minecraft va a renderizar el nombre
     * encima de la cabeza de un jugador.
     * Añade el prefijo [Rx|Px] delante del nombre del jugador.
     * No fuerza ningún color para evitar conflictos con otros mods.
     */

    @SubscribeEvent
    public static void onRenderNameTag(net.minecraftforge.client.event.RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;

        PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
        if (data == null) return;

        Component newName = Component.literal(data.getPrefix() + " " + player.getName().getString());
        event.setContent(newName);
    }
}