package dev.waldq.mipp.worldgen.features; // Ваш пакет

import com.mojang.serialization.Codec;

import dev.waldq.mipp.MIPPConfig;

import dev.waldq.mipp.worldgen.veins.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;


public class OreVeinFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SEARCH_RADIUS = 2;

    public OreVeinFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!MIPPConfig.ENABLE_ORE_GENERATION.getAsBoolean()) return false;

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        ResourceLocation dimension = level.getLevel().dimension().location();
        Holder<Biome> biome = level.getBiome(context.origin().atY(level.getLevel().getSeaLevel()).offset(8, 0, 8));

        List<OreVeinConfig> activeVeins = OreVeinConfigLoader.getVeinsForBiome(biome, dimension);
        if (activeVeins.isEmpty()) return false;

        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());

        boolean placedAnyVein = false;

        for (int cx = -SEARCH_RADIUS; cx <= SEARCH_RADIUS; cx++) {
            for (int cz = -SEARCH_RADIUS; cz <= SEARCH_RADIUS; cz++) {
                placedAnyVein = false;
                int adjX = chunkX + cx;
                int adjZ = chunkZ + cz;

                RandomSource random = VeinPlacerHelper.seededXoroshiroRandomSource(
                        level.getSeed(),
                        adjX,
                        adjZ,
                        dimension
                );

                // We check if a chunk has an ore vein the same way.
                OreVeinConfig vein = VeinGenHelpers.selectVein(activeVeins, random);
                if (vein == null) continue;

                int centerX = SectionPos.sectionToBlockCoord(adjX) + 8;
                int centerZ = SectionPos.sectionToBlockCoord(adjZ) + 8;
                int randomY = random.nextIntBetweenInclusive(vein.bounds().minY(), vein.bounds().maxY());

                BlockPos veinCenter = new BlockPos(centerX, randomY, centerZ);
                try (BulkSectionAccess sectionAccess = new BulkSectionAccess(level)) {
                    if (vein.data().type().generate(vein, vein.placerHelper(), level, sectionAccess, veinCenter, chunkX, chunkZ, random)) {
                        placedAnyVein = true;
                    }
                }

                // Will spawn samples only for the center chunk
                if (placedAnyVein && cx == 0 && cz == 0) {
                    VeinPlacer.tryPlaceSamples(vein, level, new ChunkPos(chunkX, chunkZ), random);
                }
            }
        }

        return placedAnyVein;
    }
}