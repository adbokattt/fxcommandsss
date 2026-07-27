package com.example.client;

import com.example.network.CameraShakePayload;
import com.example.network.SkyColorPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Точка входа клиентской части мода.
 * <p>
 * Здесь только приём двух пакетов от сервера и тик менеджера тряски —
 * никакой игровой логики метеоритов не осталось.
 */
public class FxccommandsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CameraShakePayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        CameraShakeManager.startShake(payload.power(), payload.durationTicks())));

        ClientPlayNetworking.registerGlobalReceiver(SkyColorPayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        SkyColorManager.startOverride(payload.red(), payload.green(), payload.blue(), payload.durationTicks())));

        ClientTickEvents.END_CLIENT_TICK.register(client -> CameraShakeManager.tick());
    }
}
