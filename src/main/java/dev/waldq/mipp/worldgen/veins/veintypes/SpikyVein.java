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

public class SpikyVein {
    private static boolean firstShapeCheck(float size, double Rx, double Ry, double Rz, double x, double y, double z, long seed) {
        //   used these formulas, if someone wants to check

        //   abs(x)^{0.5}+abs(y)^{0.5}+abs(z)^{0.5} <= 1.25
        //   abs((x-y)/1.414)^{0.5}+abs((x+y)/1.414)^{0.5}+abs(z)^{0.5} <= 1.25
        //   abs(x)^{0.5}+abs((y-z)/1.414)^{0.5}+abs((y+z)/1.414)^{0.5} <= 1.25
        //   abs((x-z)/1.414)^{0.5}+abs(y)^{0.5}+abs((x+z)/1.414)^{0.5} <= 1.25
        //   abs((x+y)/1.414)^{0.5}+abs((x-y)/2-z/1.414)^{0.5}+abs((x-y)/2+z/1.414)^{0.5} <= 1.25
        //   abs((x-y)/1.414)^{0.5}+abs((x+y)/2-z/1.414)^{0.5}+abs((x+y)/2+z/1.414)^{0.5} <= 1.25

        double effectiveRx = Rx * size;
        double effectiveRy = Ry * size;
        double effectiveRz = Rz * size;

        if (effectiveRx == 0 || effectiveRy == 0 || effectiveRz == 0) return false;

        double nx = x / effectiveRx;
        double ny = y / effectiveRy;
        double nz = z / effectiveRz;

        double angleX = getHash(seed, 101) * Math.PI * 2;
        double angleY = getHash(seed, 102) * Math.PI * 2;
        double angleZ = getHash(seed, 103) * Math.PI * 2;

        // rotation around X axis
        double radX_cos = Math.cos(angleX);
        double radX_sin = Math.sin(angleX);
        double y1 = ny * radX_cos - nz * radX_sin;
        double z1 = ny * radX_sin + nz * radX_cos;

        // rotation around Y axis
        double radY_cos = Math.cos(angleY);
        double radY_sin = Math.sin(angleY);
        double x2 = nx * radY_cos + z1 * radY_sin;
        double z2 = -nx * radY_sin + z1 * radY_cos;

        // rotation around Z axis
        double radZ_cos = Math.cos(angleZ);
        double radZ_sin = Math.sin(angleZ);
        double fX = x2 * radZ_cos - y1 * radZ_sin;
        double fY = x2 * radZ_sin + y1 * radZ_cos;

        double invSqrt2 = 0.70710678;
        double threshold = 1.25;

        if (isShape(fX, fY, z2, threshold)) return true;

        // rotation in XY plane
        double xy1 = (fX - fY) * invSqrt2;
        double xy2 = (fX + fY) * invSqrt2;
        if (isShape(xy1, xy2, z2, threshold)) return true;

        // rotation in YZ plane
        double yz1 = (fY - z2) * invSqrt2;
        double yz2 = (fY + z2) * invSqrt2;
        if (isShape(fX, yz1, yz2, threshold)) return true;

        // rotation in XZ plane
        double xz1 = (fX - z2) * invSqrt2;
        double xz2 = (fX + z2) * invSqrt2;
        if (isShape(xz1, fY, xz2, threshold)) return true;

        // diagonal spikes
        double d1 = (fX + fY + z2) / 1.73205;
        double d2 = (fX - fY + z2) / 1.73205;
        double d3 = (fX + fY - z2) / 1.73205;
        return isShape(d1, d2, d3, threshold);
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
        int veinRx = (int) Math.ceil(vein.bounds().radiusX() * veinSize);
        int veinRy = (int) Math.ceil(vein.bounds().radiusY() * veinSize);
        int veinRz = (int) Math.ceil(vein.bounds().radiusZ() * veinSize);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int placedCount = 0;

        long veinSeed = origin.asLong();

        for (int localX = -veinRx; localX <= veinRx; localX++) {
            int worldX = origin.getX() + localX;

            if (SectionPos.blockToSectionCoord(worldX) != targetChunkX) continue;

            for (int localZ = -veinRy; localZ <= veinRy; localZ++) {
                int worldZ = origin.getZ() + localZ;

                if (SectionPos.blockToSectionCoord(worldZ) != targetChunkZ) continue;

                for (int localY = -veinRz; localY <= veinRz; localY++) {
                    int worldY = origin.getY() + localY;

                    if (level.isOutsideBuildHeight(worldY)) continue;

                    if (firstShapeCheck(veinSize, vein.bounds().radiusX(), vein.bounds().radiusY(), vein.bounds().radiusZ(), localX, localY, localZ, veinSeed)) {
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

    private static boolean isShape(double x, double y, double z, double threshold) {
        return (Math.sqrt(Math.abs(x)) + Math.sqrt(Math.abs(y)) + Math.sqrt(Math.abs(z)) <= threshold);
    }

    private static double getHash(long seed, int salt) {
        long h = seed * 6364136223846793005L + salt * 1442695040888963407L;
        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        h = h ^ (h >>> 31);
        return (double) (h & 0xFFFFFFF) / 0xFFFFFFF;
    }
}
