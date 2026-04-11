package com.cipollomods.prefixtag;

import net.minecraft.nbt.CompoundTag;
/**
 * Almacena los datos de tier de un jugador individual.
 * Cada jugador tiene dos tiers independientes:
 *   - Rol (R1–R5): elegido por el jugador al entrar por primera vez
 *   - PvP (P1–P5): elegido por el jugador al entrar por primera vez
 *
 * Un valor de -1 indica que el tier aún no ha sido asignado.
 */
public class PlayerTierData {

    // -1 = sin asignar. Los tiers válidos van de 1 a 5.
    private int rolTier = -1;
    private int pvpTier = -1;

    // ─ Getters -------------------------------------------------------

    /** Devuelve el tier de Rol actual. -1 si no está asignado. */
    public int getRolTier() { return rolTier; }

    /** Devuelve el tier de PvP actual. -1 si no está asignado. */
    public int getPvpTier() { return pvpTier; }

    /** Devuelve true si el jugador tiene un tier de Rol asignado. */
    public boolean hasRolTier() { return rolTier != -1; }

    /** Devuelve true si el jugador tiene un tier de PvP asignado. */
    public boolean hasPvpTier() { return pvpTier != -1; }

    /**
     * Devuelve true si el jugador tiene ambos tiers asignados.
     * Se usa para decidir si hay que mostrar la GUI de selección.
     */
    public boolean isFullyAssigned() { return hasRolTier() && hasPvpTier(); }

    // ─ Setters ------------------------------------------------------

    /**
     * Asigna el tier de Rol del jugador.
     * @param tier Valor entre 1 y 5 (ambos inclusive)
     * @throws IllegalArgumentException si el valor está fuera de rango
     */
    public void setRolTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("Rol tier debe ser entre 1 y 5");
        this.rolTier = tier;
    }
    /**
     * Asigna el tier de PvP del jugador.
     * @param tier Valor entre 1 y 5 (ambos inclusive)
     * @throws IllegalArgumentException si el valor está fuera de rango
     */
    public void setPvpTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("PvP tier debe ser entre 1 y 5");
        this.pvpTier = tier;
    }

    // ─ Prefijo -----------------------------------------------------------------

    /**
     * Genera el prefijo combinado para mostrar en chat y nametag.
     * Ejemplo: [R2|P3]
     * Si algún tier no está asignado, muestra ? en su lugar: [R?|P?]
     */
    public String getPrefix() {
        String rol = hasRolTier() ? "R" + rolTier : "R?";
        String pvp = hasPvpTier() ? "P" + pvpTier : "P?";
        return "[" + rol + "|" + pvp + "]";
    }

    // ─ NBT (guardar y cargar) --------------------------------------------------

    /**
     * Serializa los datos del jugador a un CompoundTag NBT.
     * Se llama al guardar los datos en el PersistentData del jugador.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("RolTier", rolTier);
        tag.putInt("PvpTier", pvpTier);
        return tag;
    }

    /**
     * Deserializa los datos desde un CompoundTag NBT.
     * Se llama al cargar los datos cuando el jugador entra al servidor.
     * @param tag El CompoundTag guardado previamente con save()
     */
    public static PlayerTierData load(CompoundTag tag) {
        PlayerTierData data = new PlayerTierData();
        data.rolTier = tag.getInt("RolTier");
        data.pvpTier = tag.getInt("PvpTier");
        return data;
    }

    /**
     * Reinicia ambos tiers a -1 (sin asignar).
     * Se usa cuando un admin ejecuta /tier reset <jugador>.
     * El jugador verá la GUI de selección la próxima vez que entre.
     */
    public void resetTiers() {
        this.rolTier = -1;
        this.pvpTier = -1;
    }
}