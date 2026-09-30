package dev.waldq.mipp.client;

import dev.waldq.mipp.MIPPClient;

public final class ThroughputColor {
    private static final double LOW = 60.0;
    private static final double HIGH = 1000.0;
    private static final double MAX = 2000.0;
    
    private final int r, g, b, a;

    public ThroughputColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = Math.clamp((int) (a - a * 0.4 * (g - Math.max(r, b)) / 255.0), 0, 255);
    }

    public ThroughputColor(int value) {
        this(
                red(value),
                green(value),
                blue(value),
                MIPPClient.config().pipeNetworkAnalyzer().useFullBlockOverlay() ? 70 : 170
        );
    }

    private static int red(int value) {
        return value <= LOW ? Math.clamp(Math.round((LOW - value) / LOW * 255), 0, 255) : (
                value <= HIGH ? 0 : (
                        value <= MAX ? Math.clamp(Math.round((value - HIGH) / (MAX - HIGH) * 160), 0, 255) : 160
                        )
                );
    }

    private static int green(int value) {
        return value <= LOW ? Math.clamp(Math.round(value / LOW * 255), 0, 255) : (
                value <= HIGH ? Math.clamp(Math.round((HIGH - value) / (HIGH - LOW) * 255), 0, 255) : 0
                );
    }

    private static int blue(int value) {
        return value <= LOW ? 0 : (
                value <= HIGH ? Math.clamp(Math.round((value - LOW) / (HIGH - LOW) * 255), 0, 255) : 255
                );
    }


    /**
     * @param extracted last extracted amount
     * @param inserted last inserted amount
     */
    public static ThroughputColor fromValue(int extracted, int inserted) {
        if (extracted > 0) return new ThroughputColor(extracted);
        if (inserted > 0) return new ThroughputColor(inserted);
        return new ThroughputColor(0);
    }

    public int getHex() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public float af() { return a / 255f; }
    public float rf() { return r / 255f; }
    public float gf() { return g / 255f; }
    public float bf() { return b / 255f; }
}