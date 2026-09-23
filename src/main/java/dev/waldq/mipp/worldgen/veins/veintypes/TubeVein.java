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

public class TubeVein {
    private static boolean isInsideTube(
            double localX, double localY, double localZ,
            double radius, double phaseX, double phaseZ
    ) {
        //   used these formulas, if someone wants to check

        //   x_{0}(y)=A_{1}*\sin(w_{1}*y+p_{1})+A_{2}*\cos(w_{2}*y)+k_{x}*y
        //   z_{0}(y)=B_{1}*\cos(w_{1}*y+p_{2})+B_{2}*\sin(w_{3}*y)+k_{z}*y
        //   x'_{0}(y)=A_{1}*w_{1}*\cos(w_{1}*y+p_{1})-A_{2}*w_{2}*\sin(w_{2}*y)+k_{x}
        //   z'_{0}(y)=-B_{1}*w_{1}*\sin(w_{1}*y+p_{2})+B_{2}*w_{3}*\cos(w_{3}*y)+k_{z}

        //   ((x-x_{0}(y))\sqrt{1+(x'_{0}(y))^{2}})^{2}+((z-z_{0}(y))\sqrt{1+(z'_{0}(y))^{2}})^{2}\le R^{2}

        //   k_{x}=0.08, k_{z}=-0.05
        //   A_{1}=0.25, B_{1}=1.3, A_{2}=1.8, B_{2}=-0.01
        //   w_{1}=0.4, w_{2}=0.2, w_{3}=0.15
        //   p_{1}=2.5, p_{2}=1.2, R=1

        double x0 = 0.25 * Math.sin(0.4 * localY + phaseX) + 1.8 * Math.cos(0.2 * localY) + 0.08 * localY;
        double z0 = 1.3 * Math.cos(0.4 * localY + phaseZ) - 0.01 * Math.sin(0.15 * localY) - 0.05 * localY;

        double dxdy = 0.25 * 0.4 * Math.cos(0.4 * localY + phaseX) - 1.8 * 0.2 * Math.sin(0.2 * localY) + 0.08;
        double dzdy = -1.3 * 0.4 * Math.sin(0.4 * localY + phaseZ) - 0.01 * 0.15 * Math.cos(0.15 * localY) - 0.05;

        double factorX = Math.sqrt(1.0 + dxdy * dxdy);
        double factorZ = Math.sqrt(1.0 + dzdy * dzdy);

        double normX = (localX - x0) * factorX;
        double normZ = (localZ - z0) * factorZ;

        double distSq = normX * normX + normZ * normZ;


        if (distSq > radius * radius * 1.5) return false;

        double angle = Math.atan2(normZ, normX);
        double roughness = 1.0
                + 0.10 * Math.sin(angle * 4.0 + localY * 0.2)
                + 0.06 * Math.cos(angle * 7.0 - localY * 0.3);

        double effectiveRadius = radius * roughness;

        return distSq <= (effectiveRadius * effectiveRadius);
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

        double radius = vein.bounds().radiusX() * veinSize;
        int veinRy = (int) Math.ceil(vein.bounds().radiusY() * veinSize);

        double phaseX = random.nextDouble() * Math.PI * 2.0;
        double phaseZ = random.nextDouble() * Math.PI * 2.0;

        int maxOffsetXZ = (int) Math.ceil(radius + 4.0 + (0.08 * veinRy));

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int placedCount = 0;

        for (int localX = -maxOffsetXZ; localX <= maxOffsetXZ; localX++) {
            int worldX = origin.getX() + localX;

            if (SectionPos.blockToSectionCoord(worldX) != targetChunkX) continue;

            for (int localZ = -maxOffsetXZ; localZ <= maxOffsetXZ; localZ++) {
                int worldZ = origin.getZ() + localZ;

                if (SectionPos.blockToSectionCoord(worldZ) != targetChunkZ) continue;

                for (int localY = -veinRy; localY <= veinRy; localY++) {
                    int worldY = origin.getY() + localY;

                    if (level.isOutsideBuildHeight(worldY)) continue;

                    if (isInsideTube(localX, localY, localZ, radius, phaseX, phaseZ)) {
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