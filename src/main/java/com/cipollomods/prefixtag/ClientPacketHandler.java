package com.cipollomods.prefixtag;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Contiene el código de cliente que reacciona a los packets del servidor.
 *
 * Esta clase existe separada de {@link OpenTierGuiPacket} por una razón crítica:
 * cualquier clase que referencie {@link net.minecraft.client.gui.screens.Screen}
 * (directa o indirectamente, incluso dentro de una lambda) provoca que la JVM
 * intente cargar esa clase al verificar el bytecode del archivo que la contiene.
 * Si eso pasara el servidor dedicado crashearia.
 */
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

    /**
     * Abre la pantalla de selección de tier de Rol.
     * Llamado únicamente desde {@link OpenTierGuiPacket#handle} a través de
     * {@link net.minecraftforge.fml.DistExecutor#unsafeRunWhenOn}.
     */
    public static void openTierGui() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new TierSelectionScreen(TierSelectionScreen.Mode.ROL));
        }
    }
}