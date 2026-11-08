package dev.waldq.mipp;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.swedz.tesseract.neoforge.lang.annotation.LangKey;
import net.swedz.tesseract.neoforge.lang.annotation.Parsed;
import net.swedz.tesseract.neoforge.lang.annotation.TextSubSection;
import net.swedz.tesseract.neoforge.lang.annotation.WithStyle;

public interface MIPPText {
    @TextSubSection
    Binds binds();

    @TextSubSection
    ProcessConditions processConditions();

    @TextSubSection
    ChunkLoader chunkLoader();

    @TextSubSection
    NetworkAnalyzer networkAnalyzer();

    @TextSubSection
    Prospector prospector();

    interface Binds {
        @LangKey(text = "Alt")
        MutableComponent keyAlt();

        @LangKey(text = "Mouse Scroll")
        MutableComponent keyMouseScroll();
    }

    interface ProcessConditions {
        @LangKey(text = "Generates %s")
        MutableComponent energyGenerationTooltip(@WithStyle("highlighted") Component energy);

        @WithStyle("gray")
        @LangKey(text = "Requires %s underneath")
        MutableComponent veinUnderneathTooltip(@WithStyle("highlighted") Component veinName);
    }

    interface ChunkLoader {
        @WithStyle("red")
        @LangKey(text = "Can't place another loader. You already have %s chunks loaded.")
        MutableComponent tooMuch(@WithStyle("highlighted") Component amount);

        @WithStyle("yellow")
        @LangKey(text = "Found chunk loaders:")
        MutableComponent found();

        @WithStyle("green")
        @LangKey(text = "----------- %s -----------")
        MutableComponent line(@WithStyle("highlighted") Component nickname);

        @LangKey(text = "Dimension: %s. Coordinates: %s. Tier: %s. Amount: %s.")
        MutableComponent info(
                @WithStyle("highlighted") Component dimension,
                @WithStyle("highlighted") Component coords,
                @WithStyle("highlighted") Component tier,
                @WithStyle("highlighted") Component amount
        );

        @LangKey(text = "%s doesn't have any loaders.")
        MutableComponent noLoaders(@WithStyle("highlighted") Component nickname);

        @LangKey(text = "Click to tp.")
        MutableComponent clickToTp();

        @LangKey(text = "There are no chunk loaders in the world.")
        MutableComponent none();
    }

    interface NetworkAnalyzer {
        @LangKey(text = "Pipe Network Analyzer range: %s blocks")
        MutableComponent range(Component amount);

        @LangKey(text = "- Press %s on an item pipe block to highlight directly connected inventories.")
        @WithStyle("tooltip")
        MutableComponent help1(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1
        );

        @LangKey(text = "- Press %s + %s on an item pipe to highlight all inventories connected to its network.")
        @WithStyle("tooltip")
        MutableComponent help2(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1,
                @Parsed("keybind") @WithStyle("highlighted") String keybind2
        );

        @LangKey(text = "- Use %s + %s to change rendering range.")
        @WithStyle("tooltip")
        MutableComponent help3(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1,
                @Parsed("keybind") @WithStyle("highlighted") String keybind2
        );

        @LangKey(text = "- Reset using %s + %s on air.")
        @WithStyle("tooltip")
        MutableComponent help4(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1,
                @Parsed("keybind") @WithStyle("highlighted") String keybind2
        );

        @LangKey(text = "Viewing connected inventories.")
        MutableComponent viewing();

        @LangKey(text = "Cleared selected inventories.")
        MutableComponent reset();

        @WithStyle("red")
        @LangKey(text = "No inventories are connected to this node.")
        MutableComponent emptyNode();

        @WithStyle("red")
        @LangKey(text = "No inventories are connected to this network")
        MutableComponent emptyNetwork();
    }

    interface Prospector {
        @LangKey(text = "- Press %s to search for nearby ore veins. Click a chat message to set it as a target.")
        @WithStyle("tooltip")
        MutableComponent help1(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1
        );

        @LangKey(text = "- Press %s + %s to open the ore vein map.")
        @WithStyle("tooltip")
        MutableComponent help2(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1,
                @Parsed("keybind") @WithStyle("highlighted") String keybind2
        );

        @LangKey(text = "- Press %s again to clear selected target.")
        @WithStyle("tooltip")
        MutableComponent help3(
                @Parsed("keybind") @WithStyle("highlighted") String keybind1
        );

        @WithStyle("red")
        @LangKey(text = "Not enough energy. Requires %s to scan.")
        MutableComponent energyNotice(Component energy);

        @WithStyle("yellow")
        @LangKey(text = "=== Searching for nearby ore veins ===")
        MutableComponent searchingLarge();

        @LangKey(text = "Searching for nearby ore veins.")
        MutableComponent searchingLocal();

        @LangKey(text = "Found %s approximately %s blocks away.")
        MutableComponent foundVein(@WithStyle("highlighted") Component veinString, @WithStyle("highlighted") Component distance);

        @WithStyle("red")
        @LangKey(text = "There are no ore vein nearby!")
        MutableComponent noVeins();

        @LangKey(text = "All resources")
        MutableComponent allRes();
    }
}
