package com.example.network;

import com.example.Fxccommands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Пакет Server -> Client: приказывает клиенту временно перекрасить небо.
 *
 * @param red           канал R (0-255)
 * @param green         канал G (0-255)
 * @param blue          канал B (0-255)
 * @param durationTicks сколько тиков небо держит указанный цвет,
 *                      прежде чем плавно вернуться к обычному
 */
public record SkyColorPayload(int red, int green, int blue, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SkyColorPayload> TYPE =
            new CustomPacketPayload.Type<>(Fxccommands.id("sky_color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkyColorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SkyColorPayload::red,
            ByteBufCodecs.VAR_INT, SkyColorPayload::green,
            ByteBufCodecs.VAR_INT, SkyColorPayload::blue,
            ByteBufCodecs.VAR_INT, SkyColorPayload::durationTicks,
            SkyColorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
