package dev.waldq.mipp.item.debugjournal;

import dev.waldq.mipp.MIPP;
import net.minecraft.network.chat.MutableComponent;

public enum MachineState {
    WORKING(MIPP.text().debugJournal().states().working(), 0xFF1D9E75, false),
    LOW_ENERGY(MIPP.text().debugJournal().states().lowEnergy(), 0xFF97C459, true),
    REDSTONE(MIPP.text().debugJournal().states().redstone(), 0xFF7F77DD, true),
    MULTIPLE_RECIPES(MIPP.text().debugJournal().states().multipleRecipes(), 0xFFD4537E, true),
    NO_ENERGY(MIPP.text().debugJournal().states().noEnergy(), 0xFFE24B4A, true), // or steam
    OUTPUT_FULL(MIPP.text().debugJournal().states().outputFull(), 0xFFD85A30, true),
    CONDITION(MIPP.text().debugJournal().states().condition(), 0xFFEF9F27, true),
    MULTI_INVALID_SHAPE(MIPP.text().debugJournal().states().invalidShape(), 0xFF378ADD, true),
    NO_DATA(MIPP.text().debugJournal().states().noData(), 0x33FFFFFF, false), // machine didn't tick yet
    NO_RECIPE(MIPP.text().debugJournal().states().noRecipe(), 0xFF888780, true),
    UNKNOWN(MIPP.text().debugJournal().states().unknown(), 0xFF444441, true);

    public static final MachineState[] values = MachineState.values();

    private final MutableComponent text;
    private final int color;
    private final boolean problem;

    MachineState(MutableComponent text, int color, boolean problem) {
        this.text = text;
        this.color = color;
        this.problem = problem;
    }

    public MutableComponent text() {
        return text;
    }

    public int color() {
        return color;
    }

    public boolean isProblem() {
        return problem;
    }
}
