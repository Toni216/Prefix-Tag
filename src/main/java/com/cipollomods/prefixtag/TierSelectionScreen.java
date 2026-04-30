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
 *   1. {@link Mode#ROL}  > el jugador elige su tier de Rol
 *   2. {@link Mode#PVP}  > el jugador elige su tier de PvP
 *
 * Al confirmar la selección de Rol se abre automáticamente la pantalla de PvP.
 * Al confirmar la selección de PvP la GUI se cierra y el jugador entra al mundo.
 *
 * La pantalla no se puede cerrar con Escape ({@link #shouldCloseOnEsc} = false)
 * para garantizar que el jugador siempre tenga ambos tiers asignados.
 *
 * Los botones se distribuyen en filas de máximo 3, centradas horizontalmente.
 * El número de botones depende del numero de tiers configurado en
 * {@link PrefixTagConfig}.
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

    /** Espacio vertical entre filas de botones en píxeles. */
    private static final int ROW_GAP = 8;

    /** Máximo de botones por fila. */
    private static final int MAX_PER_ROW = 3;

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
     * Crea y posiciona los botones de selección de tier en filas de máximo {@link #MAX_PER_ROW}.
     * Cada fila se centra horizontalmente de forma independiente.
     * Se llama automáticamente al abrir la pantalla y al redimensionar la ventana.
     */
    @Override
    protected void init() {
        int count  = mode == Mode.ROL ? PrefixTagConfig.getRolTierCount() : PrefixTagConfig.getPvpTierCount();
        int rows   = (int) Math.ceil((double) count / MAX_PER_ROW);

        // Altura total ocupada por todas las filas, centrada verticalmente
        int totalH = rows * BTN_H + (rows - 1) * ROW_GAP;
        int startY = this.height / 2 - totalH / 2;

        for (int row = 0; row < rows; row++) {
            // Tiers que van en esta fila
            int firstTier = row * MAX_PER_ROW + 1;
            int lastTier  = Math.min(firstTier + MAX_PER_ROW - 1, count);
            int inRow     = lastTier - firstTier + 1;

            // Centrar esta fila horizontalmente
            int rowWidth = inRow * BTN_W + (inRow - 1) * BTN_GAP;
            int startX   = this.width / 2 - rowWidth / 2;
            int y        = startY + row * (BTN_H + ROW_GAP);

            for (int col = 0; col < inRow; col++) {
                final int tier = firstTier + col;
                int x = startX + col * (BTN_W + BTN_GAP);
                String label = mode == Mode.ROL
                        ? PrefixTagConfig.getRolLabel(tier)
                        : PrefixTagConfig.getPvpLabel(tier);

                this.addRenderableWidget(Button.builder(
                        Component.literal(label),
                        btn -> onTierSelected(tier)
                ).pos(x, y).size(BTN_W, BTN_H).build());
            }
        }
    }

    // ─ Render ----------

    /**
     * Dibuja el fondo, el título y la descripción en cada frame.
     * El texto se posiciona por encima del bloque de botones para evitar superposiciones.
     *
     * Llamado en cada frame — mantener el código eficiente.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        String title = mode == Mode.ROL ? PrefixTagConfig.ROL_GUI_TITLE.get() : PrefixTagConfig.PVP_GUI_TITLE.get();
        String desc  = mode == Mode.ROL ? PrefixTagConfig.ROL_GUI_DESC.get()  : PrefixTagConfig.PVP_GUI_DESC.get();

        // Calcular dónde empieza el bloque de botones (misma lógica que init)
        int count  = mode == Mode.ROL ? PrefixTagConfig.getRolTierCount() : PrefixTagConfig.getPvpTierCount();
        int rows   = (int) Math.ceil((double) count / MAX_PER_ROW);
        int totalH = rows * BTN_H + (rows - 1) * ROW_GAP;
        int btnsStartY = this.height / 2 - totalH / 2;

        // Descripción justo encima de los botones, con 8px de margen
        int descY    = btnsStartY - 18;
        int maxWidth = this.width - 60;
        // Dibujar líneas de atrás hacia adelante para calcular el espacio que ocupa
        var lines = this.font.split(Component.literal(desc), maxWidth);
        descY -= (lines.size() - 1) * 10; // ajustar si hay varias líneas
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, (this.width - this.font.width(line)) / 2, descY, COLOR_DESC);
            descY += 10;
        }

        // Título encima de la descripción, con 14px de margen
        int titleY = btnsStartY - 18 - lines.size() * 10 - 14;
        graphics.drawCenteredString(this.font, title, this.width / 2, titleY, COLOR_TITLE);
    }

    // ─ Selección ------------------

    /**
     * Se ejecuta al pulsar un botón de tier.
     * Envía el tier elegido al servidor via /tierself y avanza al siguiente paso.
     *
     * @param tier El número de tier elegido
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