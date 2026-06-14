package com.cipollomods.prefixtag;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Define y gestiona toda la configuración del mod mediante la Forge Config API.
 * Al cargarse por primera vez genera el archivo 'prefixtag.toml' en config/.
 *
 * Secciones configurables:
 *   - tier_count         > número de tiers activos de Rol y de PvP (2–10)
 *   - rol_labels         > etiquetas de cada tier de Rol (R1–R10, se usan los primeros N)
 *   - pvp_labels         > etiquetas de cada tier de PvP (P1–P10, se usan los primeros N)
 *   - gui_texts          > título y descripción de las pantallas de selección
 *   - tier_colors        > color del prefijo para cada número de tier (1–10)
 *   - tier_descriptions  > tooltip (descripción al pasar el ratón) de cada tier
 *
 * Los comentarios de los campos TOML están en inglés para que sean legibles
 * por cualquier administrador de servidor independientemente del idioma.
 *
 * Sistema bilingüe:
 *   Los textos de GUI y los tooltips usan claves de traducción como valor por
 *   defecto (ej. "prefixtag.gui.rol_title"). Esas claves se traducen automáticamente
 *   al idioma del jugador mediante los archivos de idioma en
 *   assets/prefixtag/lang/ (en_us.json, es_es.json).
 *
 *   Si un administrador reemplaza el valor por su propio texto, ese texto se
 *   muestra tal cual (porque ya no es una clave conocida). Así se consigue:
 *   defaults localizados por jugador + override total por parte del admin.
 */
public class PrefixTagConfig {

    /** Número máximo de tiers soportados (límite de los arrays de config). */
    private static final int MAX_TIERS = 10;

    /** Spec registrado en Forge. Se pasa a {@link net.minecraftforge.fml.ModLoadingContext#registerConfig}. */
    public static final ForgeConfigSpec SPEC;

    // Conteo de tiers activos. Se usan como ConfigValue<Integer> (no IntValue)
    // para evitar que Forge añada la anotación "#Range" en el TOML.
    public static ForgeConfigSpec.ConfigValue<Integer> ROL_TIER_COUNT;
    public static ForgeConfigSpec.ConfigValue<Integer> PVP_TIER_COUNT;

    // Arrays de 10 posiciones. Solo se usan los primeros ROL/PVP_TIER_COUNT.
    public static ForgeConfigSpec.ConfigValue<String>[] ROL_LABELS;
    public static ForgeConfigSpec.ConfigValue<String>[] PVP_LABELS;
    public static ForgeConfigSpec.ConfigValue<String>[] TIER_COLORS;

    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> ROL_GUI_DESC;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_TITLE;
    public static ForgeConfigSpec.ConfigValue<String> PVP_GUI_DESC;

    // Descripciones de tooltip por tier. Por defecto son claves de traducción
    // (se localizan solas); el admin puede sobrescribirlas con su propio texto.
    public static ForgeConfigSpec.ConfigValue<String>[] ROL_DESCS;
    public static ForgeConfigSpec.ConfigValue<String>[] PVP_DESCS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // Conteo de tiers ----------
        builder.comment("Number of active tiers. Min: 2, Max: 10").push("tier_count");
        ROL_TIER_COUNT = builder.comment("Number of Rol tiers").define("rol_tier_count", 5);
        PVP_TIER_COUNT = builder.comment("Number of PvP tiers").define("pvp_tier_count", 5);
        builder.pop();

        // Etiquetas de Rol — sin comentario por entrada para no ensuciar el TOML
        builder.comment("Labels for each Rol tier (only the first rol_tier_count are used)").push("rol_labels");
        ROL_LABELS = newStringArray(MAX_TIERS);
        for (int i = 0; i < MAX_TIERS; i++) {
            ROL_LABELS[i] = builder.define("rol" + (i + 1), "R" + (i + 1));
        }
        builder.pop();

        // Etiquetas de PvP
        builder.comment("Labels for each PvP tier (only the first pvp_tier_count are used)").push("pvp_labels");
        PVP_LABELS = newStringArray(MAX_TIERS);
        for (int i = 0; i < MAX_TIERS; i++) {
            PVP_LABELS[i] = builder.define("pvp" + (i + 1), "P" + (i + 1));
        }
        builder.pop();

        // Textos de GUI — valores por defecto son claves de traducción
        builder.comment("Texts shown in the tier selection screen. " +
                        "Default values are translation keys, automatically localized to the " +
                        "player's language (see assets/prefixtag/lang/). " +
                        "Replace any value with your own text to override it for all players.")
                .push("gui_texts");
        ROL_GUI_TITLE = builder.define("rol_title", "prefixtag.gui.rol_title");
        ROL_GUI_DESC  = builder.define("rol_desc", "prefixtag.gui.rol_desc");
        PVP_GUI_TITLE = builder.define("pvp_title", "prefixtag.gui.pvp_title");
        PVP_GUI_DESC  = builder.define("pvp_desc", "prefixtag.gui.pvp_desc");
        builder.pop();

        // Colores de prefijo por número de tier
        String[] defaultColors = {
                "green", "yellow", "gold", "red", "dark_red",
                "aqua", "light_purple", "blue", "dark_purple", "dark_gray"
        };
        builder.comment("Color of the prefix for each tier number. " +
                        "Available values: black, dark_blue, dark_green, dark_aqua, dark_red, " +
                        "dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white")
                .push("tier_colors");
        TIER_COLORS = newStringArray(MAX_TIERS);
        for (int i = 0; i < MAX_TIERS; i++) {
            TIER_COLORS[i] = builder.define("tier" + (i + 1) + "_color", defaultColors[i]);
        }
        builder.pop();

        // Descripciones de tooltip por tier — claves de traducción por defecto
        builder.comment("Hover tooltip shown on each tier button in the selection screen. " +
                        "Default values are translation keys, automatically localized to the " +
                        "player's language (see assets/prefixtag/lang/). " +
                        "Replace any value with your own text to override it for all players. " +
                        "Use Minecraft formatting codes (e.g. \u00a7a, \u00a7c, \u00a7l) and \\n for line breaks. " +
                        "Leave a value empty (\"\") to hide the tooltip for that tier.")
                .push("tier_descriptions");
        ROL_DESCS = newStringArray(MAX_TIERS);
        for (int i = 0; i < MAX_TIERS; i++) {
            ROL_DESCS[i] = builder.define("rol" + (i + 1) + "_desc", "prefixtag.desc.rol" + (i + 1));
        }
        PVP_DESCS = newStringArray(MAX_TIERS);
        for (int i = 0; i < MAX_TIERS; i++) {
            PVP_DESCS[i] = builder.define("pvp" + (i + 1) + "_desc", "prefixtag.desc.pvp" + (i + 1));
        }
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * Crea un array tipado de ConfigValue<String>.
     * Aislado en un método para confinar el warning de creación de array genérico.
     */
    @SuppressWarnings("unchecked")
    private static ForgeConfigSpec.ConfigValue<String>[] newStringArray(int size) {
        return (ForgeConfigSpec.ConfigValue<String>[]) new ForgeConfigSpec.ConfigValue[size];
    }

    /**
     * Limita un conteo de tiers al rango soportado [2, MAX_TIERS].
     * Protege la GUI y los getters de valores fuera de rango puestos a mano en el TOML.
     *
     * @param value Valor leído de la config
     * @return Valor restringido entre 2 y 10
     */
    private static int clampCount(int value) {
        return Math.max(2, Math.min(MAX_TIERS, value));
    }

    /**
     * Devuelve el número de tiers de Rol activos (entre 2 y 10).
     * Lo usa la GUI para saber cuántos botones crear.
     */
    public static int getRolTierCount() {
        return clampCount(ROL_TIER_COUNT.get());
    }

    /**
     * Devuelve el número de tiers de PvP activos (entre 2 y 10).
     * Lo usa la GUI para saber cuántos botones crear.
     */
    public static int getPvpTierCount() {
        return clampCount(PVP_TIER_COUNT.get());
    }

    /**
     * Devuelve la etiqueta configurada para un tier de Rol.
     *
     * @param tier Número de tier (1–10)
     * @return La etiqueta configurada, o "R?" si el tier no es válido
     */
    public static String getRolLabel(int tier) {
        if (tier < 1 || tier > MAX_TIERS) return "R?";
        return ROL_LABELS[tier - 1].get();
    }

    /**
     * Devuelve la etiqueta configurada para un tier de PvP.
     *
     * @param tier Número de tier (1–10)
     * @return La etiqueta configurada, o "P?" si el tier no es válido
     */
    public static String getPvpLabel(int tier) {
        if (tier < 1 || tier > MAX_TIERS) return "P?";
        return PVP_LABELS[tier - 1].get();
    }

    /**
     * Devuelve la descripción (tooltip) configurada para un tier de Rol.
     * Puede ser una clave de traducción o texto literal puesto por el admin.
     *
     * @param tier Número de tier (1–10)
     * @return El texto/clave configurado, o "" si el tier no es válido
     *         (una cadena vacía hace que no se muestre tooltip)
     */
    public static String getRolDesc(int tier) {
        if (tier < 1 || tier > MAX_TIERS) return "";
        return ROL_DESCS[tier - 1].get();
    }

    /**
     * Devuelve la descripción (tooltip) configurada para un tier de PvP.
     * Puede ser una clave de traducción o texto literal puesto por el admin.
     *
     * @param tier Número de tier (1–10)
     * @return El texto/clave configurado, o "" si el tier no es válido
     *         (una cadena vacía hace que no se muestre tooltip)
     */
    public static String getPvpDesc(int tier) {
        if (tier < 1 || tier > MAX_TIERS) return "";
        return PVP_DESCS[tier - 1].get();
    }

    /**
     * Devuelve el código de formato de Minecraft para el color de un tier.
     * Lee el nombre de color desde la config y lo convierte con {@link #colorNameToCode}.
     *
     * @param tier Número de tier (1–10)
     * @return Código de formato de Minecraft (ej. "§a")
     */
    public static String getTierColor(int tier) {
        if (tier < 1 || tier > MAX_TIERS) return colorNameToCode("white");
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