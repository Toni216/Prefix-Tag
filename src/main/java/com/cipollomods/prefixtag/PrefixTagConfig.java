package com.cipollomods.prefixtag;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Define y gestiona toda la configuración del mod mediante la Forge Config API.
 * Al cargarse por primera vez genera el archivo 'prefixtag.toml' en config/.
 *
 * Secciones configurables:
 *   - rol_labels   > etiquetas de cada tier de Rol (R1–R5 por defecto)
 *   - pvp_labels   > etiquetas de cada tier de PvP (P1–P5 por defecto)
 *   - gui_texts    > título y descripción de las pantallas de selección
 *   - tier_colors  > color del prefijo para cada número de tier
 *
 * Los comentarios de los campos TOML están en inglés para que sean legibles
 * por cualquier administrador de servidor independientemente del idioma.
 */
public class PrefixTagConfig {

    /** Spec registrado en Forge. Se pasa a {@link net.minecraftforge.fml.ModLoadingContext#registerConfig}. */
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<String> ROL1_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> ROL2_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> ROL3_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> ROL4_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> ROL5_LABEL;

    public static ForgeConfigSpec.ConfigValue<String> PVP1_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> PVP2_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> PVP3_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> PVP4_LABEL;
    public static ForgeConfigSpec.ConfigValue<String> PVP5_LABEL;

    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_DESC;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_DESC;

    public static ForgeConfigSpec.ConfigValue<String> TIER1_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TIER2_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TIER3_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TIER4_COLOR;
    public static ForgeConfigSpec.ConfigValue<String> TIER5_COLOR;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Labels for each Rol tier").push("rol_labels");
        ROL1_LABEL = builder.comment("Label for Rol tier 1").define("rol1", "R1");
        ROL2_LABEL = builder.comment("Label for Rol tier 2").define("rol2", "R2");
        ROL3_LABEL = builder.comment("Label for Rol tier 3").define("rol3", "R3");
        ROL4_LABEL = builder.comment("Label for Rol tier 4").define("rol4", "R4");
        ROL5_LABEL = builder.comment("Label for Rol tier 5").define("rol5", "R5");
        builder.pop();

        builder.comment("Labels for each PvP tier").push("pvp_labels");
        PVP1_LABEL = builder.comment("Label for PvP tier 1").define("pvp1", "P1");
        PVP2_LABEL = builder.comment("Label for PvP tier 2").define("pvp2", "P2");
        PVP3_LABEL = builder.comment("Label for PvP tier 3").define("pvp3", "P3");
        PVP4_LABEL = builder.comment("Label for PvP tier 4").define("pvp4", "P4");
        PVP5_LABEL = builder.comment("Label for PvP tier 5").define("pvp5", "P5");
        builder.pop();

        builder.comment("Texts shown in the tier selection screen").push("gui_texts");
        ROL_GUI_TITLE = builder.comment("Title of the Rol selection screen")
                .define("rol_title", "Selecciona tu rango de Rol");
        ROL_GUI_DESC  = builder.comment("Description of the Rol selection screen")
                .define("rol_desc", "Elige el rango de rol que desees tener, puedes leerlo en el canal de discord dispuesto para ello");
        PVP_GUI_TITLE = builder.comment("Title of the PvP selection screen")
                .define("pvp_title", "Selecciona tu rango de PvP");
        PVP_GUI_DESC  = builder.comment("Description of the PvP selection screen")
                .define("pvp_desc", "Elige el rango de PvP que desees tener, puedes leerlo en el canal de discord dispuesto para ello");
        builder.pop();

        builder.comment("Color of the prefix for each tier number. " +
                        "Available values: black, dark_blue, dark_green, dark_aqua, dark_red, " +
                        "dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white")
                .push("tier_colors");
        TIER1_COLOR = builder.comment("Color for tier 1 prefix").define("tier1_color", "green");
        TIER2_COLOR = builder.comment("Color for tier 2 prefix").define("tier2_color", "yellow");
        TIER3_COLOR = builder.comment("Color for tier 3 prefix").define("tier3_color", "gold");
        TIER4_COLOR = builder.comment("Color for tier 4 prefix").define("tier4_color", "red");
        TIER5_COLOR = builder.comment("Color for tier 5 prefix").define("tier5_color", "dark_red");
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * Devuelve la etiqueta configurada para un tier de Rol.
     *
     * @param tier Número de tier (1–5)
     * @return La etiqueta configurada, o "R?" si el tier no es válido
     */
    public static String getRolLabel(int tier) {
        return switch (tier) {
            case 1 -> ROL1_LABEL.get();
            case 2 -> ROL2_LABEL.get();
            case 3 -> ROL3_LABEL.get();
            case 4 -> ROL4_LABEL.get();
            case 5 -> ROL5_LABEL.get();
            default -> "R?";
        };
    }

    /**
     * Devuelve la etiqueta configurada para un tier de PvP.
     *
     * @param tier Número de tier (1–5)
     * @return La etiqueta configurada, o "P?" si el tier no es válido
     */
    public static String getPvpLabel(int tier) {
        return switch (tier) {
            case 1 -> PVP1_LABEL.get();
            case 2 -> PVP2_LABEL.get();
            case 3 -> PVP3_LABEL.get();
            case 4 -> PVP4_LABEL.get();
            case 5 -> PVP5_LABEL.get();
            default -> "P?";
        };
    }

    /**
     * Devuelve el código de formato de Minecraft para el color de un tier.
     * Lee el nombre de color desde la config y lo convierte con {@link #colorNameToCode}.
     *
     * @param tier Número de tier (1–5)
     * @return Código de formato de Minecraft (ej. "§a")
     */
    public static String getTierColor(int tier) {
        String colorName = switch (tier) {
            case 1 -> TIER1_COLOR.get();
            case 2 -> TIER2_COLOR.get();
            case 3 -> TIER3_COLOR.get();
            case 4 -> TIER4_COLOR.get();
            case 5 -> TIER5_COLOR.get();
            default -> "white";
        };
        return colorNameToCode(colorName);
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