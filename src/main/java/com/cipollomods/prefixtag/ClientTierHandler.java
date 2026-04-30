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
 * en {@link OpenTierGuiPacket}
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
     * de tier disponibles, reemplaza el nombre con el prefijo + nombre de display.
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
        if (data == null) return;

        // Usa el nombre personalizado si existe, o el username real si no
        String nombre = data.getDisplayName(player.getName().getString());

        event.setContent(Component.literal(
                data.getPrefix() + " " + data.getColorCode() + nombre + "§r"
        ));
    }
}