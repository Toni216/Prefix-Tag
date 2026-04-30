package com.cipollomods.prefixtag;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Define y gestiona toda la configuración del mod mediante la Forge Config API.
 * Al cargarse por primera vez genera el archivo 'prefixtag.toml' en config/.
 *
 * Secciones configurables:
 *   - tier_count   > número de tiers activos para Rol y PvP (2–10)
 *   - rol_labels   > etiquetas de cada tier de Rol (hasta 10)
 *   - pvp_labels   > etiquetas de cada tier de PvP (hasta 10)
 *   - gui_texts    > título y descripción de las pantallas de selección
 *   - tier_colors  > color del prefijo para cada número de tier (hasta 10)
 *
 * Los comentarios de los campos TOML están en inglés para que sean legibles
 * por cualquier administrador de servidor independientemente del idioma.
 */
public class PrefixTagConfig {

    // Spec registrado en Forge. Se pasa a {@link net.minecraftforge.fml.ModLoadingContext#registerConfig}
    public static final ForgeConfigSpec SPEC;

    // Número de tiers activos — determina cuántas etiquetas y colores se usan
    public static ForgeConfigSpec.ConfigValue<Integer> ROL_TIER_COUNT;
    public static ForgeConfigSpec.ConfigValue<Integer> PVP_TIER_COUNT;

    // Arrays de 10 posiciones (índice 0 = tier 1, índice 9 = tier 10)
    public static final ForgeConfigSpec.ConfigValue<String>[] ROL_LABELS = new ForgeConfigSpec.ConfigValue[10];
    public static final ForgeConfigSpec.ConfigValue<String>[] PVP_LABELS = new ForgeConfigSpec.ConfigValue[10];
    public static final ForgeConfigSpec.ConfigValue<String>[] TIER_COLORS = new ForgeConfigSpec.ConfigValue[10];

    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_DESC;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_DESC;

    // Colores por defecto para los 10 posibles tiers
    private static final String[] DEFAULT_COLORS = {
            "green", "yellow", "gold", "red", "dark_red",
            "aqua", "light_purple", "blue", "dark_purple", "dark_gray"
    };

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // Numero de tiers — se lee al arrancar para saber cuántos usar
        builder.comment("Number of active tiers. Min: 2, Max: 10").push("tier_count");
        ROL_TIER_COUNT = builder.comment("Number of Rol tiers").define("rol_tier_count", 5);
        PVP_TIER_COUNT = builder.comment("Number of PvP tiers").define("pvp_tier_count", 5);
        builder.pop();

        // Etiquetas de Rol — siempre se registran las 10, solo se usan las primeras COUNT
        builder.comment("Labels for each Rol tier (only the first rol_tier_count are used)").push("rol_labels");
        for (int i = 0; i < 10; i++) {
            int num = i + 1;
            ROL_LABELS[i] = builder.define("rol" + num, "R" + num);
        }
        builder.pop();

        // Etiquetas de PvP
        builder.comment("Labels for each PvP tier (only the first pvp_tier_count are used)").push("pvp_labels");
        for (int i = 0; i < 10; i++) {
            int num = i + 1;
            PVP_LABELS[i] = builder.define("pvp" + num, "P" + num);
        }
        builder.pop();

        // Textos de la GUI
        builder.comment("Texts shown in the tier selection screen").push("gui_texts");
        ROL_GUI_TITLE = builder.define("rol_title", "Select your Rol rank");
        ROL_GUI_DESC  = builder.define("rol_desc", "Choose the Rol rank you want. You can read about each rank in the designated Discord channel.");
        PVP_GUI_TITLE = builder.define("pvp_title", "Select your PvP rank");
        PVP_GUI_DESC  = builder.define("pvp_desc", "Choose the PvP rank you want. You can read about each rank in the designated Discord channel.");
        builder.pop();

        // Colores por tier
        builder.comment("Color of the prefix for each tier number. " +
                        "Available values: black, dark_blue, dark_green, dark_aqua, dark_red, " +
                        "dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white. " +
                        "Only the first rol_tier_count / pvp_tier_count entries are used.")
                .push("tier_colors");
        for (int i = 0; i < 10; i++) {
            int num = i + 1;
            TIER_COLORS[i] = builder.define("tier" + num + "_color", DEFAULT_COLORS[i]);
        }
        builder.pop();

        SPEC = builder.build();
    }

    // Acceso al numero de tiers --------

    /** Número de tiers de Rol activos según la config (2–10). */
    public static int getRolTierCount() {
        return ROL_TIER_COUNT.get();
    }

    /** Número de tiers de PvP activos según la config (2–10). */
    public static int getPvpTierCount() {
        return PVP_TIER_COUNT.get();
    }

    // Acceso a etiquetas ------

    /**
     * Devuelve la etiqueta configurada para un tier de Rol.
     *
     * @param tier Número de tier (1–getRolTierCount())
     * @return La etiqueta configurada, o "R?" si el tier está fuera de rango
     */
    public static String getRolLabel(int tier) {
        if (tier < 1 || tier > 10) return "R?";
        return ROL_LABELS[tier - 1].get();
    }

    /**
     * Devuelve la etiqueta configurada para un tier de PvP.
     *
     * @param tier Número de tier (1–getPvpTierCount())
     * @return La etiqueta configurada, o "P?" si el tier está fuera de rango
     */
    public static String getPvpLabel(int tier) {
        if (tier < 1 || tier > 10) return "P?";
        return PVP_LABELS[tier - 1].get();
    }

    // Acceso a colores --------

    /**
     * Devuelve el código de formato de Minecraft para el color de un tier.
     *
     * @param tier Número de tier (1–10)
     * @return Código de formato de Minecraft (ej. "§a")
     */
    public static String getTierColor(int tier) {
        if (tier < 1 || tier > 10) return "§f";
        return colorNameToCode(TIER_COLORS[tier - 1].get());
    }

    /**
     * Convierte un nombre de color legible a su código de formato de Minecraft.
     * Si el nombre no se reconoce, devuelve blanco (§f) como fallback seguro.
     *
     * @param name Nombre del color en minúsculas (ej. "gold", "dark_red")
     * @return Código de formato de Minecraft (ej. "§6", "§4")
     */
    public static String colorNameToCode(String name) {
        return switch (name.toLowerCase()) {
            case "black"        -> "§0";
            case "dark_blue"    -> "§1";
            case "dark_green"   -> "§2";
            case "dark_aqua"    -> "§3";
            case "dark_red"     -> "§4";
            case "dark_purple"  -> "§5";
            case "gold"         -> "§6";
            case "gray"         -> "§7";
            case "dark_gray"    -> "§8";
            case "blue"         -> "§9";
            case "green"        -> "§a";
            case "aqua"         -> "§b";
            case "red"          -> "§c";
            case "light_purple" -> "§d";
            case "yellow"       -> "§e";
            default             -> "§f"; // white y cualquier valor desconocido
        };
    }
}