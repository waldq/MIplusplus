package dev.waldq.mipp.item.debugjournal.utils.session;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.debugjournal.JournalEntry;
import dev.waldq.mipp.item.debugjournal.MachineState;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Predicate;

public enum FilterMode implements ITextOwner {
    NONE(ref -> true, MIPP.text().debugJournal().filters().none()),
    ANY_PROBLEM(ref -> {
        JournalEntry entry = ref.entry();
        if (entry == null) return false;
        MachineState state = entry.getMainHistoryState();
        return !(state.equals(MachineState.WORKING) || state.equals(MachineState.LOW_ENERGY));
    }, MIPP.text().debugJournal().filters().anyProblem()),
    WORKING_AND_LOW_ENERGY(ref -> {
        JournalEntry entry = ref.entry();
        if (entry == null) return false;
        MachineState state = entry.getMainHistoryState();
        return state.equals(MachineState.WORKING) || state.equals(MachineState.LOW_ENERGY);
    }, MIPP.text().debugJournal().filters().workingAndLowEnergy()),
    NO_ENERGY(ref ->
            ref.entry() != null && ref.entry().getMainHistoryState() == MachineState.NO_ENERGY,
            MIPP.text().debugJournal().filters().noEnergy());

    public static final FilterMode[] values = FilterMode.values();

    private final Predicate<JournalSession.JournalRef> predicate;
    private final MutableComponent text;

    public Predicate<JournalSession.JournalRef> getPredicate() {
        return predicate;
    }

    public MutableComponent getText() {
        return text;
    }

    FilterMode(Predicate<JournalSession.JournalRef> predicate, MutableComponent text) {
        this.predicate = predicate;
        this.text = text;
    }
}