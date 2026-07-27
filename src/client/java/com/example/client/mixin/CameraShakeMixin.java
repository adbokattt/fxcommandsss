package com.example.client.mixin;

import com.example.client.CameraShakeManager;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Внедряет тряску камеры в {@link Camera#setup}.
 * <p>
 * ВАЖНО: имя метода {@code setup} и таргет {@code setRotation(FF)V}
 * соответствуют официальным (Mojang) маппингам на момент написания под 1.21.9.
 * Если после апдейта Minecraft/маппингов Mixin перестанет применяться —
 * проверьте актуальные имена через {@code ./gradlew genSources} и IDE
 * ("Go to declaration" по классу {@code Camera}).
 */
@Mixin(Camera.class)
public abstract class CameraShakeMixin {

    @Shadow
    @Final
    private Quaternionf rotation;

    @Inject(method = "setup", at = @At("HEAD"))
    private void fxcommands$updateShakeEveryFrame(CallbackInfo ci) {
        CameraShakeManager.updateFrame();
    }

    @ModifyArgs(
            method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    )
    private void fxcommands$applyShakeToRotation(Args args) {
        if (CameraShakeManager.isShaking()) {
            float yaw = args.get(0);
            float pitch = args.get(1);
            args.set(0, yaw + CameraShakeManager.getShakeYaw());
            args.set(1, pitch + CameraShakeManager.getShakePitch());
        }
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void fxcommands$applyRollShake(CallbackInfo ci) {
        if (CameraShakeManager.isShaking()) {
            float rollRadians = CameraShakeManager.getShakeRoll() * ((float) Math.PI / 180F);
            this.rotation.mul(new Quaternionf().rotationZ(rollRadians));
        }
    }
}
