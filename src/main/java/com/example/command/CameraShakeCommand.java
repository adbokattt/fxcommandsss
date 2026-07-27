package com.example.command;

import com.example.network.CameraShakePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;

/**
 * /camerashake <duration_seconds> [power] [targets]
 * <p>
 * - duration_seconds — длительность тряски в секундах (обязательный).
 * - power            — сила тряски, по умолчанию 1.0 (необязательный).
 * - targets          — селектор игроков (@s, @a, @p, ник и т.д.).
 *                       Если не указан — тряска применяется только к вызвавшему игроку.
 *                       Чтобы затрясти всех игроков сервера, укажите {@code @a}.
 * <p>
 * Сама тряска целиком выполняется на клиенте — сервер лишь рассылает
 * {@link CameraShakePayload} нужным игрокам.
 */
public class CameraShakeCommand {

    private static final float DEFAULT_POWER = 1.0F;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("camerashake")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("duration", FloatArgumentType.floatArg(0.05F))
                        .executes(ctx -> execute(ctx, DEFAULT_POWER, resolveSelf(ctx)))
                        .then(Commands.argument("power", FloatArgumentType.floatArg(0.0F))
                                .executes(ctx -> execute(ctx, FloatArgumentType.getFloat(ctx, "power"), resolveSelf(ctx)))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> execute(
                                                ctx,
                                                FloatArgumentType.getFloat(ctx, "power"),
                                                EntityArgument.getPlayers(ctx, "targets")))))));
    }

    private static Collection<ServerPlayer> resolveSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(player);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, float power, Collection<ServerPlayer> targets) {
        float durationSeconds = FloatArgumentType.getFloat(ctx, "duration");
        int durationTicks = Math.max(1, Math.round(durationSeconds * 20.0F));

        if (targets.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                    "Не удалось определить цель — укажите игроков явно (например, @a) или вызовите команду от лица игрока."));
            return 0;
        }

        CameraShakePayload payload = new CameraShakePayload(power, durationTicks);
        for (ServerPlayer target : targets) {
            ServerPlayNetworking.send(target, payload);
        }

        int count = targets.size();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Тряска камеры (сила " + power + ") на " + durationSeconds + " сек. запущена для " + count + " игрок(ов)."), true);
        return count;
    }
}
