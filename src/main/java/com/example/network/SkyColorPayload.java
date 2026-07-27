package com.example.network;

import com.example.Fxccommands;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SkyColorPayload(int red, int green, int blue, int durationTicks) implements CustomPayload {

    public static final CustomPayload.Id<SkyColorPayload> ID =
            new CustomPayload.Id<>(Fxccommands.id("sky_color"));

    public static final PacketCodec<RegistryByteBuf, SkyColorPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SkyColorPayload::red,
            PacketCodecs.VAR_INT, SkyColorPayload::green,
            PacketCodecs.VAR_INT, SkyColorPayload::blue,
            PacketCodecs.VAR_INT, SkyColorPayload::durationTicks,
            SkyColorPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
