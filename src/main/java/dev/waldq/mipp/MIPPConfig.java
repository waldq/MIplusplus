package dev.waldq.mipp;

import net.swedz.tesseract.config.annotation.ConfigComment;
import net.swedz.tesseract.config.annotation.ConfigKey;
import net.swedz.tesseract.config.annotation.Range;
import net.swedz.tesseract.config.annotation.SubSection;

public interface MIPPConfig {
    @ConfigKey
    @ConfigComment("The maximum number of chunks that can be loaded for each player on the server.")
    @Range.Integer(min = 0, max = 100)
    default int maxChunksPerPlayer() { return 25; }

    @ConfigKey
    @SubSection
    ProspectorParameters prospectorPerameters();

    @ConfigKey
    @SubSection
    OreGenParameters oreGenParameters();

    @ConfigKey
    @SubSection
    DebugJournal debugJournal();

    interface ProspectorParameters {
        @ConfigKey
        @ConfigComment("How much energy will ore prospector consume when starting a scan.")
        @Range.Integer(min = 0, max = 3200)
        default int prospectorEnergyCost() { return 32; }

        @ConfigKey
        @ConfigComment("Chunk radius that prospector will search in when doing a large scan.")
        @Range.Integer(min = 1, max = 1000)
        default int prospectorLargeScanR() { return 20; }

        @ConfigKey
        @ConfigComment("Chunk radius that prospector will search in when doing a local scan.")
        @Range.Integer(min = 1, max = 11)
        default int prospectorLocalScanR() { return 5; }

        @ConfigKey
        @ConfigComment("Number of chunk sections (16x16x16 blocks) allowed to be processed by a prospector every tick.")
        @Range.Integer(min = 1, max = 1000)
        default int chunksPerTick() { return 100; }
    }

    interface OreGenParameters {
        @ConfigKey
        @ConfigComment("Whether to enable vanilla ore veins or not.")
        default boolean enableVanillaOres() { return false; }

        @ConfigKey
        @ConfigComment("Whether to enable large ore vein generation from this mod or not.")
        default boolean enableCustomOreGeneration() { return true; }

        @ConfigKey
        @ConfigComment("Chance of a chunk being skipped during ore gen stage.")
        @Range.Integer(min = 1, max = 100)
        default int chunkSkipChance() { return 99; }
    }

    interface DebugJournal {
        @ConfigKey
        @ConfigComment("Max length of the debug journal in buckets (30 seconds).")
        @Range.Integer(min = 1, max = 480)
        default int historyLength() { return 60; }

        @ConfigKey
        @ConfigComment({
                "Max days the debug journal stores the players' bindings withouth joining the server.",
                "0 means never deleting such entries."
        })
        @Range.Integer(min = 0, max = Integer.MAX_VALUE)
        default int maxEntryLifeSpan() { return 30; }
    }
}
