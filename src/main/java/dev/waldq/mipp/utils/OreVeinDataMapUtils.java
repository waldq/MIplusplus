package dev.waldq.mipp.utils;

import dev.waldq.mipp.MIPPDataMaps;
import dev.waldq.mipp.datamaps.OreMapData;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class OreVeinDataMapUtils {
    private static final Map<BlockState, OreMapData> ORE_MAP_DATA_MAP = new HashMap<>();

    public static OreMapData getCachedOreMapData(BlockState state) {
        return ORE_MAP_DATA_MAP.computeIfAbsent(state, s -> {
            Optional<Holder.Reference<Block>> holder = BuiltInRegistries.BLOCK.getHolder(GeneralUtils.locationFromBlockState(s));
            if (holder.isPresent()) {
                OreMapData data = holder.get().getData(MIPPDataMaps.ORE_COLOR);
                if (data != null) {
                    return data;
                }
            }
            return new OreMapData(0xFFAAAAAA, null);
        });
    }

    public static int getCachedOreColor(BlockState state) {
        OreMapData data = getCachedOreMapData(state);
        if (data != null) {
            return 0xFF000000 | (data.hexColor() & 0xFFFFFF);
        }
        return 0xFFAAAAAA;
    }

    public static BlockState getMainBlock(BlockState state) {
        OreMapData data = getCachedOreMapData(state);
        if (data != null && data.mainBlock() != null && data.mainBlock().isPresent()) {
            return BuiltInRegistries.BLOCK.get(data.mainBlock().get()).defaultBlockState();
        }
        return state;
    }
}
