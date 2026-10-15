package fun.minerworld.minervisuals;

/** Состояние модуля China Hat. */
public final class ChinaHat {
    public enum HatColor {
        GREEN("Green", 0x2ECC40),
        YELLOW("Yellow", 0xFFDC00),
        PURPLE("Purple", 0xB44DFF);

        public final String label;
        public final int rgb;

        HatColor(String label, int rgb) {
            this.label = label;
            this.rgb = rgb;
        }
    }

    public static boolean enabled = false;
    public static HatColor color = HatColor.GREEN;

    /** 0..1 — прогресс анимации включения/выключения. */
    private static float anim = 0f;
    private static long last = System.nanoTime();

    private ChinaHat() {}

    /** Вызывать один раз за кадр. */
    public static void tick() {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
        last = now;
        float step = dt / 0.45f; // 0.45 сек на анимацию
        if (enabled) anim = Math.min(1f, anim + step);
        else anim = Math.max(0f, anim - step);
    }

    /** Сглаженный прогресс анимации (smoothstep). */
    public static float progress() {
        return anim * anim * (3f - 2f * anim);
    }
}
