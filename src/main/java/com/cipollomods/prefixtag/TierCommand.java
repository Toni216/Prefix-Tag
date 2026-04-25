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
 *     setrol  <jugador> <1-5>         > asigna el tier de Rol (puede sobrescribir)
 *     setpvp  <jugador> <1-5>         > asigna el tier de PvP (puede sobrescribir)
 *     setcolor <jugador> <color>      > cambia el color del nombre en chat y nametag
 *     online  <jugador> <true|false>  > activa o desactiva el indicador de conexión (§a●)
 *     check   <jugador>               > muestra el prefijo actual de un jugador
 *     reset   <jugador>               > reinicia los tiers; el jugador verá la GUI al reconectarse
 *
 *   /tierself (sin restricción de permisos — uso exclusivo de la GUI)
 *     rol <1-5> > el jugador se asigna su propio tier de Rol (solo si no tiene uno)
 *     pvp <1-5> > el jugador se asigna su propio tier de PvP (solo si no tiene uno)
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
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                .executes(ctx -> setRol(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                )))))

                        .then(Commands.literal("setpvp")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                .executes(ctx -> setPvp(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                )))))

                        .then(Commands.literal("setcolor")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("color", StringArgumentType.string())
                                                .executes(ctx -> setColor(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        StringArgumentType.getString(ctx, "color")
                                                )))))

                        .then(Commands.literal("online")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("valor", BoolArgumentType.bool())
                                                .executes(ctx -> setOnline(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        BoolArgumentType.getBool(ctx, "valor")
                                                )))))

                        .then(Commands.literal("check")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .executes(ctx -> check(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jugador")
                                        ))))

                        .then(Commands.literal("reset")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .executes(ctx -> reset(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jugador")
                                        ))))
        );

        // * /tierself (GUI interna) -------------
        // Sin restricción de permisos — los métodos comprueban internamente
        // que el tier no esté ya asignado para evitar abusos.
        event.getDispatcher().register(
                Commands.literal("tierself")

                        .then(Commands.literal("rol")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(ctx -> setSelfRol(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))))

                        .then(Commands.literal("pvp")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(ctx -> setSelfPvp(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))))
        );
    }

    // * /tier setrol -------------

    /**
     * Asigna el tier de Rol a un jugador. Puede sobrescribir un tier existente.
     * Notifica al admin que ejecuta el comando y al jugador afectado.
     */
    private static int setRol(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        data.setRolTier(tier);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aTier de Rol de " + target.getName().getString() + " establecido a R" + tier
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eTu tier de Rol ha sido actualizado a §fR" + tier
        ));
        return 1;
    }

    // * /tier setpvp -------------

    /**
     * Asigna el tier de PvP a un jugador. Puede sobrescribir un tier existente.
     * Notifica al admin que ejecuta el comando y al jugador afectado.
     */
    private static int setPvp(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        data.setPvpTier(tier);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aTier de PvP de " + target.getName().getString() + " establecido a P" + tier
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eTu tier de PvP ha sido actualizado a §fP" + tier
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
            source.sendFailure(Component.literal("§cColor no reconocido: " + color));
            return 0;
        }

        data.setNameColor(color);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aColor de nombre de " + target.getName().getString() + " establecido a " + colorCode + color
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eTu color de nombre ha sido actualizado a " + colorCode + color
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
                "§aIndicador online de " + target.getName().getString() + " " + (value ? "§aactivado" : "§cdesactivado")
        ), true);
        return 1;
    }

    // * /tier check --------------

    /**
     * Muestra el prefijo actual de un jugador al admin.
     * No notifica al jugador afectado.
     */
    private static int check(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());
        if (data == null) return sendPlayerNotFound(source, target);

        source.sendSuccess(() -> Component.literal(
                "§f" + target.getName().getString() + " — " + data.getPrefix()
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
                "§aTiers de " + target.getName().getString() + " reiniciados. Verá la GUI al reconectarse."
        ), true);
        target.sendSystemMessage(Component.literal(
                "§eTus tiers han sido reiniciados. Por favor reconéctate para elegirlos de nuevo."
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
                source.sendFailure(Component.literal("§cYa tienes un tier de Rol asignado."));
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
                source.sendFailure(Component.literal("§cYa tienes un tier de PvP asignado."));
                return 0;
            }

            data.setPvpTier(tier);
            TierEventHandler.savePlayerData(player);
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
        source.sendFailure(Component.literal("§cNo se encontraron datos para " + target.getName().getString()));
        return 0;
    }
}