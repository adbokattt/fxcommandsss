package com.example.client;

import net.minecraft.util.math.Vec3d;

public final class SkyColorManager {

    private static final long FADE_MS = 1000L;

    private static boolean active = false;
    private static long startTimeMs;
    private static long holdDurationMs;
    private static Vec3d targetColor = Vec3d.ZERO;

    private SkyColorManager() {
    }

    public static void startOverride(int red, int green, int blue, int durationTicks) {
        targetColor = new Vec3d(
                clamp01(red / 255.0),
                clamp01(green / 255.0),
                clamp01(blue / 255.0));

        long totalMs = durationTicks * 50L;
        holdDurationMs = Math.max(0L, totalMs - FADE_MS - FADE_MS);
        startTimeMs = System.currentTimeMillis();
        active = true;
    }

    public static boolean isActive() {
        if (!active) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - startTimeMs;
        long total = FADE_MS + holdDurationMs + FADE_MS;
        if (elapsed > total) {
            active = false;
            return false;
        }
        return true;
    }

    public static Vec3d applyOverride(Vec3d originalColor) {
        if (!isActive()) {
            return originalColor;
        }

        long elapsed = System.currentTimeMillis() - startTimeMs;
        float alpha;

        if (elapsed < FADE_MS) {
            alpha = (float) elapsed / FADE_MS;
        } else if (elapsed < FADE_MS + holdDurationMs) {
            alpha = 1.0F;
        } else {
            long fadeOutElapsed = elapsed - FADE_MS - holdDurationMs;
            alpha = 1.0F - (float) fadeOutElapsed / FADE_MS;
        }

        alpha = Math.max(0.0F, Math.min(1.0F, alpha));
        return originalColor.lerp(targetColor, alpha);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
