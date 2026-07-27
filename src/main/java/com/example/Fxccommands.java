package com.example;

import com.example.command.CameraShakeCommand;
import com.example.command.SkyColorCommand;
import com.example.network.CameraShakePayload;
import com.example.network.SkyColorPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа мода на стороне сервера (common).
 * <p>
 * Мод не содержит игровой логики метеоритов — только две команды
 * ({@code /camerashake} и {@code /setskycolor}) и сетевые пакеты,
 * которые заставляют клиент воспроизвести визуальный эффект.
 */
public class Fxccommands implements ModInitializer {

    public static final String MOD_ID = "fxccommands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Регистрируем типы кастомных пакетов Server -> Client.
        PayloadTypeRegistry.playS2C().register(CameraShakePayload.TYPE, CameraShakePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SkyColorPayload.TYPE, SkyColorPayload.STREAM_CODEC);

        // Команды регистрируются ИСКЛЮЧИТЕЛЬНО на сервере.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CameraShakeCommand.register(dispatcher);
            SkyColorCommand.register(dispatcher);
        });

        LOGGER.info("[{}] server-side commands registered: /camerashake, /setskycolor", MOD_ID);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
