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

public class DikeVein {

    private static boolean firstShapeCheck(
            double localX, double localY, double localZ,
            double thickness, double angle, double yaw,
            float phaseX, float phaseY, double rxSq
    ) {
        //   used these formulas, if someone wants to check

        //   X_{rot}\left(x,z\right)=x\cos\left(Y_{aw}\right)-z\sin\left(Y_{aw}\right)
        //   Z_{rot}\left(x,z\right)=x\sin\left(Y_{aw}\right)+z\cos\left(Y_{aw}\right)

        //   Z_{0}\left(x,y,z\right)=y\tan\left(A\right)+A_{1}\sin\left(w_{1}X_{rot}\left(x,z\right)+p_{1}\right)+A_{2}\cos\left(w_{2}X_{rot}\left(x,z\right)\right)+A_{3}\sin\left(w_{3}y+p_{2}\right)
        //   Z_{x}\left(x,z\right)=A_{1}w_{1}\cos\left(w_{1}X_{rot}\left(x,z\right)+p_{1}\right)-A_{2}w_{2}\sin\left(w_{2}X_{rot}\left(x,z\right)\right)

        //   T_{noise}\left(x,y,z\right)=1+B_{1}\sin\left(w_{4}X_{rot}\left(x,z\right)+w_{5}y\right)+B_{2}\cos\left(w_{6}X_{rot}\left(x,z\right)-w_{7}y\right)
        //   \left|Z_{rot}\left(x,z\right)-Z_{0}\left(x,y,z\right)\right|\cdot\sqrt{1+\left(Z_{x}\left(x,z\right)\right)^{2}}\le\frac{T}{2}\cdot T_{noise}\left(x,y,z\right)\ \left\{\left(X_{rot}\left(x,z\right)\right)^{2}\le R_{x}^{2}\right\}\ \left\{\left|y\right|\le R_{y}\right\}

        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double rotX = localX * cosYaw - localZ * sinYaw;
        double rotZ = localX * sinYaw + localZ * cosYaw;

        if (rotX * rotX > rxSq) return false;

        double z0 = localY * Math.tan(angle)
                + 3.5 * Math.sin(0.25 * rotX + phaseX)
                + 1.2 * Math.cos(0.6 * rotX)
                + 0.8 * Math.sin(0.15 * localY + phaseY);

        double dzdx = 3.5 * 0.25 * Math.cos(0.25 * rotX + phaseX) - 1.2 * 0.6 * Math.sin(0.6 * rotX);
        double factorXZ = Math.sqrt(1.0 + dzdx * dzdx);

        double tNoise = 1.0
                + 0.30 * Math.sin(0.4 * rotX + 0.2 * localY)
                + 0.20 * Math.cos(0.7 * rotX - 0.35 * localY);

        double effectiveThickness = (thickness / 2.0) * tNoise;
        double distToPlane = Math.abs(rotZ - z0) * factorXZ;

        return distToPlane <= effectiveThickness;
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

        double rx = vein.bounds().radiusX() * veinSize;
        int heightY = (int) Math.ceil(vein.bounds().radiusY() * veinSize);
        double thickness = Math.max(1.5, vein.bounds().radiusZ() * veinSize);

        double yaw = random.nextDouble() * Math.PI;
        double angle = (random.nextDouble() - 0.5) * 0.2;
        float phaseX = random.nextFloat() * 6.28318f;
        float phaseY = random.nextFloat() * 6.28318f;

        double rxSq = rx * rx;
        int maxOffset = (int) Math.ceil(rx + Math.abs(heightY * Math.tan(angle)) + thickness + 6.0);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int placedCount = 0;

        for (int localX = -maxOffset; localX <= maxOffset; localX++) {
            int worldX = origin.getX() + localX;

            if (SectionPos.blockToSectionCoord(worldX) != targetChunkX) continue;

            for (int localZ = -maxOffset; localZ <= maxOffset; localZ++) {
                int worldZ = origin.getZ() + localZ;

                if (SectionPos.blockToSectionCoord(worldZ) != targetChunkZ) continue;

                for (int localY = -heightY; localY <= heightY; localY++) {
                    int worldY = origin.getY() + localY;

                    if (level.isOutsideBuildHeight(worldY)) continue;

                    if (firstShapeCheck(
                            localX, localY, localZ,
                            thickness, angle, yaw,
                            phaseX, phaseY, rxSq
                    )) {
                        mutablePos.set(worldX, worldY, worldZ);

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
        return placedCount > 0;
    }
}