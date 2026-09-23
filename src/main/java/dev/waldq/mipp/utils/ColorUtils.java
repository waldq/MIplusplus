package dev.waldq.mipp.utils;

import net.minecraft.world.level.material.MapColor;

public class ColorUtils {
    public record ColorEntry(String id, String englishName, MapColor mapColor, int hex) {}

    public static final ColorEntry[] COLORS = new ColorEntry[] {
            new ColorEntry("white", "White", MapColor.SNOW, 0xF9FFFE),
            new ColorEntry("light_gray", "Light Gray", MapColor.COLOR_LIGHT_GRAY, 0x9D9D97),
            new ColorEntry("gray", "Gray", MapColor.COLOR_GRAY, 0x474F52),
            new ColorEntry("black", "Black", MapColor.COLOR_BLACK, 0x1D1D21),
            new ColorEntry("brown", "Brown", MapColor.COLOR_BROWN, 0x835432),
            new ColorEntry("red", "Red", MapColor.COLOR_RED, 0xB02E26),
            new ColorEntry("orange", "Orange", MapColor.COLOR_ORANGE, 0xF9801D),
            new ColorEntry("yellow", "Yellow", MapColor.COLOR_YELLOW, 0xFED83D),
            new ColorEntry("lime", "Lime", MapColor.COLOR_LIGHT_GREEN, 0x80C71F),
            new ColorEntry("green", "Green", MapColor.COLOR_GREEN, 0x5E7C16),
            new ColorEntry("cyan", "Cyan", MapColor.COLOR_CYAN, 0x169C9C),
            new ColorEntry("light_blue", "Light Blue", MapColor.COLOR_LIGHT_BLUE, 0x3AB3DA),
            new ColorEntry("blue", "Blue", MapColor.COLOR_BLUE, 0x3C44AA),
            new ColorEntry("purple", "Purple", MapColor.COLOR_PURPLE, 0x8932B8),
            new ColorEntry("magenta", "Magenta", MapColor.COLOR_MAGENTA, 0xC74EBD),
            new ColorEntry("pink", "Pink", MapColor.COLOR_PINK, 0xF38BAA)
    };
}
