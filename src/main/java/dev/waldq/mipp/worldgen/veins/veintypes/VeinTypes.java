package dev.waldq.mipp.worldgen.veins.veintypes;

import com.mojang.serialization.Codec;

import dev.waldq.mipp.worldgen.veins.VeinPlacerHelper;
import dev.waldq.mipp.worldgen.veins.OreVeinConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;

public enum VeinTypes implements StringRepresentable {
    DIKE("dike") {
        @Override
        public boolean generate(
                OreVeinConfig vein,
                VeinPlacerHelper placerHelper,
                WorldGenLevel level,
                BulkSectionAccess sectionAccess,
                BlockPos origin,
                int chunkX,
                int chunkZ,
                RandomSource random
        ) {
            return DikeVein.generate(vein, placerHelper, level, sectionAccess, origin, chunkX, chunkZ, random);
        }
    },

    SPIKY("spiky") {
        @Override
        public boolean generate(
                OreVeinConfig vein,
                VeinPlacerHelper placerHelper,
                WorldGenLevel level,
                BulkSectionAccess sectionAccess,
                BlockPos origin,
                int chunkX,
                int chunkZ,
                RandomSource random
        ) {
            return SpikyVein.generate(vein, placerHelper, level, sectionAccess, origin, chunkX, chunkZ, random);
        }
    },

    STOCK("stock") {
        @Override
        public boolean generate(
                OreVeinConfig vein,
                VeinPlacerHelper placerHelper,
                WorldGenLevel level,
                BulkSectionAccess sectionAccess,
                BlockPos origin,
                int chunkX,
                int chunkZ,
                RandomSource random
        ) {
            return StockVein.generate(vein, placerHelper, level, sectionAccess, origin, chunkX, chunkZ, random);
        }
    },

    TUBE("tube") {
        @Override
        public boolean generate(
                OreVeinConfig vein,
                VeinPlacerHelper placerHelper,
                WorldGenLevel level,
                BulkSectionAccess sectionAccess,
                BlockPos origin,
                int chunkX,
                int chunkZ,
                RandomSource random
        ) {
            return TubeVein.generate(vein, placerHelper, level, sectionAccess, origin, chunkX, chunkZ, random);
        }
    };

    public static final Codec<VeinTypes> CODEC = StringRepresentable.fromEnum(VeinTypes::values);

    private final String typeName;

    VeinTypes(String name) { this.typeName = name; }

    @Override
    public String getSerializedName() { return this.typeName; }

    public abstract boolean generate(
            OreVeinConfig vein,
            VeinPlacerHelper placerHelper,
            WorldGenLevel level,
            BulkSectionAccess sectionAccess,
            BlockPos origin,
            int chunkX,
            int chunkZ,
            RandomSource random
    );

}
