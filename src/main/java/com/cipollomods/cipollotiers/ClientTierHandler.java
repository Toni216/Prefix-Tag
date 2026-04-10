package com.cipollomods.cipollotiers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = CipolloTiers.MOD_ID, value = Dist.CLIENT)
public class ClientTierHandler {

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
}