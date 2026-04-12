package com.cipollomods.prefixtag;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Pantalla de selección de tier que se muestra al jugador
 * cuando entra al servidor por primera vez o tras un reset.
 *
 * El proceso es de dos pasos:
 *   1. Pantalla de Rol  (Mode.ROL) → el jugador elige R1–R5
 *   2. Pantalla de PvP  (Mode.PVP) → el jugador elige P1–P5
 *
 * Al elegir en la pantalla de Rol, se abre automáticamente la de PvP.
 * Al elegir en la pantalla de PvP, la GUI se cierra y el jugador entra al mundo.
 *
 * La pantalla no se puede cerrar con Escape para garantizar
 * que el jugador siempre tenga ambos tiers asignados.
 */
public class TierSelectionScreen extends Screen {

    // ─ Modo de la pantalla -------------------------------------------

    /** Define si la pantalla es para elegir Rol o PvP */
    public enum Mode { ROL, PVP }
    private final Mode mode;

    // ─ Colores -----------------------------------------

    /** Color del título en formato ARGB hexadecimal */
    private static final int COLOR_TITLE = 0xFFD700; // Dorado

    /** Color de la descripción en formato ARGB hexadecimal */
    private static final int COLOR_DESC  = 0xAAAAAA; // Gris claro

    // ─ Constructor --------------------------------------------------------

    /**
     * @param mode ROL para la pantalla de Rol, PVP para la de PvP
     */
    public TierSelectionScreen(Mode mode) {
        super(Component.literal(mode == Mode.ROL
                ? PrefixTagConfig.ROL_GUI_TITLE.get()
                : PrefixTagConfig.PVP_GUI_TITLE.get()));
        this.mode = mode;
    }

    // ─ Métodos de texto desde config ──────────────────────────────────────────

    /**
     * Devuelve el título correspondiente al modo actual desde la config.
     */
    public String getRolOrPvpTitle() {
        return mode == Mode.ROL
                ? PrefixTagConfig.ROL_GUI_TITLE.get()
                : PrefixTagConfig.PVP_GUI_TITLE.get();
    }

    /**
     * Devuelve la descripción correspondiente al modo actual desde la config.
     */
    public String getRolOrPvpDesc() {
        return mode == Mode.ROL
                ? PrefixTagConfig.ROL_GUI_DESC.get()
                : PrefixTagConfig.PVP_GUI_DESC.get();
    }

    // ─ Comportamiento de la pantalla -------------------------------------------

    /**
     * Impide cerrar la pantalla con la tecla Escape.
     * El jugador debe elegir un tier obligatoriamente.
     */
    @Override
    public boolean shouldCloseOnEsc() { return false; }

    /**
     * Impide que la pantalla pause el juego en singleplayer.
     * En servidores esto no tiene efecto.
     */
    @Override
    public boolean isPauseScreen() { return false; }

    // ─ Inicializar botones ----------------------------------------------

    /**
     * Crea y posiciona los 5 botones de selección de tier.
     * Los botones se centran horizontalmente en la pantalla.
     * Se llama automáticamente al abrir la pantalla y al redimensionar la ventana.
     */
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY  = this.height / 2 - 10;
        int btnW    = 80;
        int btnH    = 20;
        int gap     = 10; // Espacio entre botones
        int total   = 5 * btnW + 4 * gap; // Ancho total de los 5 botones + huecos
        int startX  = centerX - total / 2; // X de inicio para centrar el conjunto

        // Prefijo del botón según el modo: etiqueta de config para cada tier
        for (int i = 1; i <= 5; i++) {
            final int tier = i;
            int x = startX + (i - 1) * (btnW + gap);

            // Etiqueta del botón viene de la config (ej. "R1" o lo que configure el admin)
            String label = mode == Mode.ROL
                    ? PrefixTagConfig.getRolLabel(tier)
                    : PrefixTagConfig.getPvpLabel(tier);

            this.addRenderableWidget(Button.builder(
                    Component.literal(label),
                    btn -> onTierSelected(tier)
            ).pos(x, startY).size(btnW, btnH).build());
        }
    }

    // ─ Render ------------------------------------------------------------

    /**
     * Dibuja el fondo, el título y la descripción de la pantalla.
     * Se llama en cada frame — el código aquí debe ser eficiente.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título y descripción vienen de la config
        String title = getRolOrPvpTitle();
        String desc  = getRolOrPvpDesc();

        // Título centrado en dorado
        graphics.drawCenteredString(this.font, title, this.width / 2, this.height / 2 - 50, COLOR_TITLE);

        // Descripción con wrap automático para que no se salga de la pantalla
        int descY = this.height / 2 - 30;
        int maxWidth = this.width - 60; // Margen de 30px a cada lado
        for (FormattedCharSequence line : this.font.split(Component.literal(desc), maxWidth)) {
            graphics.drawString(this.font, line,
                    (this.width - this.font.width(line)) / 2,
                    descY, COLOR_DESC);
            descY += 10; // Separación entre líneas
        }
    }

    // ─ Lógica de selección ---------------------------------------------------

    /**
     * Se ejecuta al pulsar uno de los botones de tier.
     * Envía el tier elegido al servidor via comando /tierself.
     * Si era la pantalla de Rol, abre automáticamente la de PvP.
     * Si era la pantalla de PvP, cierra la GUI — el jugador ya tiene ambos tiers.
     *
     * @param tier El número de tier elegido (1–5)
     */
    private void onTierSelected(int tier) {
        if (this.minecraft == null) return;

        if (mode == Mode.ROL) {
            // Guardar tier de Rol en el servidor y pasar al siguiente paso
            this.minecraft.player.connection.sendCommand("tierself rol " + tier);
            this.minecraft.setScreen(new TierSelectionScreen(Mode.PVP));
        } else {
            // Guardar tier de PvP en el servidor y cerrar la GUI
            this.minecraft.player.connection.sendCommand("tierself pvp " + tier);
            this.minecraft.setScreen(null);
        }
    }
}