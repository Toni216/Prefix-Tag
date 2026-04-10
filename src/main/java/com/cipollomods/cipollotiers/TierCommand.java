package com.cipollomods.cipollotiers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CipolloTiers.MOD_ID)
public class TierCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        // Comandos de admin: /tier
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
        );

        // Comando interno para la GUI (sin restricción de permisos): /tierself
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

    // ── /tier setrol ──────────────────────────────────────────────────────────

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

    // ── /tier setpvp ──────────────────────────────────────────────────────────

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

    // ── /tier check ───────────────────────────────────────────────────────────

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
    // ── /cipollotiers setself rol ─────────────────────────────────────────────

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

// ── /cipollotiers setself pvp ─────────────────────────────────────────────

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
}