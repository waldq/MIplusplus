package dev.waldq.mipp;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.swedz.tesseract.neoforge.lang.annotation.LangKey;
import net.swedz.tesseract.neoforge.lang.annotation.Parsed;
import net.swedz.tesseract.neoforge.lang.annotation.WithStyle;

public interface MIPPText {
    @LangKey(text = "Alt")
    MutableComponent keyAlt();

    @LangKey(text = "Mouse Scroll")
    MutableComponent keyMouseScroll();

    @WithStyle("gray")
    @LangKey(text = "Generates %s")
    MutableComponent energyGenerationTooltip(@WithStyle("highlighted") Component energy);

    @WithStyle("gray")
    @LangKey(text = "Requires %s underneath")
    MutableComponent veinUnderneathTooltip(@WithStyle("highlighted") Component veinName);

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

    @LangKey(text = "- Use %s + %s to change rendering range.")
    @WithStyle("tooltip")
    MutableComponent pipeNetworkAnalyzerHelp3(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "- Reset using %s + %s on air.")
    @WithStyle("tooltip")
    MutableComponent pipeNetworkAnalyzerHelp4(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "Viewing connected inventories.")
    MutableComponent pipeNetworkAnalyzerViewing();

    @LangKey(text = "Cleared selected inventories.")
    MutableComponent pipeNetworkAnalyzerReset();

    @WithStyle("red")
    @LangKey(text = "No inventories are connected to this node.")
    MutableComponent pipeNetworkAnalyzerEmptyNode();

    @WithStyle("red")
    @LangKey(text = "No inventories are connected to this network")
    MutableComponent pipeNetworkAnalyzerEmptyNetwork();

    @LangKey(text = "- Press %s to search for nearby ore veins. Click a chat message to set it as a target.")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp1(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1
    );

    @LangKey(text = "- Press %s + %s to open the ore vein map.")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp2(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1,
            @Parsed("keybind") @WithStyle("highlighted") String keybind2
    );

    @LangKey(text = "- Press %s again to clear selected target.")
    @WithStyle("tooltip")
    MutableComponent electricProspectorHelp3(
            @Parsed("keybind") @WithStyle("highlighted") String keybind1
    );

    @WithStyle("red")
    @LangKey(text = "Not enough energy. Requires %s to scan.")
    MutableComponent electricProspectorEnergyNotice(Component energy);

    @WithStyle("yellow")
    @LangKey(text = "=== Searching for nearby ore veins ===")
    MutableComponent electricProspectorSearchingLarge();

    @LangKey(text = "Searching for nearby ore veins.")
    MutableComponent electricProspectorSearchingLocal();

    @LangKey(text = "Found %s approximately %s blocks away.")
    MutableComponent electricProspectorFoundVein(@WithStyle("highlighted") Component veinString, @WithStyle("highlighted") Component distance);

    @WithStyle("red")
    @LangKey(text = "There are no ore vein nearby!")
    MutableComponent electricProspectorNoVeins();

    @LangKey(text = "All resources")
    MutableComponent electricProspectorAllRes();
}
