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

    @TextSubSection
    DebugJournal debugJournal();

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
        @LangKey(text = "Requires vein below machine: %s")
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

        @LangKey(text = "Dimension: %s. Coordinates: %s. Tier: %s. Chunks: %s.")
        MutableComponent info(
                @WithStyle("highlighted") Component dimension,
                @WithStyle("highlighted") Component coords,
                @WithStyle("highlighted") Component tier,
                @WithStyle("highlighted") Component amount
        );

        @LangKey(text = "%s doesn't have any loaders.")
        MutableComponent noLoaders(@WithStyle("highlighted") Component nickname);

        @LangKey(text = "Click to teleport")
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
        @LangKey(text = "No inventories are connected to this network.")
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
        @LangKey(text = "There are no ore veins nearby!")
        MutableComponent noVeins();

        @LangKey(text = "All resources")
        MutableComponent allRes();
    }

    interface DebugJournal {
        @TextSubSection
        FilterModes filters();

        @TextSubSection
        GroupModes groups();

        @TextSubSection
        SortModes sorts();

        @TextSubSection
        States states();

        @TextSubSection
        Expanded expanded();

        @TextSubSection
        Highlight highlights();

        @WithStyle("red")
        @LangKey(text = "Not enough energy. Requires %s to show data.")
        MutableComponent energyNotice(Component energy);

        @WithStyle("red")
        @LangKey(text = "No machines ticked in the world since the server restart.")
        MutableComponent noMachines();

        @LangKey(text = "Total work time: %ss")
        MutableComponent workTime(Component time);

        @LangKey(text = "Machines: %s")
        MutableComponent machinesAmount(Component amount);

        interface FilterModes {
            @LangKey(text = "None")
            MutableComponent none();

            @LangKey(text = "Any problem")
            MutableComponent anyProblem();

            @LangKey(text = "Working and low energy")
            MutableComponent workingAndLowEnergy();

            @LangKey(text = "No energy")
            MutableComponent noEnergy();
        }

        interface GroupModes {
            @LangKey(text = "None")
            MutableComponent none(); // yes, the third none, so what

            @LangKey(text = "Dimension")
            MutableComponent dimension();

            @LangKey(text = "State")
            MutableComponent state();

            @LangKey(text = "Block")
            MutableComponent block();
        }

        interface SortModes {
            @LangKey(text = "None")
            MutableComponent none();

            @LangKey(text = "Problem first")
            MutableComponent problemFirst();
        }

        interface States {
            @LangKey(text = "Working")
            MutableComponent working();

            @LangKey(text = "No data")
            MutableComponent noData();

            @LangKey(text = "Redstone")
            MutableComponent redstone();

            @LangKey(text = "Multiple recipes")
            MutableComponent multipleRecipes();

            @LangKey(text = "Invalid shape")
            MutableComponent invalidShape();

            @LangKey(text = "Low energy")
            MutableComponent lowEnergy();

            @LangKey(text = "No energy")
            MutableComponent noEnergy();

            @LangKey(text = "No recipe")
            MutableComponent noRecipe();

            @LangKey(text = "Output full")
            MutableComponent outputFull();

            @LangKey(text = "Condition(s) not met")
            MutableComponent condition();

            @LangKey(text = "Unknown")
            MutableComponent unknown();
        }

        interface Expanded {
            @LangKey(text = "XYZ: %s")
            MutableComponent blockPos(Component pos);

            @LangKey(text = "Dimension: %s")
            MutableComponent dimension(Component dimension);

            @LangKey(text = "Energy tier: %s")
            MutableComponent energyTier(Component tier);

            @LangKey(text = "Overdriving: %s")
            MutableComponent overdriving(Component isOverdriving);
        }

        interface Highlight {
            @LangKey(text = "Highlight all in the world")
            MutableComponent highlightAll();

            @LangKey(text = "Highlight in the world")
            MutableComponent highlight();
        }
    }
}
