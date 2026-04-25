package com.cipollomods.prefixtag;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Clase principal del mod y punto de entrada que Forge carga al arrancar Minecraft.
 * Su responsabilidad es mínima: registrar la configuración y el canal de red.
 * El grueso de la lógica está distribuido entre las clases especializadas:
 *
 *   - {@link PlayerTierData}      > modelo de datos de tier por jugador (NBT)
 *   - {@link TierEventHandler}    > eventos de servidor: login, logout, chat
 *   - {@link TierCommand}         > comandos /tier y /tierself
 *   - {@link TierSelectionScreen} > GUI de selección de tier (cliente)
 *   - {@link ClientTierHandler}   > eventos de cliente: nametag
 *   - {@link PacketHandler}       > registro del canal de red servidor → cliente
 *   - {@link OpenTierGuiPacket}   > packet que ordena al cliente abrir la GUI
 *   - {@link PrefixTagConfig}     > configuración via Forge Config API (prefixtag.toml)
 */
@Mod(PrefixTag.MOD_ID)
public class PrefixTag {

    public static final String MOD_ID = "prefixtag";

    public PrefixTag() {
        // Registrar la configuración COMMON — genera prefixtag.toml en config/
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PrefixTagConfig.SPEC, "prefixtag.toml");

        // Registrar el canal de red durante el setup, no en el constructor,
        // para garantizar que Forge ha preparado el entorno de red.
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Se ejecuta durante la inicialización común (cliente y servidor).
     * Registra el canal de red y todos los packets del mod.
     */
    private void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.register();
    }
}