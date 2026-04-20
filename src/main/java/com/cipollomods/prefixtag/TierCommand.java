package com.cipollomods.prefixtag;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;

/**
 * TierCommand
 *
 * Registra y gestiona todos los comandos del mod.
 * Hay dos grupos de comandos:
 *
 *   /tier — Comandos de administrador (requieren op level 2)
 *     setrol <jugador> <1-5> → Asigna el tier de Rol a un jugador
 *     setpvp <jugador> <1-5> → Asigna el tier de PvP a un jugador
 *     check  <jugador>       → Muestra el prefijo actual de un jugador
 *     reset  <jugador>       → Reinicia los tiers, verá la GUI al reconectarse
 *
 *   /tierself — Comando interno usado exclusivamente por la GUI
 *     rol <1-5> → El jugador asigna su propio tier de Rol (solo si no tiene uno)
 *     pvp <1-5> → El jugador asigna su propio tier de PvP (solo si no tiene uno)
 */

@Mod.EventBusSubscriber(modid = PrefixTag.MOD_ID)
public class TierCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        // ─ Comandos de admin: /tier ---------------------------------------
        // Requieren op level 2 — jugadores normales no pueden usarlos
        event.getDispatcher().register(
                Commands.literal("tier")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("online")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("valor", BoolArgumentType.bool())
                                                .executes(ctx -> setOnline(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        BoolArgumentType.getBool(ctx, "valor")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("setrol")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                .executes(ctx -> setRol(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("setpvp")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                .executes(ctx -> setPvp(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        IntegerArgumentType.getInteger(ctx, "tier")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("check")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .executes(ctx -> check(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jugador")
                                        ))
                                )
                        )

                        .then(Commands.literal("reset")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .executes(ctx -> reset(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jugador")
                                        ))
                                )
                        )
                        .then(Commands.literal("setcolor")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("color", StringArgumentType.string())
                                                .executes(ctx -> setColor(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jugador"),
                                                        StringArgumentType.getString(ctx, "color")
                                                ))
                                        )
                                )
                        )

        );

        // ── Comando interno para la GUI: /tierself ------------------------------------------
        // Sin restricción de permisos — cualquier jugador puede ejecutarlo
        // pero los métodos comprueban internamente que el tier no esté ya asignado
        event.getDispatcher().register(
                Commands.literal("tierself")

                        .then(Commands.literal("rol")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(ctx -> setSelfRol(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))
                                )
                        )

                        .then(Commands.literal("pvp")
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                        .executes(ctx -> setSelfPvp(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "tier")
                                        ))
                                )
                        )
        );
    }

    // ─ /tier setrol ----------------------------------------------------------

    /**
     * Asigna el tier de Rol a un jugador concreto.
     * Puede sobreescribir un tier existente — es un comando de admin.
     * Notifica tanto al admin que ejecuta el comando como al jugador afectado.
     */
    private static int setRol(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

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

    // ─ /tier setpvp ----------------------------------------------------------

    /**
     * Asigna el tier de PvP a un jugador concreto.
     * Puede sobreescribir un tier existente — es un comando de admin.
     * Notifica tanto al admin que ejecuta el comando como al jugador afectado.
     */
    private static int setPvp(CommandSourceStack source, ServerPlayer target, int tier) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

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

    // ─ /tier check ----------------------------------------------------------
    /**
     * Muestra el prefijo actual de un jugador al admin que ejecuta el comando.
     * No notifica al jugador afectado.
     */
    private static int check(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "§f" + target.getName().getString() + " — " + data.getPrefix()
        ), false);

        return 1;
    }

    // ─ /tierself rol ----------------------------------------------------------
    /**
     * Permite al jugador asignarse su propio tier de Rol.
     * Solo funciona si el jugador aún no tiene un tier de Rol asignado.
     * Este comando lo ejecuta la GUI automáticamente — no está pensado
     * para ser usado manualmente por los jugadores.
     */
    private static int setSelfRol(CommandSourceStack source, int tier) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());

            if (data == null) return 0;

            // Solo permitir si aún no tiene rol asignado
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

    // ─ /tierself pvp ---------------------------------------------------
    /**
     * Permite al jugador asignarse su propio tier de PvP.
     * Solo funciona si el jugador aún no tiene un tier de PvP asignado.
     * Este comando lo ejecuta la GUI automáticamente — no está pensado
     * para ser usado manualmente por los jugadores.
     */
    private static int setSelfPvp(CommandSourceStack source, int tier) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            PlayerTierData data = TierEventHandler.getPlayerData(player.getUUID());

            if (data == null) return 0;

            // Solo permitir si aún no tiene pvp asignado
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

    // ── /tier reset ----------------------------------------------------------
    /**
     * Reinicia ambos tiers de un jugador a -1 (sin asignar).
     * La próxima vez que el jugador entre al servidor verá la GUI de selección.
     * Notifica tanto al admin como al jugador afectado.
     */
    private static int reset(CommandSourceStack source, ServerPlayer target) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

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

    // ── /tier setcolor ────────────────────────────────────────────────────────

    /**
     * Asigna el color del nombre a un jugador concreto.
     * Solo afecta al nombre, no al prefijo.
     * Colores disponibles: black, dark_blue, dark_green, dark_aqua, dark_red,
     * dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white
     */
    private static int setColor(CommandSourceStack source, ServerPlayer target, String color) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

        // Verificar que el color es válido
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

    /**
     * Asigna modo OnRol
     */
    private static int setOnline(CommandSourceStack source, ServerPlayer target, boolean value) {
        PlayerTierData data = TierEventHandler.getPlayerData(target.getUUID());

        if (data == null) {
            source.sendFailure(Component.literal("No se encontraron datos para " + target.getName().getString()));
            return 0;
        }

        data.setShowOnline(value);
        TierEventHandler.savePlayerData(target);

        source.sendSuccess(() -> Component.literal(
                "§aIndicador online de " + target.getName().getString() + " " + (value ? "§aactivado" : "§cdesactivado")
        ), true);

        return 1;
    }
}