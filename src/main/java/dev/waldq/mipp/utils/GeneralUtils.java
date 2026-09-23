package dev.waldq.mipp.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class GeneralUtils {
    public static String stringFromBlockState(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    public static ResourceLocation locationFromBlockState(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }
}
