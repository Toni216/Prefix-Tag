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
 * TierEventHandler
 *
 * Escucha eventos del servidor relacionados con jugadores y chat.
 * Es el núcleo del mod en el lado servidor — gestiona:
 *   - Carga y guardado de datos de tier via NBT
 *   - Mapa en memoria para acceso rápido durante la sesión
 *   - Modificación del chat para añadir el prefijo
 *
 * Los datos se mantienen en memoria mientras el jugador está conectado
 * y se persisten en NBT al desconectarse.
 */

@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID)
public class TierEventHandler {

    // Mapa en memoria: UUID del jugador → sus datos de tier
    // Se usa para evitar leer NBT en cada evento, que sería muy lento
    private static final Map<UUID, PlayerTierData> tierMap = new HashMap<>();

    // Clave con la que se guardan los datos en el PersistentData del jugador
    // Cambiar esta clave en una versión futura haría perder los datos guardados
    private static final String NBT_KEY = "PrefixTagData";

    // ── Al entrar al servidor ─────────────────────────────────────────────────

    /**
     * Se ejecuta cuando un jugador entra al servidor.
     * Si ya tiene datos NBT guardados, los carga al mapa en memoria.
     * Si es la primera vez que entra, crea un registro vacío.
     * La GUI de selección se lanza desde el cliente (ClientTierHandler).
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistedData = player.getPersistentData();

        if (persistedData.contains(NBT_KEY)) {
            // Jugador conocido: cargar datos desde NBT
            PlayerTierData data = PlayerTierData.load(persistedData.getCompound(NBT_KEY));
            tierMap.put(player.getUUID(), data);
        } else {
            // Jugador nuevo: crear datos vacíos (-1)
            PlayerTierData data = new PlayerTierData();
            tierMap.put(player.getUUID(), data);
        }
    }

    // ── Al salir del servidor ─────────────────────────────────────────────────

    /**
     * Se ejecuta cuando un jugador sale del servidor.
     * Guarda los datos actuales en NBT y los elimina del mapa en memoria.
     * Esto garantiza que los tiers persisten entre sesiones.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerTierData data = tierMap.get(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.save());
            tierMap.remove(player.getUUID());
        }
    }

    // ── En el chat: añadir prefijo ────────────────────────────────────────────

    /**
     * Se ejecuta cada vez que un jugador envía un mensaje al chat.
     * Modifica el mensaje para añadir el prefijo [Rx|Px] delante del nombre.
     * Ejemplo: [R2|P3] Jugador: hola
     * No fuerza ningún color para evitar conflictos con otros mods.
     */
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerTierData data = tierMap.get(player.getUUID());

        if (data != null) {
            String prefix = data.getPrefix();
            Component newMessage = Component.literal(prefix + " " + player.getName().getString() + ": " + event.getRawText());
            event.setMessage(newMessage);
        }
    }

    // ── Métodos de acceso para otros archivos -----------------------------------
    /**
     * Devuelve los datos de tier de un jugador por su UUID.
     * Devuelve null si el jugador no está conectado o no tiene datos.
     * Usado por TierCommand y ClientTierHandler.
     */
    public static PlayerTierData getPlayerData(UUID uuid) {
        return tierMap.get(uuid);
    }

    /**
     * Guarda los datos actuales del jugador en su NBT persistente.
     * Se llama después de cualquier cambio de tier para garantizar
     * que los datos no se pierdan si el servidor se cierra inesperadamente.
     */
    public static void savePlayerData(ServerPlayer player) {
        PlayerTierData data = tierMap.get(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.save());
        }
    }
}
