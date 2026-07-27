package com.example.client.mixin;

import com.example.client.SkyColorManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Подменяет итоговый цвет неба, который ванильный рендерер вычисляет в
 * {@link ClientLevel#getSkyColor(Vec3, float)}. Этот метод используется
 * и для купола неба, и участвует в расчёте цвета тумана/горизонта — поэтому
 * одной точки внедрения достаточно, чтобы перекрасить небо целиком.
 * <p>
 * ВАЖНО: сверьте точную сигнатуру {@code getSkyColor(Vec3, float)} через
 * {@code ./gradlew genSources}, если версия Minecraft поменяется.
 */
@Mixin(ClientLevel.class)
public abstract class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void fxcommands$overrideSkyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (SkyColorManager.isActive()) {
            cir.setReturnValue(SkyColorManager.applyOverride(cir.getReturnValue()));
        }
    }
}
