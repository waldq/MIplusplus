package dev.waldq.mipp;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.swedz.tesseract.neoforge.lang.annotation.LangKey;
import net.swedz.tesseract.neoforge.lang.annotation.LangKeyPattern;
import net.swedz.tesseract.neoforge.lang.annotation.Parsed;
import net.swedz.tesseract.neoforge.lang.annotation.WithStyle;

public interface MIPPText {
    @WithStyle("gray")
    @LangKey(text = "Generates %s")
    MutableComponent energyGenerationTooltip(@WithStyle("highlighted") Component energy);

    @WithStyle("gray")
    @LangKey(text = "Requires %s underneath")
    MutableComponent veinUnderneathTooltip(@WithStyle("highlighted") Component veinName);

    @WithStyle("red")
    @LangKey(text = "Not enough energy. Requires %s to scan.")
    MutableComponent prospectorEnergyNotice(Component energy);

    @LangKey(text = "Found %s approximately %s blocks away.")
    MutableComponent prospectorFoundVein(Component veinString, Component distance);

    @LangKey(text = "All resources")
    MutableComponent prospectorAllRes();

    @WithStyle("red")
    @LangKey(text = "Can't place another loader. You already have %s chunks loaded.")
    MutableComponent chunkLoaderTooMuch(@WithStyle("highlighted") Component amount);

    @LangKey(text = "Pipe Network Analyzer range: %s blocks")
    MutableComponent pipeNetworkAnalyzerRange(Component amount);

    @LangKey(text = "- Press %s on an item pipe block to highlight directly connected inventories.")
    @WithStyle("tooltip")
    MutableComponent pipeNetworkAnalyzerHelp1(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1
    );

    @LangKey(text = "- Press %s + %s on an item pipe to highlight all inventories connected to its network.")
    @WithStyle("tooltip")
    MutableComponent pipeNetworkAnalyzerHelp2(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "- Reset using %s + %s on air.")
    @WithStyle("tooltip")
    MutableComponent pipeNetworkAnalyzerHelp3(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "- Press %s to search for nearby ore veins. Click a chat message to set it as a target.")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp1(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1
    );

    @LangKey(text = "- Press %s + %s to open the ore vein map")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp2(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "- Press %s to clear selected target.")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp3(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1
    );
}
