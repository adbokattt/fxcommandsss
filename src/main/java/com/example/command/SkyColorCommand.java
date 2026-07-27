package com.example.command;

import com.example.network.SkyColorPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SkyColorCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("setskycolor")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("r", IntegerArgumentType.integer(0, 255))
                        .then(CommandManager.argument("g", IntegerArgumentType.integer(0, 255))
                                .then(CommandManager.argument("b", IntegerArgumentType.integer(0, 255))
                                        .then(CommandManager.argument("duration", FloatArgumentType.floatArg(0.05F))
                                                .executes(SkyColorCommand::execute))))));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        int r = IntegerArgumentType.getInteger(ctx, "r");
        int g = IntegerArgumentType.getInteger(ctx, "g");
        int b = IntegerArgumentType.getInteger(ctx, "b");
        float durationSeconds = FloatArgumentType.getFloat(ctx, "duration");
        int durationTicks = Math.max(1, Math.round(durationSeconds * 20.0F));

        SkyColorPayload payload = new SkyColorPayload(r, g, b, durationTicks);

        int count = 0;
        for (ServerPlayerEntity player : PlayerLookup.all(ctx.getSource().getServer())) {
            ServerPlayNetworking.send(player, payload);
            count++;
        }

        int sentTo = count;
        ctx.getSource().sendFeedback(() -> Text.literal(
                "Цвет неба изменён на RGB(" + r + ", " + g + ", " + b + ") на " + durationSeconds
                        + " сек. для " + sentTo + " игрок(ов)."), true);
        return count;
    }
}
