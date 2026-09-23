package dev.waldq.mipp.worldgen.veins.veintypes;

import dev.waldq.mipp.worldgen.veins.VeinPlacer;
import dev.waldq.mipp.worldgen.veins.VeinPlacerHelper;
import dev.waldq.mipp.worldgen.veins.OreVeinConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class StockVein {

    private static boolean firstShapeCheck(
            float size, double Rx, double Ry, double Rz,
            double localX, double localY, double localZ,
            float phaseX, float phaseY, float phaseZ, float scaleMod
    ) {

        //   used these formulas, if someone wants to check

        //   n_{localX}=\frac{localX}{R_{localX}s}, n_{localY}=\frac{localY}{R_{localY}s}, n_{localZ}=\frac{localZ}{R_{localZ}s}
        //   d\ =\ n_{localX}^{2}\ +\ n_{localY}^{2}+n_{localZ}^{2}
        //   l=\sqrt{d}

        //   d_{localX}=\frac{n_{localX}}{l}
        //   d_{localY}=\frac{n_{localY}}{l}
        //   d_{localZ}=\frac{n_{localZ}}{l}

        //   t=1.0 + 0.14\sin\left(4.5d_{x}c+3d_{y}+p_{x}\right)
        //         + 0.1\cos\left(5d_{y}c-4d_{z}+p_{y}\right)
        //         + 0.08\sin\left(8d_{z}c+6d_{x}+p_{z}\right)
        //         + 0.05\cos\left(11d_{x}+9d_{z}-p_{x}\right)

        //   n_{localX}^{2}\ +\ n_{localY}^{2}+n_{localZ}^{2}\le t^{2}

        double effectiveRx = Rx * size;
        double effectiveRy = Ry * size;
        double effectiveRz = Rz * size;

        double nx = localX / effectiveRx;
        double ny = localY / effectiveRy;
        double nz = localZ / effectiveRz;

        double distanceSq = nx * nx + ny * ny + nz * nz;

        if (distanceSq > 2.2) return false;

        double length = Math.sqrt(distanceSq);
        if (length == 0) return true;

        double dirX = nx / length;
        double dirY = ny / length;
        double dirZ = nz / length;

        double roughness = 1.0
                + 0.14 * Math.sin(dirX * 4.5 * scaleMod + dirY * 3.0 + phaseX)
                + 0.10 * Math.cos(dirY * 5.0 * scaleMod - dirZ * 4.0 + phaseY)
                + 0.08 * Math.sin(dirZ * 8.0 * scaleMod + dirX * 6.0 + phaseZ)
                + 0.05 * Math.cos(dirX * 11.0 + dirZ * 9.0 - phaseX);

        return distanceSq <= (roughness * roughness);
    }



    public static boolean generate(
            OreVeinConfig vein,
            VeinPlacerHelper placerHelper,
            WorldGenLevel level,
            BulkSectionAccess sectionAccess,
            BlockPos origin,
            int targetChunkX,
            int targetChunkZ,
            RandomSource random
    ) {
        float veinSize = vein.bounds().size();

        float phaseX = random.nextFloat() * 6.28318f;
        float phaseY = random.nextFloat() * 6.28318f;
        float phaseZ = random.nextFloat() * 6.28318f;
        float scaleMod = 0.7f + random.nextFloat() * 0.6f;

        int veinRx = (int) Math.ceil(vein.bounds().radiusX() * veinSize * 1.35);
        int veinRy = (int) Math.ceil(vein.bounds().radiusY() * veinSize * 1.35);
        int veinRz = (int) Math.ceil(vein.bounds().radiusZ() * veinSize * 1.35);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int placedCount = 0;

        for (int localX = -veinRx; localX <= veinRx; localX++) {
            int worldX = origin.getX() + localX;

            if (SectionPos.blockToSectionCoord(worldX) != targetChunkX) continue;

            for (int localZ = -veinRz; localZ <= veinRz; localZ++) {
                int worldZ = origin.getZ() + localZ;

                if (SectionPos.blockToSectionCoord(worldZ) != targetChunkZ) continue;

                for (int localY = -veinRy; localY <= veinRy; localY++) {
                    int worldY = origin.getY() + localY;

                    if (level.isOutsideBuildHeight(worldY)) continue;

                    if (firstShapeCheck(
                            veinSize, vein.bounds().radiusX(), vein.bounds().radiusY(), vein.bounds().radiusZ(),
                            localX, localY, localZ,
                            phaseX, phaseY, phaseZ, scaleMod
                    )) {
                        mutablePos.set(worldX, worldY, worldZ);

                        if (level.ensureCanWrite(mutablePos)) {
                            LevelChunkSection section = sectionAccess.getSection(mutablePos);

                            if (section != null) {
                                int relX = SectionPos.sectionRelative(worldX);
                                int relY = SectionPos.sectionRelative(worldY);
                                int relZ = SectionPos.sectionRelative(worldZ);

                                BlockState existing = section.getBlockState(relX, relY, relZ);

                                if (VeinPlacer.tryPlaceVein(vein, placerHelper, existing, section, relX, relY, relZ, worldY, random)) {
                                    placedCount++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return placedCount > 0;
    }
}