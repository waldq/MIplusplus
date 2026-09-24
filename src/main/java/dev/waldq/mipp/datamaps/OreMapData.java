package dev.waldq.mipp.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record OreMapData(int hexColor, Optional<ResourceLocation> replacement) {
    public static final Codec<OreMapData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(
                    hex -> (int) Long.parseLong(hex.startsWith("#") ? hex.substring(1) : hex, 16),
                    color -> String.format("%06X", color)
            ).fieldOf("color").forGetter(OreMapData::hexColor),
            ResourceLocation.CODEC.optionalFieldOf("replacement").forGetter(OreMapData::replacement)
    ).apply(i, OreMapData::new));
}
