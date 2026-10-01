package dev.waldq.mipp;

import net.swedz.tesseract.config.annotation.ConfigComment;
import net.swedz.tesseract.config.annotation.ConfigKey;
import net.swedz.tesseract.config.annotation.Range;
import net.swedz.tesseract.config.annotation.SubSection;

public interface MIPPClientConfig {
    @ConfigKey
    @SubSection
    PipeNetworkAnalyzer pipeNetworkAnalyzer();

    interface PipeNetworkAnalyzer {
        @ConfigKey
        @ConfigComment({
                "Whether to use full block overlay for pipe network connections or not.",
                "MI standard outline will be used otherwise."
        })
        default boolean useFullBlockOverlay() { return true; }
    }
}
