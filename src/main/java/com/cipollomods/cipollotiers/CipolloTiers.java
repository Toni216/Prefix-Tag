package com.cipollomods.cipollotiers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CipolloTiers.MOD_ID)
public class CipolloTiers {

    public static final String MOD_ID = "cipollotiers";

    public CipolloTiers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Registrar el evento de login en cliente para lanzar la GUI
        MinecraftForge.EVENT_BUS.addListener(ClientTierHandler::onClientLogin);
    }
}