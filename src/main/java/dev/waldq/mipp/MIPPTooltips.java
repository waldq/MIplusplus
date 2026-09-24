package dev.waldq.mipp;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.energy.EnergyApi;

import dev.technici4n.grandpower.api.ILongEnergyStorage;

import net.minecraft.core.registries.BuiltInRegistries;

import net.swedz.tesseract.neoforge.tooltip.TooltipAttachment;

import java.util.Optional;

import static aztech.modern_industrialization.MITooltips.DEFAULT_STYLE;
import static aztech.modern_industrialization.MITooltips.EU_MAXED_PARSER;

public class MIPPTooltips {

    public static final TooltipAttachment ENERGY_STORED_ITEM = TooltipAttachment.singleLineOptional(
            (stack, item) -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MIPP.ID),
            (flags, context, stack, item) -> {
                ILongEnergyStorage energyStorage = stack.getCapability(EnergyApi.ITEM);
                if (energyStorage != null) {
                    long capacity = energyStorage.getCapacity();
                    if (capacity > 0) {
                        return Optional.of(MIText.EnergyStored.text(EU_MAXED_PARSER.parse(new MITooltips.NumberWithMax(energyStorage.getAmount(), capacity))).withStyle(DEFAULT_STYLE));
                    }
                }
                return Optional.empty();
            }
    ).noShiftRequired();

    public static void init() {}
}
