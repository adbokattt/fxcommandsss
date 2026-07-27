package com.example.client.mixin;

import com.example.client.SkyColorManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void fxcommands$overrideSkyColor(Vec3d pos, float partialTick, CallbackInfoReturnable<Vec3d> cir) {
        if (SkyColorManager.isActive()) {
            cir.setReturnValue(SkyColorManager.applyOverride(cir.getReturnValue()));
        }
    }
}
