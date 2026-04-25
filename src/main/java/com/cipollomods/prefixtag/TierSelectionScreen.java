package com.cipollomods.prefixtag;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Pantalla de selección de tier que se muestra al jugador cuando entra
 * al servidor sin tener ambos tiers asignados, o tras un /tier reset.
 *
 * El flujo es siempre de dos pasos secuenciales:
 *   1. {@link Mode#ROL}  > el jugador elige su tier de Rol (R1–R5)
 *   2. {@link Mode#PVP}  > el jugador elige su tier de PvP (P1–P5)
 *
 * Al confirmar la selección de Rol se abre automáticamente la pantalla de PvP.
 * Al confirmar la selección de PvP la GUI se cierra y el jugador entra al mundo.
 *
 * La pantalla no se puede cerrar con Escape ({@link #shouldCloseOnEsc} = false)
 * para garantizar que el jugador siempre tenga ambos tiers asignados.
 *
 * Cada elección se envía al servidor via el comando /tierself,
 * que valida que el tier no esté ya asignado antes de guardarlo.
 */
public class TierSelectionScreen extends Screen {

    // ─ Modo ----------

    /** Define si la pantalla corresponde a la selección de Rol o de PvP. */
    public enum Mode { ROL, PVP }

    private final Mode mode;

    // ─ Constantes visuales ----------

    /** Color del título en formato ARGB. Dorado: 0xFFD700. */
    private static final int COLOR_TITLE = 0xFFD700;

    /** Color de la descripción en formato ARGB. Gris claro: 0xAAAAAA. */
    private static final int COLOR_DESC  = 0xAAAAAA;

    /** Ancho de cada botón de tier en píxeles. */
    private static final int BTN_W = 80;

    /** Alto de cada botón de tier en píxeles. */
    private static final int BTN_H = 20;

    /** Espacio horizontal entre botones en píxeles. */
    private static final int BTN_GAP = 10;

    // ─ Constructor --------------

    /**
     * @param mode {@link Mode#ROL} para la pantalla de Rol, {@link Mode#PVP} para la de PvP
     */
    public TierSelectionScreen(Mode mode) {
        super(Component.literal(mode == Mode.ROL
                ? PrefixTagConfig.ROL_GUI_TITLE.get()
                : PrefixTagConfig.PVP_GUI_TITLE.get()));
        this.mode = mode;
    }

    // ─ Comportamiento --------------

    /** Impide cerrar la pantalla con Escape. El jugador debe elegir un tier. */
    @Override
    public boolean shouldCloseOnEsc() { return false; }

    /** Impide pausar el juego en singleplayer mientras la pantalla está abierta. */
    @Override
    public boolean isPauseScreen() { return false; }

    // ─ Inicialización ------------

    /**
     * Crea y posiciona los 5 botones de selección de tier, centrados horizontalmente.
     * Se llama automáticamente al abrir la pantalla y al redimensionar la ventana.
     */
    @Override
    protected void init() {
        int totalWidth = 5 * BTN_W + 4 * BTN_GAP;
        int startX = this.width / 2 - totalWidth / 2;
        int startY = this.height / 2 - BTN_H / 2;

        for (int i = 1; i <= 5; i++) {
            final int tier = i;
            int x = startX + (i - 1) * (BTN_W + BTN_GAP);
            String label = mode == Mode.ROL
                    ? PrefixTagConfig.getRolLabel(tier)
                    : PrefixTagConfig.getPvpLabel(tier);

            this.addRenderableWidget(Button.builder(
                    Component.literal(label),
                    btn -> onTierSelected(tier)
            ).pos(x, startY).size(BTN_W, BTN_H).build());
        }
    }

    // ─ Render ----------

    /**
     * Dibuja el fondo, el título y la descripción en cada frame.
     * La descripción tiene wrap automático para adaptarse al ancho de la ventana.
     *
     * Llamado en cada frame — mantener el código eficiente.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        String title = mode == Mode.ROL ? PrefixTagConfig.ROL_GUI_TITLE.get() : PrefixTagConfig.PVP_GUI_TITLE.get();
        String desc  = mode == Mode.ROL ? PrefixTagConfig.ROL_GUI_DESC.get()  : PrefixTagConfig.PVP_GUI_DESC.get();

        // Título centrado, 50px por encima del centro
        graphics.drawCenteredString(this.font, title, this.width / 2, this.height / 2 - 50, COLOR_TITLE);

        // Descripción con wrap automático, 30px por encima del centro
        int descY    = this.height / 2 - 30;
        int maxWidth = this.width - 60; // 30px de margen a cada lado
        for (FormattedCharSequence line : this.font.split(Component.literal(desc), maxWidth)) {
            graphics.drawString(this.font, line, (this.width - this.font.width(line)) / 2, descY, COLOR_DESC);
            descY += 10;
        }
    }

    // ─ Selección ------------------

    /**
     * Se ejecuta al pulsar un botón de tier.
     * Envía el tier elegido al servidor via /tierself y avanza al siguiente paso.
     *
     * @param tier El número de tier elegido (1–5)
     */
    private void onTierSelected(int tier) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        if (mode == Mode.ROL) {
            this.minecraft.player.connection.sendCommand("tierself rol " + tier);
            this.minecraft.setScreen(new TierSelectionScreen(Mode.PVP));
        } else {
            this.minecraft.player.connection.sendCommand("tierself pvp " + tier);
            this.minecraft.setScreen(null);
        }
    }
}