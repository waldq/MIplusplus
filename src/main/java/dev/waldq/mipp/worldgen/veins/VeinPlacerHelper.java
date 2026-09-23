package dev.waldq.mipp.worldgen.veins;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.*;
import java.util.function.Predicate;

public class VeinPlacerHelper {
    public record TargetRule(
            Predicate<BlockState> matcher,
            NavigableMap<Integer, ResourceLocation> oreWeightMap,
            int totalWeight
    ) {}

    private final List<TargetRule> targetRules = new ArrayList<>();

    private final NavigableMap<Integer, ResourceLocation> rareWeightMap = new TreeMap<>();
    private int totalRare = 0;

    private final NavigableMap<Integer, ResourceLocation> sampleWeightMap = new TreeMap<>();
    private int totalSample = 0;

    public VeinPlacerHelper(Map<String, Map<ResourceLocation, Integer>> oreWeights) {
        fillOreWeightMap(oreWeights);
    }

    public VeinPlacerHelper(
            Map<String, Map<ResourceLocation, Integer>> oreWeights,
            Map<ResourceLocation, Integer> rareBlocks,
            Map<ResourceLocation, Integer> sampleWeights
    ) {
        fillOreWeightMap(oreWeights);
        if (rareBlocks != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : rareBlocks.entrySet()) {
                var weight = entry.getValue();
                totalRare += weight;
                rareWeightMap.put(totalRare, entry.getKey());
            }
        }
        if (sampleWeights != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : sampleWeights.entrySet()) {
                var weight = entry.getValue();
                totalSample += weight;
                sampleWeightMap.put(totalSample, entry.getKey());
            }
        }
    }

    private void fillOreWeightMap(Map<String, Map<ResourceLocation, Integer>> oreWeights) {
        for (Map.Entry<String, Map<ResourceLocation, Integer>> blockEntry : oreWeights.entrySet()) {
            String target = blockEntry.getKey();

            Predicate<BlockState> matcher = createMatcher(target);

            NavigableMap<Integer, ResourceLocation> weightMap = new TreeMap<>();
            var totalOre = 0;

            for (Map.Entry<ResourceLocation, Integer> entry : blockEntry.getValue().entrySet()) {
                var weight = entry.getValue();
                totalOre += weight;
                weightMap.put(totalOre, entry.getKey());
            }

            if (totalOre > 0) {
                targetRules.add(new TargetRule(matcher, weightMap, totalOre));
            }
        }
    }

    private static Predicate<BlockState> createMatcher(String target) {
        if (target.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.parse(target.substring(1));
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
            return state -> state.is(tagKey);
        } else {
            ResourceLocation blockId = ResourceLocation.parse(target);
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            return state -> state.is(block);
        }
    }

    public TargetRule getMatchingRule(BlockState state) {
        for (TargetRule rule : targetRules) {
            if (rule.matcher().test(state)) {
                return rule;
            }
        }
        return null;
    }

    public ResourceLocation getNextOre(BlockState stateToReplace, RandomSource random) {
        TargetRule rule = getMatchingRule(stateToReplace);
        if (rule == null || rule.totalWeight() <= 0) return null;
        var nextOre = rule.oreWeightMap().higherEntry(random.nextInt(rule.totalWeight()));
        return nextOre != null ? nextOre.getValue() : null;
    }

    public ResourceLocation getNextRare(RandomSource random) {
        if (totalRare <= 0) return null;
        var nextRare = rareWeightMap.higherEntry(random.nextInt(totalRare));
        if (nextRare == null) { return null; }
        return nextRare.getValue();
    }

    public ResourceLocation getNextSample(RandomSource random) {
        if (totalSample <= 0) return null;
        var nextSample = sampleWeightMap.higherEntry(random.nextInt(totalSample));
        if (nextSample == null) { return null; }
        return nextSample.getValue();
    }

    public boolean useDensity(float density, RandomSource random) {
        return random.nextFloat() < density;
    }

    public boolean canReplace(BlockState state) { return getMatchingRule(state) != null; }


    public static Random seededRandom(long worldSeed, int chunkX, int chunkZ, ResourceLocation dimension) {
        long seed = worldSeed
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L)
                ^ dimension.toString().hashCode();
        return new Random(seed);
    }

    public static RandomSource seededRandomSource(long worldSeed, int chunkX, int chunkZ, ResourceLocation dimension) {
        long seed = worldSeed
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L)
                ^ dimension.toString().hashCode();
        return RandomSource.create(seed);
    }

    public static RandomSource seededXoroshiroRandomSource(long worldSeed, int chunkX, int chunkZ, ResourceLocation dimension) {
        long seed = worldSeed
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L)
                ^ dimension.toString().hashCode();
        return new XoroshiroRandomSource(seed);
    }
}
