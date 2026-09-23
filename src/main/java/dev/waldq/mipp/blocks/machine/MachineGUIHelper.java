package dev.waldq.mipp.blocks.machine;

import net.swedz.tesseract.neoforge.compat.mi.machine.builder.MachineGuiConfiguration;

public class MachineGUIHelper {
    public static void getTurbineGUI(MachineGuiConfiguration gui) {
        gui.slots(slots -> {
            slots.fluidInput(26, 37);
            slots.fluidOutput(88, 28);
            slots.fluidOutput(106, 28);
            slots.fluidOutput(88, 46);
            slots.fluidOutput(106, 46);
        });
        gui.progressBar(56, 35, "yai_charge");
    }
}
