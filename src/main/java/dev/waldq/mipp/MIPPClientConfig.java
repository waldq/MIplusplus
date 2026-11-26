package dev.waldq.mipp;

import net.swedz.tesseract.config.annotation.*;

public interface MIPPClientConfig {
    @ConfigKey
    @SubSection
    PipeNetworkAnalyzer pipeNetworkAnalyzer();

    @ConfigKey
    @SubSection
    DebugJournal debugJournal();

    interface PipeNetworkAnalyzer {
        @ConfigKey
        @ConfigComment({
                "Whether to use full block overlay for pipe network connections or not.",
                "MI standard outline will be used otherwise."
        })
        default boolean useFullBlockOverlay() { return true; }
    }

    interface DebugJournal {
        @ConfigKey
        @ConfigComment("Maximum buckets (sections, 30 seconds each) shown in the debug journal screen.")
        @Range.Integer(min = 1, max = 480)
        default int maxBucketsShown() { return 60; }

        @ConfigKey
        @ConfigComment({
                "Whether to use full block overlay for machines or not.",
                "MI standard outline will be used otherwise."
        })
        default boolean useFullBlockOverlay() { return true; }
    }
}
