package com.cipollomods.prefixtag;

import net.minecraft.nbt.CompoundTag;

/**
 * Modelo de datos de tier para un jugador individual.
 * Almacena y serializa en NBT todos los datos que el mod necesita
 * persistir entre sesiones para cada jugador.
 *
 * Campos persistidos:
 *   - rolTier      > tier de Rol elegido (1–5), o -1 si no está asignado
 *   - pvpTier      > tier de PvP elegido (1–5), o -1 si no está asignado
 *   - nameColor    > nombre del color del jugador en chat y nametag
 *   - showOnline   > si es true, muestra un círculo verde (§a●) antes del prefijo
 *
 * El ciclo de vida de esta clase lo gestiona {@link TierEventHandler}:
 * se crea o carga al hacer login y se guarda al hacer logout.
 */
public class PlayerTierData {

    // -1 = sin asignar. Tiers válidos: 1–5.
    private int rolTier   = -1;
    private int pvpTier   = -1;

    private String nameColor = "white";

    // Si es true, se antepone "§a● " al prefijo en chat y nametag.
    // Se gestiona con /tier online <jugador> <true|false>.
    private boolean showOnline = false;

    public int getRolTier() { return rolTier; }

    public int getPvpTier() { return pvpTier; }

    public boolean hasRolTier() { return rolTier != -1; }

    public boolean hasPvpTier() { return pvpTier != -1; }

    public boolean isFullyAssigned() { return hasRolTier() && hasPvpTier(); }

    public String getNameColor() { return nameColor; }

    public boolean isShowOnline() { return showOnline; }

    public String getColorCode() {
        return PrefixTagConfig.colorNameToCode(nameColor);
    }

    /**
     * Asigna el tier de Rol del jugador.
     *
     * @param tier Valor entre 1 y 5 (ambos inclusive)
     * @throws IllegalArgumentException si el valor está fuera de rango
     */
    public void setRolTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("Rol tier debe estar entre 1 y 5, recibido: " + tier);
        this.rolTier = tier;
    }

    /**
     * Asigna el tier de PvP del jugador.
     *
     * @param tier Valor entre 1 y 5 (ambos inclusive)
     * @throws IllegalArgumentException si el valor está fuera de rango
     */
    public void setPvpTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("PvP tier debe estar entre 1 y 5, recibido: " + tier);
        this.pvpTier = tier;
    }

    /**
     * Asigna el color del nombre del jugador.
     *
     * @param color Nombre del color (ej. "gold", "red"). Ver {@link PrefixTagConfig#colorNameToCode}.
     */
    public void setNameColor(String color) {
        this.nameColor = color;
    }

    /**
     * Activa o desactiva el indicador de conexión (§a●) en el prefijo.
     *
     * @param showOnline true para activarlo, false para desactivarlo
     */
    public void setShowOnline(boolean showOnline) {
        this.showOnline = showOnline;
    }

    /**
     * Genera el prefijo completo para mostrar en chat y nametag.
     *
     * Formato: [Rx|Px]
     * Con indicador online activo: §a● §r[Rx|Px]
     * Con tiers sin asignar: [R?|P?]
     *
     * @return String con códigos de formato de Minecraft
     */
    public String getPrefix() {
        String rolLabel  = hasRolTier() ? PrefixTagConfig.getRolLabel(rolTier) : "R?";
        String pvpLabel  = hasPvpTier() ? PrefixTagConfig.getPvpLabel(pvpTier) : "P?";
        String rolColor  = hasRolTier() ? PrefixTagConfig.getTierColor(rolTier) : "§f";
        String pvpColor  = hasPvpTier() ? PrefixTagConfig.getTierColor(pvpTier) : "§f";
        String online    = showOnline ? "§a● §r" : "";

        return online + "§f[" + rolColor + rolLabel + "§f|" + pvpColor + pvpLabel + "§f]§r";
    }

    /**
     * Serializa los datos del jugador a un {@link CompoundTag} NBT.
     * Llamado por {@link TierEventHandler} al hacer logout o al guardar manualmente.
     *
     * @return CompoundTag con todos los campos del jugador
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("RolTier", rolTier);
        tag.putInt("PvpTier", pvpTier);
        tag.putString("NameColor", nameColor);
        tag.putBoolean("ShowOnline", showOnline);
        return tag;
    }

    /**
     * Deserializa los datos desde un {@link CompoundTag} NBT.
     * Llamado por {@link TierEventHandler} al hacer login con datos existentes.
     *
     * Los campos opcionales (NameColor, ShowOnline) tienen valores por defecto
     * para mantener compatibilidad con saves de versiones anteriores del mod.
     *
     * @param tag CompoundTag guardado previamente con {@link #save()}
     * @return Nueva instancia de PlayerTierData con los datos cargados
     */
    public static PlayerTierData load(CompoundTag tag) {
        PlayerTierData data = new PlayerTierData();
        data.rolTier    = tag.getInt("RolTier");
        data.pvpTier    = tag.getInt("PvpTier");
        data.nameColor  = tag.contains("NameColor")  ? tag.getString("NameColor")   : "white";
        data.showOnline = tag.contains("ShowOnline") && tag.getBoolean("ShowOnline");
        return data;
    }

    /**
     * Reinicia ambos tiers a -1 (sin asignar).
     * La próxima vez que el jugador entre al servidor se le mostrará la GUI de selección.
     * Llamado desde {@link TierCommand} al ejecutar /tier reset.
     */
    public void resetTiers() {
        this.rolTier = -1;
        this.pvpTier = -1;
    }
}