package dev.waldq.mipp.worldgen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;

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
public class FeatureUtils {
    public static boolean ensureCanWriteNoThrow(WorldGenLevel level, BlockPos pos) {
        if (level instanceof WorldGenRegion region) {
            ChunkPos center = region.getCenter();
            int i = SectionPos.blockToSectionCoord(pos.getX());
            int j = SectionPos.blockToSectionCoord(pos.getZ());
            int k = Math.abs(center.x - i);
            int l = Math.abs(center.z - j);
            // writeRadiusCutoff is not accessible, so use a constant 1 for 3x3 generation.
            return k <= 1 && l <= 1;
        } else {
            // All feature levels *should* be WorldGenRegions (this has not thrown yet)
            return false;
        }
    }

    public static boolean enqueueBlockPlacement(WorldGenLevel level, ChunkPos chunk, BlockPos pos, BlockState state) {
        if (!level.hasChunk(chunk.x, chunk.z)) return false;
        if (!ensureCanWriteNoThrow(level, pos)) return false;

        ChunkAccess chunkaccess = level.getChunk(pos);
        BlockState blockstate = chunkaccess.setBlockState(pos, state, false);
        //            level.getLevel().onBlockStateChange(pos, blockstate, state);
        return blockstate != null;
    }

    public static void fixSnowyBlock(WorldGenLevel level, BlockPos posPlaced) {
        BlockState below = level.getBlockState(posPlaced.below());
        if (below.hasProperty(BlockStateProperties.SNOWY)) {
            level.setBlock(posPlaced.below(), below.setValue(BlockStateProperties.SNOWY, Boolean.valueOf(false)),
                    2 | 16);
        }
    }
}
