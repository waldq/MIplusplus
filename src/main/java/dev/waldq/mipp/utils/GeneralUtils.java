package dev.waldq.mipp.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class GeneralUtils {
    public static ResourceLocation locationFromBlockState(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    public static String formatBlockPos(BlockPos pos) {
        return "%d, %d, %d".formatted(pos.getX(), pos.getY(), pos.getZ());
    }

    private GeneralUtils() {}
}
