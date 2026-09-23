package dev.waldq.mipp;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.swedz.tesseract.neoforge.lang.annotation.LangKey;
import net.swedz.tesseract.neoforge.lang.annotation.WithStyle;

public interface MIPPText {
    @WithStyle("gray")
    @LangKey(text = "Generates %s")
    MutableComponent energyGenerationTooltip(@WithStyle("highlighted") Component energy);

    @WithStyle("red")
    @LangKey(text = "Not enough energy. Needs %s to scan")
    MutableComponent prospectorEnergyNotice(Component energy);

    @WithStyle("green")
    @LangKey(text = "Found %s in approximately %s blocks away.")
    MutableComponent prospectorFoundVein(Component veinString, Component distance);

    @LangKey(text = "All resources")
    MutableComponent prospectorAllRes();

    @WithStyle("red")
    @LangKey(text = "Can't place another loader. You have %s chunks loaded.")
    MutableComponent chunkLoaderTooMuch(@WithStyle("highlighted") Component amount);
}
