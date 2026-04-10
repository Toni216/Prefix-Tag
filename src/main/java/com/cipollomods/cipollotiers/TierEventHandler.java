package com.cipollomods.cipollotiers;

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

@Mod.EventBusSubscriber(modid = CipolloTiers.MOD_ID)
public class TierEventHandler {

    // Mapa en memoria: UUID del jugador → sus datos de tier
    private static final Map<UUID, PlayerTierData> tierMap = new HashMap<>();

    // ── Clave NBT para guardar en el jugador ──────────────────────────────────
    private static final String NBT_KEY = "CipolloTierData";

    // ── Al entrar al servidor ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistedData = player.getPersistentData();

        if (persistedData.contains(NBT_KEY)) {
            // Jugador conocido: cargar datos
            PlayerTierData data = PlayerTierData.load(persistedData.getCompound(NBT_KEY));
            tierMap.put(player.getUUID(), data);
        } else {
        // Jugador nuevo: crear datos vacíos
        PlayerTierData data = new PlayerTierData();
        tierMap.put(player.getUUID(), data);
        }
    }

    // ── Al salir del servidor ─────────────────────────────────────────────────

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

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerTierData data = tierMap.get(player.getUUID());

        if (data != null) {
            String prefix = data.getPrefix();
            Component newMessage = Component.literal(prefix + " §f" + player.getName().getString() + ": " + event.getRawText());
            event.setMessage(newMessage);
        }
    }

    // ── Métodos de acceso para otros archivos ─────────────────────────────────

    public static PlayerTierData getPlayerData(UUID uuid) {
        return tierMap.get(uuid);
    }

    public static void savePlayerData(ServerPlayer player) {
        PlayerTierData data = tierMap.get(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.save());
        }
    }
}
