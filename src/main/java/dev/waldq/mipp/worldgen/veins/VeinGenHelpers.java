package dev.waldq.mipp.worldgen.veins;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPConfig;
import net.minecraft.util.RandomSource;

import java.util.List;


public class VeinGenHelpers {

    public static OreVeinConfig selectVein(List<OreVeinConfig> activeVeins, RandomSource random) {
        if (activeVeins.isEmpty()) return null;

        int skipChance = MIPP.config().oreGenParameters().chunkSkipChance();
        double roll = random.nextIntBetweenInclusive(1, 100);
        if (roll <= skipChance) return null;

        int totalVein = 0;
        for (OreVeinConfig vein : activeVeins) {
            totalVein += vein.data().weight();
        }

        if (totalVein <= 0) return null;

        int veinRoll = random.nextInt(totalVein);

        for (OreVeinConfig vein : activeVeins) {
            if (veinRoll < vein.data().weight()) {
                return vein;
            }
            veinRoll -= vein.data().weight();
        }

        return null;
    }
}
