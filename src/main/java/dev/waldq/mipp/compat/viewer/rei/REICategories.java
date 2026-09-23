package dev.waldq.mipp.compat.viewer.rei;

import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.compat.rei.machines.SteamMode;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.blocks.machine.MIPPMachines;

public class REICategories {
    public static void registerTurbineREICategory() {
        ReiMachineRecipes.registerCategory(MIPP.id("turbine"), new MachineCategoryParams(
                "Turbine", MIPP.id("turbine"),
                new SlotPositions.Builder().build(),
                new SlotPositions.Builder().build(),
                new SlotPositions.Builder().addSlots(26, 37, 1, 1).build(),
                new SlotPositions.Builder().addSlots(88, 28, 2, 2).build(),
                new ProgressBar.Params(56, 35, "yai_charge"),
                MIPPMachines.RecipeTypes.TURBINE,
                recipe -> true,
                false,
                SteamMode.NEITHER
        ));
    }
}
