package dev.waldq.mipp.item.debugjournal;

import dev.waldq.mipp.MIPP;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class JournalEntry {
    private static final int BUCKET_LENGTH = 600;
    private static final int HISTORY_LENGTH = MIPP.config().debugJournal().historyLength();

    @Nullable
    private final UUID owner;
    private final Block machine;

    private byte state;
    private final short[] counts = new short[MachineState.values.length];
    private byte curMainStateIndex;
    private final byte[] history = new byte[HISTORY_LENGTH];
    private byte mainHistoryState = (byte) MachineState.NO_DATA.ordinal();
    private final short[] workTicks = new short[HISTORY_LENGTH]; // TODO either use or remove it entirely
    private int head;
    private long bucketEnd;
    private boolean isValid = true;

    public JournalEntry(@Nullable UUID owner, Block machine, long currentTick) {
        this.owner = owner;
        this.machine = machine;
        this.bucketEnd = currentTick + BUCKET_LENGTH;
        Arrays.fill(history, (byte) MachineState.NO_DATA.ordinal());
    }

    public void updateEntry(byte newState, long currentTick) {
        if (currentTick >= bucketEnd) {
            long elapsed = (currentTick - bucketEnd) / BUCKET_LENGTH + 1;
            closeBucket();
            int toFill = (int) Math.min(elapsed - 1, HISTORY_LENGTH);
            for (int i = 0; i < toFill; i++) {
                history[head] = (byte) MachineState.NO_DATA.ordinal();
                workTicks[head] = 0;
                updateHead();
            }
            bucketEnd += elapsed * BUCKET_LENGTH;
            updateMainHistoryState();
        }

        state = newState;
        counts[newState]++;
        if (counts[newState] > counts[curMainStateIndex]) curMainStateIndex = newState;
    }

    private void updateHead() {
        head = (head + 1) % HISTORY_LENGTH;
    }

    private void closeBucket() {
        history[head] = curMainStateIndex;
        workTicks[head] = (short) (counts[MachineState.WORKING.ordinal()] + counts[MachineState.LOW_ENERGY.ordinal()]);
        updateHead();
        resetCounts();
        curMainStateIndex = 0;
    }

    private void resetCounts() {
        Arrays.fill(counts, (short) 0);
    }

    private void updateMainHistoryState() {
        int[] counts = new int[MachineState.values.length];
        for (byte state : history) {
            counts[state]++;
        }
        int maxCnt = -1;
        int maxCntState = MachineState.NO_DATA.ordinal();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > maxCnt && i != MachineState.NO_DATA.ordinal()) {
                maxCnt = counts[i];
                maxCntState = i;
            }
        }
        mainHistoryState = (byte) maxCntState;
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public Block getMachine() {
        return machine;
    }

    public MachineState getState() {
        return MachineState.values[state];
    }

    public short[] getCounts() {
        return counts.clone();
    }

    public MachineState getCurrentBucketMainState() {
        return MachineState.values[curMainStateIndex];
    }

    public byte[] getHistory() {
        return ArrayUtils.addAll(
                Arrays.copyOfRange(history, head, history.length),
                Arrays.copyOfRange(history, 0, head)
        );
    }

    public short[] getWorkTicks() {
        return ArrayUtils.addAll(
                Arrays.copyOfRange(workTicks, head, workTicks.length),
                Arrays.copyOfRange(workTicks, 0, head)
        );
    }

    public MachineState getMainHistoryState() {
        return MachineState.values[mainHistoryState];
    }

    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }
}
