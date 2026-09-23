package dev.waldq.mipp.worldgen.veins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.waldq.mipp.worldgen.veins.veintypes.VeinTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Map;

public record OreVeinConfig(
        ResourceLocation id,
        VeinData data,
        DimData dimData,
        Map<String, Map<ResourceLocation, Integer>> oreWeights,
        Map<ResourceLocation, Integer> rareBlocks,
        float rareBlockChance,
        VeinSize bounds,
        float discardChanceOnAirExposure,
        Samples samples,
        boolean enabled,
        VeinPlacerHelper placerHelper
) implements FeatureConfiguration {
    public OreVeinConfig(
            ResourceLocation id, VeinData data, DimData dimData,
            Map<String, Map<ResourceLocation, Integer>> oreWeights,
            Map<ResourceLocation, Integer> rareBlocks, float rareBlockChance,
            VeinSize bounds, float discardChanceOnAirExposure,
            Samples samples, boolean enabled
    ) {
        this(
                id, data, dimData, oreWeights, rareBlocks,
                rareBlockChance, bounds,  discardChanceOnAirExposure,
                samples, enabled,
                new VeinPlacerHelper(oreWeights, rareBlocks, samples.sampleWeights)
        );
    }

    public record DimData(List<ResourceLocation> dimensions, List<String> biomes) {
        public static final Codec<DimData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf()
                        .optionalFieldOf("dimensions", List.of(ResourceLocation.parse("minecraft:overworld")))
                        .forGetter(DimData::dimensions),
                Codec.STRING.listOf()
                        .optionalFieldOf("biomes", List.of())
                        .forGetter(DimData::biomes)
        ).apply(instance, DimData::new));
    }

    public record VeinData(VeinTypes type, int weight, float density) {
        public static final Codec<VeinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VeinTypes.CODEC.optionalFieldOf("type", VeinTypes.STOCK).forGetter(VeinData::type),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("weight", 10).forGetter(VeinData::weight),
                Codec.floatRange(0.01F, 1.0F).optionalFieldOf("density", 0.8F).forGetter(VeinData::density)
        ).apply(instance, VeinData::new));
    }

    public record VeinSize(int radiusX, int radiusY, int radiusZ, int minY, int maxY, float size) {
        public static final Codec<VeinSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("radiusX", 7).forGetter(VeinSize::radiusX),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("radiusY", 7).forGetter(VeinSize::radiusY),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("radiusZ", 7).forGetter(VeinSize::radiusZ),
                Codec.INT.fieldOf("minY").forGetter(VeinSize::minY),
                Codec.INT.fieldOf("maxY").forGetter(VeinSize::maxY),
                Codec.floatRange(0.1F, 5.0F).optionalFieldOf("size", 1.0F).forGetter(VeinSize::size)
        ).apply(instance, VeinSize::new));
    }

    public record Samples(int number, Map<ResourceLocation, Integer> sampleWeights) {
        public static final Codec<Samples> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 20).optionalFieldOf("number", 5).forGetter(Samples::number),
                Codec.unboundedMap(
                        ResourceLocation.CODEC,
                        Codec.intRange(1, Integer.MAX_VALUE)
                ).optionalFieldOf("sampleWeights", Map.of()).forGetter(Samples::sampleWeights)
        ).apply(instance, Samples::new));
    }

    public static final Codec<OreVeinConfig> CODEC = RecordCodecBuilder.create(vein -> vein.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(OreVeinConfig::id),

            VeinData.CODEC.fieldOf("data").forGetter(OreVeinConfig::data),

            DimData.CODEC.fieldOf("dimData").forGetter(OreVeinConfig::dimData),

            Codec.unboundedMap(
                    Codec.STRING,
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.intRange(1, Integer.MAX_VALUE))
            ).optionalFieldOf("oreWeights", Map.of()).forGetter(OreVeinConfig::oreWeights),

            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    Codec.intRange(1, Integer.MAX_VALUE)
                    ).optionalFieldOf("rareBlocks", Map.of(ResourceLocation.parse("minecraft:air"), 10)).forGetter(OreVeinConfig::rareBlocks),

            Codec.FLOAT.optionalFieldOf("rareBlockChance", 0F).forGetter(OreVeinConfig::rareBlockChance),

            VeinSize.CODEC.fieldOf("bounds").forGetter(OreVeinConfig::bounds),

            Codec.floatRange(0.01F, 1.0F).optionalFieldOf("discardChanceOnAirExposure", 0.8F).forGetter(OreVeinConfig::discardChanceOnAirExposure),

            Samples.CODEC.fieldOf("samples").forGetter(OreVeinConfig::samples),

            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(OreVeinConfig::enabled)
    ).apply(vein, OreVeinConfig::new));
}
