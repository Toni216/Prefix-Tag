package com.cipollomods.prefixtag;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * PrefixTag
 *
 * Clase principal del mod. Es el punto de entrada que Forge carga
 * al arrancar Minecraft.
 *
 * Se encarga de:
 *   - Definir el MOD_ID usado en todas las anotaciones del mod
 *   - Registrar el evento de setup del cliente para conectar la GUI
 *
 * El grueso de la lógica está repartido entre:
 *   - PlayerTierData      → almacenamiento de datos por jugador
 *   - TierEventHandler    → eventos de servidor (login, chat)
 *   - TierCommand         → comandos de admin e internos
 *   - TierSelectionScreen → GUI de selección de tier
 *   - ClientTierHandler   → eventos de cliente (login, nametag)
 */

@Mod(PrefixTag.MOD_ID)
public class PrefixTag {

    // Identificador único del mod. Debe coincidir con el modId en mods.toml
    public static final String MOD_ID = "prefixtag";

    public PrefixTag() {
        // Registrar el setup del cliente para conectar el evento de login de la GUI
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Se ejecuta durante la inicialización del cliente.
     * Registra el evento onClientLogin de ClientTierHandler en el bus de eventos,
     * lo que permite lanzar la GUI de selección cuando el jugador se conecta.
     */
    private void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ClientTierHandler::onClientLogin);
    }
}