package dev.waldq.mipp;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MIPPConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CHUNKS_PER_PLAYER = BUILDER
            .comment("Maximum number of chunks loaded for each player on the server.")
            .defineInRange("maxChunksPerPlayer", 25, 0, 100);

    public static final ModConfigSpec.IntValue PROSPECTOR_ENERGY_COST = BUILDER
            .comment("How much energy will ore prospector consume when starting a scan.")
            .defineInRange("prospectorEnergyCost", 32, 0, 3200);

    public static final ModConfigSpec.IntValue PROSPECTOR_LARGE_SCAN_RANGE = BUILDER
            .comment("Chunk chunkRadius that prospector will search in when doing a large scan.")
            .defineInRange("prospectorLargeScanR", 1000, 1, 1000);

    public static final ModConfigSpec.IntValue PROSPECTOR_LOCAL_SCAN_RANGE = BUILDER
            .comment("Chunk chunkRadius that prospector will search in when doing a local scan.")
            .defineInRange("prospectorLocalScanR", 5, 1, 11);

    public static final ModConfigSpec.IntValue CHUNKS_PER_TICK = BUILDER
            .comment("Number of chunk sections (16x16x16) allowed to be processed by a prospector every tick.")
            .defineInRange("chunksPerTick", 3, 1, 500);

    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_ORE_GENERATION = BUILDER
            .comment("Whether to enable vanilla ore veins or not")
            .define("enableVanillaOres", false);

    public static final ModConfigSpec.BooleanValue ENABLE_ORE_GENERATION = BUILDER
            .comment("Whether to enable large ore vein generation from this mod or not.")
            .define("enableCustomOreGeneration", true);

    public static final ModConfigSpec.IntValue CHUNK_SKIP_CHANCE = BUILDER
            .comment("Chance of a chunk being skipped during ore gen stage.")
            .defineInRange("chunkSkipChance", 95, 1, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();

}
