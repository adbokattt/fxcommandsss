package com.example.network;

import com.example.Fxccommands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Пакет Server -> Client: приказывает клиенту запустить тряску камеры.
 *
 * @param power         "сила" тряски (амплитуда шума). 1.0 — стандартная сила.
 * @param durationTicks длительность эффекта в тиках (20 тиков = 1 секунда).
 */
public record CameraShakePayload(float power, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CameraShakePayload> TYPE =
            new CustomPacketPayload.Type<>(Fxccommands.id("camera_shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CameraShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, CameraShakePayload::power,
            ByteBufCodecs.VAR_INT, CameraShakePayload::durationTicks,
            CameraShakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
