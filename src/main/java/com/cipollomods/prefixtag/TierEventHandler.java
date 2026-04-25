package com.cipollomods.prefixtag;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Escucha eventos del servidor y actúa como capa de acceso a los datos de tier.
 * Es el núcleo del mod en el lado servidor, gestiona:
 *
 *   - Carga de datos NBT al hacer login y guardado al hacer logout
 *   - Mapa en memoria (UUID > {@link PlayerTierData}) para acceso rápido durante la sesión
 *   - Modificación del chat para anteponer el prefijo al nombre del jugador
 *   - Envío del {@link OpenTierGuiPacket} si el jugador no tiene tiers asignados
 *
 * El mapa en memoria evita leer NBT en cada evento de chat o nametag,
 * lo que sería muy costoso en servidores con muchos jugadores activos.
 */
@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID)
public class TierEventHandler {

    // Mapa en memoria: UUID del jugador → datos de tier activos en esta sesión.
    // Se popula en onPlayerLogin y se limpia en onPlayerLogout.
    private static final Map<UUID, PlayerTierData> tierMap = new HashMap<>();

    // Clave NBT usada para guardar los datos en el PersistentData del jugador.
    // ¡No cambiar entre versiones! Hacerlo borraría los datos de todos los jugadores.
    private static final String NBT_KEY = "PrefixTagData";

    /**
     * Se ejecuta cuando un jugador entra al servidor.
     *
     * Carga sus datos desde NBT si ya existen, o crea un registro vacío si es
     * la primera vez que entra. A continuación, si no tiene ambos tiers asignados,
     * envía un {@link OpenTierGuiPacket} para que el cliente abra la GUI de selección.
     *
     * El packet garantiza que la GUI se muestre correctamente tanto en LAN
     * como en servidores dedicados, sin depender de código del lado cliente.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistedData = player.getPersistentData();

        PlayerTierData data = persistedData.contains(NBT_KEY)
                ? PlayerTierData.load(persistedData.getCompound(NBT_KEY))
                : new PlayerTierData();

        tierMap.put(player.getUUID(), data);

        if (!data.isFullyAssigned()) {
            PacketHandler.sendToPlayer(new OpenTierGuiPacket(), player);
        }
    }

    /**
     * Se ejecuta cuando un jugador sale del servidor.
     * Persiste los datos actuales en NBT y los elimina del mapa en memoria.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerTierData data = tierMap.remove(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.save());
        }
    }

    /**
     * Se ejecuta cuando un jugador envía un mensaje al chat.
     * Antepone el prefijo [Rx|Px] y aplica el color de nombre configurado.
     * Ejemplo de resultado: §a● §r§f[§aR2§f|§6P3§f]§r §6Jugador§r: hola
     */
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerTierData data = tierMap.get(player.getUUID());
        if (data == null) return;

        String texto = data.getPrefix() + " "
                + data.getColorCode() + player.getName().getString()
                + "§r: " + event.getRawText();
        event.setMessage(Component.literal(texto));
    }

    /**
     * Devuelve los datos de tier de un jugador conectado por su UUID.
     * Devuelve {@code null} si el jugador no está en línea o no tiene datos.
     *
     * Usado por {@link TierCommand} y {@link ClientTierHandler}.
     *
     * @param uuid UUID del jugador
     * @return Sus datos de tier, o null si no está conectado
     */
    public static PlayerTierData getPlayerData(UUID uuid) {
        return tierMap.get(uuid);
    }

    /**
     * Persiste inmediatamente los datos del jugador en su NBT.
     * Se llama tras cualquier cambio de tier para evitar pérdida de datos
     * si el servidor se cierra inesperadamente antes del logout.
     *
     * @param player El jugador cuyos datos se van a guardar
     */
    public static void savePlayerData(ServerPlayer player) {
        PlayerTierData data = tierMap.get(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.save());
        }
    }
}