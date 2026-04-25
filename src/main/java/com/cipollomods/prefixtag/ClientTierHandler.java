package com.cipollomods.prefixtag;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Gestiona los eventos exclusivos del lado cliente.
 * Actualmente su única responsabilidad es modificar la nametag
 * que aparece encima de la cabeza de cada jugador para mostrar el prefijo.
 *
 * La apertura de la GUI de selección de tier al hacer login se gestiona
 * en {@link OpenTierGuiPacket}, que recibe la señal directamente del servidor.
 * Esto garantiza compatibilidad con servidores dedicados.
 *
 * Anotada con {@link OnlyIn}(Dist.CLIENT) para que el servidor nunca
 * intente cargar código de renderizado, lo que causaría un crash.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID, value = Dist.CLIENT)
public class ClientTierHandler {

    // Nametag ------

    /**
     * Se ejecuta cada vez que Minecraft va a renderizar el nombre encima
     * de la cabeza de una entidad. Si la entidad es un jugador con datos
     * de tier disponibles, reemplaza el nombre con el prefijo + color configurado.
     *
     * Este evento se llama por cada jugador visible en cada frame,
     * por lo que el código aquí debe ser lo más ligero posible.
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
        if (data == null) return;

        event.setContent(Component.literal(
                data.getPrefix() + " " + data.getColorCode() + player.getName().getString() + "§r"
        ));
    }
}