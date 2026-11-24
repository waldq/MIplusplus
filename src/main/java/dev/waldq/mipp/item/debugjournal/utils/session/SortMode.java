package dev.waldq.mipp.item.debugjournal.utils.session;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.debugjournal.JournalEntry;
import net.minecraft.network.chat.MutableComponent;

import java.util.Comparator;

public enum SortMode {
    NO_SORT((ref1, ref2) -> 0, MIPP.text().debugJournal().sorts().none()),
    PROBLEM_FIRST((ref1, ref2) -> {
        JournalEntry en1 = ref1.entry();
        JournalEntry en2 = ref2.entry();

        if (en1 == null && en2 == null) return 0;
        if (en1 == null) return 1;
        if (en2 == null) return -1;

        return Boolean.compare(
                en2.getMainHistoryState().isProblem(),
                en1.getMainHistoryState().isProblem()
        );
    }, MIPP.text().debugJournal().sorts().problemFirst());

    public static final SortMode[] values = SortMode.values();

    private final Comparator<JournalSession.JournalRef> comparator;
    private final MutableComponent text;

    public Comparator<JournalSession.JournalRef> getComparator() {
        return comparator;
    }

    public Comparator<JournalSession.JournalRef> getComparatorReversed() {
        return comparator.reversed();
    }

    public MutableComponent getText() {
        return text;
    }

    SortMode(Comparator<JournalSession.JournalRef> comparator, MutableComponent text) {
        this.comparator = comparator;
        this.text = text;
    }
}
