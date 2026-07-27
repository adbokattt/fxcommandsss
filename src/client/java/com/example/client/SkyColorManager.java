package com.example.client;

import net.minecraft.world.phys.Vec3;

/**
 * Чисто клиентское состояние временной перекраски неба.
 * <p>
 * Логика фаз (по реальному времени, не по тикам — так эффект остаётся
 * плавным независимо от tps/лагов сервера):
 * <ol>
 *     <li>Fade-in ({@link #FADE_MS} мс) — плавный переход от обычного цвета к заданному;</li>
 *     <li>Hold — небо держит заданный цвет весь оставшийся указанный срок;</li>
 *     <li>Fade-out ({@link #FADE_MS} мс) — плавный возврат к обычному цвету неба.</li>
 * </ol>
 */
public final class SkyColorManager {

    private static final long FADE_MS = 1000L;

    private static boolean active = false;
    private static long startTimeMs;
    private static long holdDurationMs;
    private static Vec3 targetColor = Vec3.ZERO;

    private SkyColorManager() {
    }

    /**
     * Запускает перекраску неба.
     *
     * @param red           0-255
     * @param green         0-255
     * @param blue          0-255
     * @param durationTicks сколько тиков всего должен длиться эффект (включая fade-in/out)
     */
    public static void startOverride(int red, int green, int blue, int durationTicks) {
        targetColor = new Vec3(
                clamp01(red / 255.0),
                clamp01(green / 255.0),
                clamp01(blue / 255.0));

        long totalMs = durationTicks * 50L; // 1 тик = 50 мс
        // Если запрошенная длительность короче, чем два fade-перехода,
        // просто ужимаем fade-in/out, чтобы эффект не "сломался" на коротких значениях.
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

    /**
     * Возвращает "смешанный" цвет неба: исходный ванильный {@code originalColor}
     * плавно замещается на {@link #targetColor} в зависимости от текущей фазы.
     */
    public static Vec3 applyOverride(Vec3 originalColor) {
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
