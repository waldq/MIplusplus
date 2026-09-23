package dev.waldq.mipp.item.pospector.utils;

import dev.waldq.mipp.worldgen.veins.OreVeinConfig;
import dev.waldq.mipp.worldgen.veins.OreVeinConfigLoader;
import dev.waldq.mipp.worldgen.veins.VeinGenHelpers;
import dev.waldq.mipp.worldgen.veins.VeinPlacerHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerLarge {
    @FunctionalInterface
    private interface ChunkChecker {
        boolean check(int chunkX, int ChunkZ);
    }

    // Spiral scanning, so that we start from the player and go outwards
    private static void scanInSpiral(ChunkPos centerChunk, int chunkRadius, ChunkChecker checker) {
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;

        int maxSteps = (2 * chunkRadius + 1) * (2 * chunkRadius + 1);

        for (int i = 0; i < maxSteps; i++) {
            if (-chunkRadius <= x && x <= chunkRadius && -chunkRadius <= z && z <= chunkRadius) {
                boolean shouldContinue = checker.check(centerChunk.x + x, centerChunk.z + z);
                if (!shouldContinue) {
                    return;
                }
            }

            if (x == z || (x < 0 && x == -z) || (x > 0 && x == -z + 1)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }

            x += dx;
            z += dz;
        }
    }

    // There's probably a better to do the scanning
    /**
     * @param level        ServerLevel to get biomes from
     * @param seed         level's seed, used to create new RandomSource object for each chunk
     * @param dim          level's dimension, used to create new RandomSource object for each chunk
     * @param centerChunk  ChunkPos from where we start the whole scanning
     * @param chunkRadius  chunkRadius to scan in
     * @param activeVeins  List of ore types for the current dimension
     * @return Map<OreVeinConfig, ChunkPos> of vein types and chunk positions
     */
    public static Map<OreVeinConfig, ChunkPos> scanArea(
            ServerLevel level,
            long seed,
            ResourceLocation dim,
            ChunkPos centerChunk,
            int chunkRadius,
            List<OreVeinConfig> activeVeins
    ) {
        Map<OreVeinConfig, ChunkPos> foundVeins = new HashMap<>();

        int seaLevel = level.getSeaLevel();

        scanInSpiral( centerChunk, chunkRadius, ( chunkX, chunkZ) -> {
            int blockX = (chunkX << 4) + 8;
            int blockZ = (chunkZ << 4) + 8;

            // We have to create a new random for each chunk since biomes can be different, and thus set of veins too
            RandomSource random = VeinPlacerHelper.seededXoroshiroRandomSource(
                    seed,
                    chunkX,
                    chunkZ,
                    dim
            );

            BlockPos center = new BlockPos(blockX, seaLevel, blockZ);

            Holder<Biome> biome = level.getBiome(center);

            List<OreVeinConfig> filteredVeins = OreVeinConfigLoader.filterByBiome(activeVeins, biome);

            OreVeinConfig vein = VeinGenHelpers.selectVein(filteredVeins, random);
            if (vein != null) {
                foundVeins.putIfAbsent(vein, new ChunkPos(chunkX, chunkZ));
            }

            // We stop either if we find all vein types loaded for the current dimension, or if we check all chunks
            boolean foundAllTypes = foundVeins.size() == activeVeins.size();
            return !foundAllTypes;
        });
        return foundVeins;
    }
}
