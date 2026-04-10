package com.cipollomods.cipollotiers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;

public class TierSelectionScreen extends Screen {

    // ── Modo de la pantalla ───────────────────────────────────────────────────
    public enum Mode { ROL, PVP }
    private final Mode mode;

    // ── Textos ────────────────────────────────────────────────────────────────
    private static final String ROL_TITLE = "Selecciona tu rango de Rol";
    private static final String PVP_TITLE = "Selecciona tu rango de PvP";
    private static final String ROL_DESC  = "Elige el rango de rol que desees tener, puedes leerlo en el canal de discord dispuesto para ello";
    private static final String PVP_DESC  = "Elige el rango de PvP que desees tener, puedes leerlo en el canal de discord dispuesto para ello";

    // ── Constructor ───────────────────────────────────────────────────────────
    public TierSelectionScreen(Mode mode) {
        super(Component.literal(mode == Mode.ROL ? ROL_TITLE : PVP_TITLE));
        this.mode = mode;
    }

    // ── No se puede cerrar con Escape ─────────────────────────────────────────
    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Inicializar botones ───────────────────────────────────────────────────
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY  = this.height / 2 - 10;
        int btnW    = 80;
        int btnH    = 20;
        int gap     = 10;
        int total   = 5 * btnW + 4 * gap;
        int startX  = centerX - total / 2;

        String prefix = mode == Mode.ROL ? "R" : "P";

        for (int i = 1; i <= 5; i++) {
            final int tier = i;
            int x = startX + (i - 1) * (btnW + gap);

            this.addRenderableWidget(Button.builder(
                    Component.literal(prefix + tier),
                    btn -> onTierSelected(tier)
            ).pos(x, startY).size(btnW, btnH).build());
        }
    }

    // ── Render: título y descripción ──────────────────────────────────────────
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        String title = mode == Mode.ROL ? ROL_TITLE : PVP_TITLE;
        String desc  = mode == Mode.ROL ? ROL_DESC  : PVP_DESC;

        // Título centrado
        graphics.drawCenteredString(this.font, title, this.width / 2, this.height / 2 - 50, 0xFFD700);

        int descY = this.height / 2 - 30;
        // Descripción con wrap automático
        int descColor = 0xAAAAAA;
        int maxWidth = this.width - 60;
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(
                Component.literal(desc), maxWidth)) {
            graphics.drawString(this.font, line,
                    (this.width - this.font.width(line)) / 2,
                    descY, descColor);
            descY += 10;
        }
            }

    // ── Al seleccionar un tier ────────────────────────────────────────────────
    private void onTierSelected(int tier) {
        if (this.minecraft == null) return;

        // Enviar al servidor via comando
        if (mode == Mode.ROL) {
            this.minecraft.player.connection.sendCommand("tierself rol " + tier);
            // Abrir pantalla de PvP a continuación
            this.minecraft.setScreen(new TierSelectionScreen(Mode.PVP));
        } else {
            this.minecraft.player.connection.sendCommand("tierself pvp " + tier);
            // Cerrar pantalla — ya tiene ambos tiers
            this.minecraft.setScreen(null);
        }
    }
}