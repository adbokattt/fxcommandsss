package com.example.client;

import net.minecraft.world.phys.Vec3;

/**
 * Чисто клиентское состояние тряски камеры.
 * <p>
 * {@link #tick()} вызывается раз в игровой тик (20 раз/сек) и считает
 * "целевое" смещение камеры на основе плавного (simplex-подобного) шума.
 * {@link #updateFrame()} вызывается каждый кадр рендера из {@link com.example.client.mixin.CameraShakeMixin}
 * и плавно (лерпом) подтягивает текущее смещение к целевому — это убирает
 * "рывки", если fps выше tps.
 */
public final class CameraShakeManager {

    private static final float SMOOTHING = 0.35F;

    private static float shakePower = 0.0F;
    private static int shakeDuration = 0;
    private static int shakeTimer = 0;

    private static Vec3 shakeOffset = Vec3.ZERO;
    private static Vec3 targetShakeOffset = Vec3.ZERO;
    private static float shakeRoll = 0.0F;
    private static float targetShakeRoll = 0.0F;
    private static float noiseTime = 0.0F;

    private CameraShakeManager() {
    }

    /**
     * Запускает (или перезапускает) тряску камеры.
     *
     * @param power         сила/амплитуда тряски
     * @param durationTicks длительность в тиках
     */
    public static void startShake(float power, int durationTicks) {
        shakePower = power;
        shakeDuration = durationTicks;
        shakeTimer = 0;
        noiseTime = 0.0F;
    }

    /** Вызывается раз в тик. */
    public static void tick() {
        if (shakeTimer < shakeDuration) {
            shakeTimer++;
            float progress = (float) shakeTimer / (float) shakeDuration;
            float decay = 1.0F - progress;
            float trauma = shakePower * decay * decay;

            noiseTime += 0.6F;
            double noiseX = simplexNoise(noiseTime, 100.0F) * trauma * 2.2;
            double noiseY = simplexNoise(noiseTime, 200.0F) * trauma * 2.2;
            double noiseZ = simplexNoise(noiseTime, 300.0F) * trauma * 1.5;

            targetShakeOffset = new Vec3(noiseX, noiseY, 0.0);
            targetShakeRoll = (float) noiseZ;
        } else {
            // Эффект закончился — плавно гасим оставшееся смещение, а не обрываем резко.
            targetShakeOffset = targetShakeOffset.scale(0.85);
            targetShakeRoll *= 0.85F;

            if (targetShakeOffset.lengthSqr() < 1.0E-4) {
                targetShakeOffset = Vec3.ZERO;
            }
            if (Math.abs(targetShakeRoll) < 0.01F) {
                targetShakeRoll = 0.0F;
            }
        }
    }

    /** Вызывается каждый кадр (из mixin) — сглаживает переход к целевому смещению. */
    public static void updateFrame() {
        shakeOffset = shakeOffset.lerp(targetShakeOffset, SMOOTHING);
        shakeRoll += (targetShakeRoll - shakeRoll) * SMOOTHING;
    }

    public static float getShakeYaw() {
        return (float) shakeOffset.x;
    }

    public static float getShakePitch() {
        return (float) shakeOffset.y;
    }

    public static float getShakeRoll() {
        return shakeRoll;
    }

    /** true, пока идёт активная тряска ИЛИ пока не догасло остаточное смещение. */
    public static boolean isShaking() {
        return shakeTimer < shakeDuration || targetShakeOffset.lengthSqr() > 1.0E-4 || shakeOffset.lengthSqr() > 1.0E-4;
    }

    public static void stopShake() {
        shakeDuration = 0;
        shakeTimer = 0;
        shakeOffset = Vec3.ZERO;
        targetShakeOffset = Vec3.ZERO;
        shakeRoll = 0.0F;
        targetShakeRoll = 0.0F;
    }

    // ---- Простой 2D simplex-подобный шум, чтобы тряска была "органичной", а не белым шумом ----

    private static float simplexNoise(float x, float y) {
        final float f2 = 0.3660254F;
        final float g2 = 0.21132487F;

        float s = (x + y) * f2;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        float t = (i + j) * g2;

        float x0 = x - (i - t);
        float y0 = y - (j - t);

        int i1;
        int j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + g2;
        float y1 = y0 - j1 + g2;
        float x2 = x0 - 1.0F + 2.0F * g2;
        float y2 = y0 - 1.0F + 2.0F * g2;

        float n0 = corner(x0, y0, i, j);
        float n1 = corner(x1, y1, i + i1, j + j1);
        float n2 = corner(x2, y2, i + 1, j + 1);

        return 70.0F * (n0 + n1 + n2);
    }

    private static float corner(float x, float y, int i, int j) {
        float t = 0.5F - x * x - y * y;
        if (t < 0.0F) {
            return 0.0F;
        }
        t *= t;
        return t * t * grad(hash(i, j), x, y);
    }

    private static int hash(int i, int j) {
        return (i * 374761393 + j * 668265263) & Integer.MAX_VALUE;
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 7;
        float u = h < 4 ? x : y;
        float v = h < 4 ? y : x;
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -2.0F * v : 2.0F * v);
    }

    private static int fastFloor(float x) {
        return x > 0.0F ? (int) x : (int) x - 1;
    }
}
