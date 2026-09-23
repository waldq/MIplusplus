package dev.waldq.mipp.worldgen.veins;

import dev.waldq.mipp.worldgen.utils.FeatureUtils;
import dev.waldq.mipp.worldgen.utils.SampleUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class VeinPlacer {
    static final ResourceLocation AIR_BLOCK = ResourceLocation.fromNamespaceAndPath("minecraft", "air");

    public static boolean tryPlaceVein(
            OreVeinConfig vein,
            VeinPlacerHelper placerHelper,
            BlockState existing,
            LevelChunkSection section,
            int relX,
            int relY,
            int relZ,
            int worldY,
            RandomSource random) {
        if (!placerHelper.canReplace(existing)) return false;

        if (worldY < vein.bounds().minY() || worldY > vein.bounds().maxY()) return false;
        if (!placerHelper.useDensity(vein.data().density(), random)) return false;

        ResourceLocation replacementOre = placerHelper.getNextOre(existing, random);
        ResourceLocation blockToPlace  = null;

        if (vein.rareBlocks() != null && vein.rareBlockChance() > 0
                && random.nextDouble() < vein.rareBlockChance()) {
            ResourceLocation replacementRare = placerHelper.getNextRare(random);
            if (replacementRare != null
                    && !replacementRare.equals(AIR_BLOCK)) {
                blockToPlace  = replacementRare;
            }
        }
        if (blockToPlace == null) {
            blockToPlace = replacementOre;
        }

        Block block = BuiltInRegistries.BLOCK.get(blockToPlace);

        section.setBlockState(relX, relY, relZ, block.defaultBlockState(), false);
        return true;
    }

    // This is adapted code originally part of Geolosys, hosted at https://git.oitsjustjose.com/me/Geolosys
    public static boolean tryPlaceSamples(OreVeinConfig vein, WorldGenLevel level, ChunkPos chunk, RandomSource random) {
        boolean anySamples = false;
        for (int i = 0; i < vein.samples().number(); i++) {
            BlockState tmp = BuiltInRegistries.BLOCK.get(vein.placerHelper().getNextSample(random)).defaultBlockState();

            BlockPos samplePos = SampleUtils.getSamplePosition(level, chunk);

            if (samplePos == null || SampleUtils.inNonWaterFluid(level, samplePos)) continue;

            if (SampleUtils.isInWater(level, samplePos) && tmp.getProperties().contains(BlockStateProperties.WATERLOGGED)) {
                tmp = tmp.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (tmp.getProperties().contains(HorizontalDirectionalBlock.FACING)) {
                tmp = tmp.setValue(HorizontalDirectionalBlock.FACING, Direction.from2DDataValue(random.nextIntBetweenInclusive(0, 3)));
            }

            FeatureUtils.enqueueBlockPlacement(level, chunk, samplePos, tmp);
            FeatureUtils.fixSnowyBlock(level, samplePos);
        }
        return anySamples;
    }
}
