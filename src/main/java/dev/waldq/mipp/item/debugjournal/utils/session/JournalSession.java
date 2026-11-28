package dev.waldq.mipp.item.debugjournal.utils.session;

import aztech.modern_industrialization.machines.MachineBlockEntity;

import dev.waldq.mipp.item.debugjournal.GlobalJournal;
import dev.waldq.mipp.item.debugjournal.JournalEntry;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.saveddata.DebugJournalSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import java.util.*;

public class JournalSession {
    private final Player player;
    private final UUID journalUUID;
    private final boolean allowAllMine;

    private FilterMode filter;
    private SortMode sort;
    private GroupMode groupMode;
    private boolean selectAllMine = false;
    private boolean dirty;
    private boolean isUpdating;
    private int refreshCounter;

    private List<JournalRef> refs = List.of();
    private long lastUpdated;

    private static final int UPDATE_INTERVAL_TICKS = 20;


    public JournalSession(Player player, UUID journalUUID, boolean allowAllMine) {
        this.player = player;
        this.journalUUID = journalUUID;
        this.allowAllMine = allowAllMine;

        this.filter = FilterMode.NONE;
        this.sort = SortMode.NO_SORT;
        this.groupMode = GroupMode.NONE;
        this.dirty = true;
    }

    public static List<JournalRef> getRefs(ServerLevel level, Player player, UUID journalUUID, boolean allowAllMine) {
        List<JournalRef> list = new ArrayList<>();
        List<GlobalPos> dead = new ArrayList<>();
        DebugJournalSavedData data = DebugJournalSavedData.get(level);
        if (allowAllMine) {
            GlobalJournal.JOURNAL.forEach((dimension, machines) -> {
                machines.forEach((pos, entry) -> {
                    if (player.getUUID().equals(entry.getOwner())) {
                        if (entry.isValid()) {
                            list.add(new JournalRef(dimension, pos, entry));
                        } else {
                            enqueueToCleanup(dimension, BlockPos.of(pos));
                        }
                    }
                });
            });
        } else {
            List<GlobalPos> bindings = data.getMachines(journalUUID);
            for (var pos : bindings) {
                var machines = GlobalJournal.JOURNAL.get(pos.dimension());
                JournalEntry entry = null;
                if (machines != null) {
                    entry = machines.get(pos.pos().asLong());
                }

                if (entry != null) {
                    if (entry.isValid()) {
                        list.add(new JournalRef(pos.dimension(), pos.pos().asLong(), entry));
                    } else {
                        enqueueToCleanup(pos.dimension(), pos.pos());
                    }
                } else {
                    var dim = level.getServer().getLevel(pos.dimension());
                    if (dim == null || !dim.isLoaded(pos.pos())) {
                        list.add(new JournalRef(pos.dimension(), pos.pos().asLong(), null));
                    } else {
                        if (isMachine(dim, pos.pos())) {
                            list.add(new JournalRef(pos.dimension(), pos.pos().asLong(), null));
                        } else {
                            dead.add(pos);
                        }
                    }
                }

            }
        }
        for (GlobalPos pos : dead) {
            data.unbind(journalUUID, pos);
        }
        return list;
    }

    private static boolean isMachine(ServerLevel level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        return be instanceof MachineBlockEntity;
    }

    private static void enqueueToCleanup(ResourceKey<Level> dim, BlockPos pos) {
        GlobalJournal.GlobalLongChunkPos key = new GlobalJournal.GlobalLongChunkPos(dim, ChunkPos.asLong(pos));
        GlobalJournal.CHUNKS_TO_CLEAN.add(key);
    }

    public static boolean noRefs(ServerLevel level, Player player, UUID journalUUID, boolean allowAllMine) {
        var bindingsAreEmpty = getRefs(level, player, journalUUID, false).isEmpty();
        return allowAllMine ? getRefs(level, player, journalUUID, true).isEmpty()
                && bindingsAreEmpty : bindingsAreEmpty;
    }

    public List<MachineRow> getRows() {
        if (!(player instanceof ServerPlayer serverPlayer)) return List.of();
        ServerLevel level = serverPlayer.serverLevel();

        boolean intervalPassed = level.getGameTime() - lastUpdated >= UPDATE_INTERVAL_TICKS;
        if (dirty || (isUpdating && intervalPassed)) update(level);
        return refs.stream()
                .filter(filter.getPredicate())
                .sorted(sort.getComparator())
                .map(JournalRef::toMachineRow)
                .toList();
    }

    private void update(ServerLevel level) {
        refs = getRefs(level, player, journalUUID, selectAllMine & allowAllMine);
        dirty = false;
        lastUpdated = level.getGameTime();
    }

    public SortMode getSort() {
        return sort;
    }

    public void setSort(SortMode sort) {
        this.sort = sort;
    }

    public FilterMode getFilter() {
        return filter;
    }

    public void setFilter(FilterMode filter) {
        this.filter = filter;
    }

    public GroupMode getGroupMode() {
        return groupMode;
    }

    public void setGroupMode(GroupMode groupMode) {
        this.groupMode = groupMode;
    }


    public boolean isSelectAllMine() {
        return selectAllMine;
    }

    public void setSelectAllMine(boolean selectAllMine) {
        this.selectAllMine = selectAllMine;
        requestUpdate();
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
        if (isUpdating) requestUpdate();
    }

    public int getRefreshCounter() {
        return refreshCounter;
    }

    public void setRefreshCounter(int refreshCounter) {
        this.refreshCounter = refreshCounter;
        requestUpdate();
    }

    private void requestUpdate() {
        dirty = true;
    }

    public record JournalRef(
            ResourceKey<Level> dimension,
            long pos,
            @Nullable JournalEntry entry
    ) {
        public MachineRow toMachineRow() {
            if (entry == null) return MachineRow.unavailable(dimension, pos);
            return new MachineRow(
                    dimension,
                    pos,
                    entry.getMachine(),
                    entry.getCableTier(),
                    entry().isOverdriving(),
                    (byte) entry.getState().ordinal(),
                    entry.getHistory(),
                    (byte) entry.getMainHistoryState().ordinal(),
                    entry.getWorkTicks()
            );
        }
    }
}
