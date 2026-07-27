package com.example.command;

import com.example.network.SkyColorPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /setskycolor <r> <g> <b> <duration_seconds>
 * <p>
 * Небо — общий визуальный элемент мира, поэтому команда рассылает пакет
 * всем игрокам, находящимся сейчас на сервере (см. {@link PlayerLookup#all}).
 * Каждый клиент самостоятельно плавно перекрашивает и возвращает небо обратно.
 */
public class SkyColorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setskycolor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("r", IntegerArgumentType.integer(0, 255))
                        .then(Commands.argument("g", IntegerArgumentType.integer(0, 255))
                                .then(Commands.argument("b", IntegerArgumentType.integer(0, 255))
                                        .then(Commands.argument("duration", FloatArgumentType.floatArg(0.05F))
                                                .executes(SkyColorCommand::execute))))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        int r = IntegerArgumentType.getInteger(ctx, "r");
        int g = IntegerArgumentType.getInteger(ctx, "g");
        int b = IntegerArgumentType.getInteger(ctx, "b");
        float durationSeconds = FloatArgumentType.getFloat(ctx, "duration");
        int durationTicks = Math.max(1, Math.round(durationSeconds * 20.0F));

        SkyColorPayload payload = new SkyColorPayload(r, g, b, durationTicks);

        int count = 0;
        for (ServerPlayer player : PlayerLookup.all(ctx.getSource().getServer())) {
            ServerPlayNetworking.send(player, payload);
            count++;
        }

        int sentTo = count;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Цвет неба изменён на RGB(" + r + ", " + g + ", " + b + ") на " + durationSeconds
                        + " сек. для " + sentTo + " игрок(ов)."), true);
        return count;
    }
}
