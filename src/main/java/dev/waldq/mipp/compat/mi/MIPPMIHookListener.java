package dev.waldq.mipp.compat.mi;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPTooltips;
import dev.waldq.mipp.blocks.machine.MIPPMachines;
import dev.waldq.mipp.blocks.machine.processcondition.EnergyGenerationCondition;

import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEntrypoint;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookListener;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.*;

@MIHookEntrypoint
public class MIPPMIHookListener implements MIHookListener {
    @Override
    public void machineProcessConditions(MachineProcessConditionsMIHookContext hook) {
        hook.register(MIPP.id("energy_generation"), EnergyGenerationCondition.CODEC, EnergyGenerationCondition.STREAM_CODEC);
    }

    @Override
    public void machineRecipeTypes(MachineRecipeTypesMIHookContext hook) { MIPPMachines.recipeTypes(hook); }

//    @Override
//    public void singleBlockCraftingMachines(SingleBlockCraftingMachinesMIHookContext hook) {
//        MIPPMachines.singleBlockCrafting(hook);
//    }

    @Override
    public void singleBlockSpecialMachines(SingleBlockSpecialMachinesMIHookContext hook) {
        MIPPMachines.singleBlockSpecial(hook);
    }

    @Override
    public void multiblockMachines(MultiblockMachinesMIHookContext hook) {
        MIPPMachines.multiblockMachines(hook);
    }

    @Override
    public void tooltips() { MIPPTooltips.init(); }
}
