package dev.waldq.mipp.client;

public final class ThroughputColor {
    private final int r, g, b, a;

    public ThroughputColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ThroughputColor(double value) {
        this(
                value <= 60
                        ? Math.clamp((int) Math.round((60.0 - value) / 60.0 * 255), 0, 255)
                        : Math.clamp((int) Math.round((value - 60.0) / (1000.0 - 60.0) * 128), 0, 128),
                value <= 60
                        ? Math.clamp((int) Math.round(value / 60.0 * 255), 0, 255)
                        : Math.clamp((int) Math.round((1000.0 - value) / (1000.0 - 60.0) * 255), 0, 255),
                value <= 60
                        ? 0
                        : Math.clamp((int) Math.round((value - 60.0) / (1000.0 - 60.0) * 128), 0, 128),
                255
        );
    }


    /**
     * @param extracted last extracted amount
     * @param inserted last inserted amount
     */
    public static ThroughputColor fromValue(double extracted, double inserted) {
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