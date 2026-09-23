package dev.waldq.mipp.worldgen.utils;

import dev.waldq.mipp.MIPPTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;

/*
 * This file is adapted code originally part of Geolosys, hosted at https://git.oitsjustjose.com/me/Geolosys
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
public class SampleUtils {
    @Nullable
    public static BlockPos getSamplePosition(WorldGenLevel level, ChunkPos chunkPos) {
        return getSamplePosition(level, chunkPos, -1);
    }

    @Nullable
    public static BlockPos getSamplePosition(WorldGenLevel level, ChunkPos chunkPos, int spread) {

        if (!(level instanceof WorldGenRegion region)) {
            return null;
        }

        var usedSpread = Math.max(6, spread);
        var xCenter = (chunkPos.getMinBlockX() + chunkPos.getMaxBlockX()) / 2;
        var zCenter = (chunkPos.getMinBlockZ() + chunkPos.getMaxBlockZ()) / 2;

        var blockPosX = xCenter
                + (level.getRandom().nextInt(usedSpread) * ((level.getRandom().nextBoolean()) ? 1 : -1));
        var blockPosZ = zCenter
                + (level.getRandom().nextInt(usedSpread) * ((level.getRandom().nextBoolean()) ? 1 : -1));

        if (!FeatureUtils.ensureCanWriteNoThrow(region, new BlockPos(blockPosX, 0, blockPosZ))) {
            return null;
        }

        if (!region.hasChunk(SectionPos.blockToSectionCoord(blockPosX), SectionPos.blockToSectionCoord(blockPosZ))) {
            return null;
        }

        var onGroundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPosX, blockPosZ);

        var searchPos = new BlockPos.MutableBlockPos(blockPosX, onGroundY + 2, blockPosZ);
        int minY = Math.max(region.getMinBuildHeight(), onGroundY - 16);

        while (searchPos.getY() > minY) {
            var blockToPlaceOn = region.getBlockState(searchPos);
            if (blockToPlaceOn.isFaceSturdy(region, searchPos, Direction.UP)) {
                if (blockToPlaceOn.is(MIPPTags.Blocks.SUPPORTS_SAMPLE)) {
                    var actualPlacePos = searchPos.above().immutable();

                    if (canReplace(region, actualPlacePos)) {
                        return actualPlacePos;
                    }
                }
            }
            searchPos.move(Direction.DOWN);
        }

        return null;
    }

    /**
     * @param level an WorldGenLevel instance
     * @param pos   A BlockPos to check in and around
     * @return true if the block at pos is replaceable
     */
    public static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.canBeReplaced() || state.isAir();
    }

    /**
     * @param level an WorldGenLevel instance
     * @param pos   A BlockPos to check in and around
     * @return true if the block is water (since we can waterlog)
     */
    public static boolean isInWater(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    /**
     * @param level an WorldGenLevel instance
     * @param pos   A BlockPos to check in and around
     * @return true if the block is in a non-water fluid
     */
    public static boolean inNonWaterFluid(WorldGenLevel level, BlockPos pos) {
        return (!level.getBlockState(pos).getFluidState().isEmpty()) && !isInWater(level, pos);
    }

    /**
     * @param posA
     * @param posB
     * @param range An integer representing how far is acceptable to be considered
     *              in range
     * @return true if within range
     */
    public static boolean isWithinRange(int posA, int posB, int range) {
        return (Math.abs(posA - posB) <= range);
    }
}
