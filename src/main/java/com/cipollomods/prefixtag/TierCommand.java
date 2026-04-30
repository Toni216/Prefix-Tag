package com.cipollomods.prefixtag;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registra y gestiona todos los comandos del mod usando Brigadier.
 * Los comandos se dividen en dos grupos:
 *
 *   /tier (requiere op level 2)
 *     setrol    <player> <1-10>       > asigna el tier de Rol (puede sobrescribir)
 *     setpvp    <player> <1-10>       > asigna el tier de PvP (puede sobrescribir)
 *     setcolor  <player> <color>      > cambia el color del nombre en chat y nametag
 *     clearname <player>              > elimina el nombre personalizado de un jugador
 *     online    <player> <true|false> > activa o desactiva el indicador de conexión (§a●)
 *     check     <player>              > muestra el prefijo actual de un jugador
 *     reset     <player>              > reinicia los tiers; el jugador verá la GUI al reconectarse
 *
 *   /tierself (sin restricción de permisos)
 *     rol <1-10>      > el jugador se asigna su propio tier de Rol (solo si no tiene uno)
 *     pvp <1-10>      > el jugador se asigna su propio tier de PvP (solo si no tiene uno)
 *     setname <name>  > establece un nombre personalizado (sin códigos de color)
 *     clearname       > elimina el nombre personalizado propio
 */
@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID)
public class TierCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        // * /tier (admin) ------------
        event.getDispatcher().register(
                Commands.literal("tier")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("setrol")
                                .then(Commands.argument("player", EntityArgument.player())
                                        // Brigadier acepta 1-10; la validación real usa el conteo de la config
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                                                .executes(ctx -> setRol(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                )))))

                        .then(Commands.literal("setpvp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                                                .executes(ctx -> setPvp(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                )))))

                        .then(Commands.literal("setcolor")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("color", StringArgumentType.string())
                                                .executes(ctx -> setColor(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "color")
                                                )))))

                        .then(Commands.literal("clearname")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminClearName(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))))

                        .then(Commands.literal("online")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setOnline(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        BoolArgumentType.getBool(ctx, "value")
                                                )))))

                        .then(Commands.literal("check")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> check(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))))

                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> reset(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))))
        );

        // * /tierself -------------
        // Sin restricción de permisos. Los métodos comprueban internamente
        // que el tier no esté ya asignado (rol/pvp) para evitar abusos desde el chat.
        event.getDispatcher().register(
                Commands.literal("tierself")

                        .then(Commands.literal("rol")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                                        .executes(ctx -> setSelfRol(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))))

                        .then(Commands.literal("pvp")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 10))
                                        .executes(ctx -> setSelfPvp(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))))

                        .then(Commands.literal("setname")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> selfSetName(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name")
                                        ))))

                        .then(Commands.literal("clearname")
                                .executes(ctx -> selfClearName(ctx.getSource())))
        );
    }

    // * /tier setrol -------------

    /**
     * Asigna el tier de Rol a un jugador. Puede sobrescribir un tier existente.
     * Si el valor supera el conteo configurado, PlayerTierData lanza IllegalArgumentException.
     * Notifica al admin que ejecuta el comando y al jugador afectado.
     */
    private static int setRol(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        try {
            data.setRolTier(tier);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§c" + e.getMessage()));
            return 0;
        }

        TierEventHandler.savePlayerData(target);
        String label = PrefixTagConfig.getRolLabel(tier);

        source.sendSuccess(() -> Component.literal(
                "§aRol tier of " + target.getName().getString() + " set to " + label
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eYour Rol tier has been updated to §f" + label
        ));
        return 1;
    }

    // * /tier setpvp -------------

    /**
     * Asigna el tier de PvP a un jugador. Puede sobrescribir un tier existente.
     * Si el valor supera el conteo configurado, PlayerTierData lanza IllegalArgumentException.
     * Notifica al admin que ejecuta el comando y al jugador afectado.
     */
    private static int setPvp(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        try {
            data.setPvpTier(tier);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§c" + e.getMessage()));
            return 0;
        }

        TierEventHandler.savePlayerData(target);
        String label = PrefixTagConfig.getPvpLabel(tier);

        source.sendSuccess(() -> Component.literal(
                "§aPvP tier of " + target.getName().getString() + " set to " + label
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eYour PvP tier has been updated to §f" + label
        ));
        return 1;
    }

    // * /tier setcolor ------------

    /**
     * Cambia el color del nombre de un jugador en chat y nametag.
     * No afecta al prefijo [Rx|Px], solo al nombre.
     * Valida que el color exista antes de aplicarlo.
     */
    private static int setColor(CommandSourceStack source, ServerPlayer target, String color) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        String colorCode = PrefixTagConfig.colorNameToCode(color);
        if (colorCode.equals("§f") && !color.equalsIgnoreCase("white")) {
            source.sendFailure(Component.literal("§cUnrecognized color: " + color));
            return 0;
        }

        data.setNameColor(color);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aName color of " + target.getName().getString() + " set to " + colorCode + color
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eYour name color has been updated to " + colorCode + color
        ));
        return 1;
    }

    // * /tier clearname -----------

    /**
     * Elimina el nombre personalizado de un jugador.
     * El jugador volverá a mostrar su username real en chat y nametag.
     * Notifica al admin y al jugador afectado.
     */
    private static int adminClearName(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        if (!data.hasCustomName()) {
            source.sendFailure(Component.literal("§c" + target.getName().getString() + " has no custom name."));
            return 0;
        }

        data.clearCustomName();
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aCustom name of " + target.getName().getString() + " removed."
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eYour custom name has been removed by an admin."
        ));
        return 1;
    }

    // * /tier online ----------

    /**
     * Activa o desactiva el indicador de conexión (§a●) en el prefijo de un jugador.
     * Cuando está activo, aparece un círculo verde antes del prefijo en chat y nametag.
     */
    private static int setOnline(CommandSourceStack source, ServerPlayer target, boolean value) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        data.setShowOnline(value);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aOnline indicator of " + target.getName().getString() + " " + (value ? "§aenabled" : "§cdisabled")
        ), true);
        return 1;
    }

    // * /tier check --------------

    /**
     * Muestra el prefijo actual y el nombre de display de un jugador al admin.
     * No notifica al jugador afectado.
     */
    private static int check(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        String displayName = data.getDisplayName(target.getName().getString());
        source.sendSuccess(() -> Component.literal(
                "§f" + target.getName().getString() + " — " + data.getPrefix() + " " + data.getColorCode() + displayName
        ), false);
        return 1;
    }

    // * /tier reset ------------------

    /**
     * Reinicia ambos tiers de un jugador a -1 (sin asignar).
     * El jugador verá la GUI de selección la próxima vez que entre al servidor.
     * Notifica al admin y al jugador afectado.
     */
    private static int reset(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        data.resetTiers();
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aTiers of " + target.getName().getString() + " reset. They will see the GUI on next login."
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eYour tiers have been reset. Please reconnect to choose them again."
        ));
        return 1;
    }

    // * /tierself rol ---------------------

    /**
     * Permite al jugador asignarse su propio tier de Rol desde la GUI.
     * Solo funciona si aún no tiene un tier de Rol asignado, para evitar
     * que se ejecute manualmente desde el chat y sobrescriba uno existente.
     */
    private static int setSelfRol(CommandSourceStack source, int tier) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
            if (data == null) return 0;

            if (data.hasRolTier()) {
                source.sendFailure(Component.literal("§cYou already have a Rol tier assigned."));
                return 0;
            }

            data.setRolTier(tier);
            TierEventHandler.savePlayerData(player);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // * /tierself pvp ---------------

    /**
     * Permite al jugador asignarse su propio tier de PvP desde la GUI.
     * Solo funciona si aún no tiene un tier de PvP asignado, para evitar
     * que se ejecute manualmente desde el chat y sobrescriba uno existente.
     */
    private static int setSelfPvp(CommandSourceStack source, int tier) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
            if (data == null) return 0;

            if (data.hasPvpTier()) {
                source.sendFailure(Component.literal("§cYou already have a PvP tier assigned."));
                return 0;
            }

            data.setPvpTier(tier);
            TierEventHandler.savePlayerData(player);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // * /tierself setname -------------

    /**
     * Permite al jugador establecer un nombre personalizado.
     * No se permiten códigos de color (§) — el color asignado por el admin se aplica automáticamente.
     * No tiene restricción de uso: el jugador puede cambiarlo cuantas veces quiera.
     */
    private static int selfSetName(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
            if (data == null) return 0;

            // Rechazar nombres con códigos de color o formato
            if (name.contains("§")) {
                source.sendFailure(Component.literal("§cThe name cannot contain color codes."));
                return 0;
            }

            data.setCustomName(name);
            TierEventHandler.savePlayerData(player);

            source.sendSuccess(() -> Component.literal(
                    "§aYour name has been updated to " + data.getColorCode() + name
            ), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // * /tierself clearname -----------

    /**
     * Permite al jugador eliminar su propio nombre personalizado.
     * Volverá a mostrar su username real en chat y nametag.
     */
    private static int selfClearName(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());
            if (data == null) return 0;

            if (!data.hasCustomName()) {
                source.sendFailure(Component.literal("§cYou have no custom name."));
                return 0;
            }

            data.clearCustomName();
            TierEventHandler.savePlayerData(player);

            source.sendSuccess(() -> Component.literal(
                    "§aYour custom name has been removed."
            ), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // * Utilidades ------------------------

    /**
     * Envía un mensaje de error estándar cuando no se encuentran datos para un jugador
     * y devuelve 0 para indicar fallo del comando.
     *
     * @param source El ejecutor del comando
     * @param target El jugador del que no se encontraron datos
     * @return Siempre 0 (fallo)
     */
    private static int sendPlayerNotFound(CommandSourceStack source, ServerPlayer target) {
        source.sendFailure(Component.literal("§cNo data found for " + target.getName().getString()));
        return 0;
    }
}