package com.cipollomods.cipollotiers;

import net.minecraft.nbt.CompoundTag;

public class PlayerTierData {

    // Valores por defecto (-1 = sin asignar)
    private int rolTier = -1;
    private int pvpTier = -1;

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getRolTier() { return rolTier; }
    public int getPvpTier() { return pvpTier; }

    public boolean hasRolTier() { return rolTier != -1; }
    public boolean hasPvpTier() { return pvpTier != -1; }
    public boolean isFullyAssigned() { return hasRolTier() && hasPvpTier(); }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setRolTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("Rol tier debe ser entre 1 y 5");
        this.rolTier = tier;
    }

    public void setPvpTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("PvP tier debe ser entre 1 y 5");
        this.pvpTier = tier;
    }

    // ── Prefijo ───────────────────────────────────────────────────────────────

    public String getPrefix() {
        String rol = hasRolTier() ? "R" + rolTier : "R?";
        String pvp = hasPvpTier() ? "P" + pvpTier : "P?";
        return "[" + rol + "|" + pvp + "]";
    }

    // ── NBT (guardar y cargar) ────────────────────────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("RolTier", rolTier);
        tag.putInt("PvpTier", pvpTier);
        return tag;
    }

    public static PlayerTierData load(CompoundTag tag) {
        PlayerTierData data = new PlayerTierData();
        data.rolTier = tag.getInt("RolTier");
        data.pvpTier = tag.getInt("PvpTier");
        return data;
    }
}