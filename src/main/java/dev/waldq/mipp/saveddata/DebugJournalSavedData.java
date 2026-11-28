package dev.waldq.mipp.saveddata;

import dev.waldq.mipp.MIPP;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DebugJournalSavedData extends SavedData {
    private static final Factory<DebugJournalSavedData> FACTORY = new Factory<>(
            DebugJournalSavedData::new,
            DebugJournalSavedData::load
    );
    private static final String ID = "mipp_debug_journal";

    public enum BindingResult {
        BOUND,
        UNBOUND,
        BOUND_AND_REMOVED,
        UNKNOWN
    }

    public static class JournalBindings {
        private final LinkedHashSet<GlobalPos> machines;
        private long lastAccess;

        public JournalBindings(LinkedHashSet<GlobalPos> machines, long lastAccess) {
            this.machines = machines;
            this.lastAccess = lastAccess;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (GlobalPos machine : machines) {
                list.add(GlobalPos.CODEC
                        .encodeStart(NbtOps.INSTANCE, machine)
                        .getOrThrow(s -> new IllegalStateException("Invalid GlobalPos " + s))
                );
            }
            tag.put("Machines", list);
            tag.putLong("LastAccess", lastAccess);
            return tag;
        }

        public static JournalBindings load(CompoundTag tag) {
            LinkedHashSet<GlobalPos> set = new LinkedHashSet<>();
            ListTag list = tag.getList("Machines", Tag.TAG_COMPOUND);
            for (Tag value : list) {
                GlobalPos.CODEC
                        .parse(NbtOps.INSTANCE, value)
                        .result().ifPresent(set::add);
            }
            long lastAccess = tag.getLong("LastAccess");
            return new JournalBindings(set, lastAccess);
        }

        public void touch() {
            lastAccess = System.currentTimeMillis();
        }
    }

    private final Map<UUID, JournalBindings> journalBindings = new HashMap<>();

    public static DebugJournalSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public BindingResult bindOrUnbind(UUID journalUUID, GlobalPos pos, int limit) {
        JournalBindings bindings = journalBindings
                .computeIfAbsent(
                        journalUUID,
                        id -> new JournalBindings(new LinkedHashSet<>(), System.currentTimeMillis())
                );
        if (bindings.machines.isEmpty() || !bindings.machines.contains(pos)) {
            return bind(journalUUID, pos, limit);
        } else {
            return unbind(journalUUID, pos);
        }
    }

    public BindingResult bind(UUID journalUUID, GlobalPos pos, int limit) {
        JournalBindings bindings = journalBindings
                .computeIfAbsent(
                        journalUUID,
                        id -> new JournalBindings(new LinkedHashSet<>(), System.currentTimeMillis())
                );
        bindings.machines.add(pos);
        boolean removed = false;
        if (bindings.machines.size() > limit) {
            bindings.machines.removeFirst();
            removed = true;
        }
        setDirty();
        bindings.touch();
        if (removed) {
            return BindingResult.BOUND_AND_REMOVED;
        }
        return BindingResult.BOUND;
    }

    public BindingResult unbind(UUID journalUUID, GlobalPos pos) {
        JournalBindings bindings = journalBindings.get(journalUUID);
        if (bindings == null) return BindingResult.UNKNOWN;
        if (bindings.machines.remove(pos)) setDirty();
        bindings.touch();
        return BindingResult.UNBOUND;
    }

    public List<GlobalPos> getMachines(UUID journalUUID) {
        JournalBindings bindings = journalBindings.get(journalUUID);
        if (bindings == null) return List.of();
        bindings.touch();
        setDirty();
        return List.copyOf(bindings.machines);
    }

    public List<JournalBindings> getAllBindings() {
        return journalBindings.values().stream().map(b -> new JournalBindings(new LinkedHashSet<>(b.machines), b.lastAccess)).toList();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag journalTag = new CompoundTag();
        journalBindings.forEach((uuid, bindings) -> journalTag.put(uuid.toString(), bindings.save()));
        tag.put("Journal", journalTag);
        return tag;
    }

    public static DebugJournalSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DebugJournalSavedData data = new DebugJournalSavedData();
        CompoundTag journalTag = tag.getCompound("Journal");
        boolean changed = false;
        int maxLifeSpan = MIPP.config().debugJournal().maxEntryLifeSpan();
        long curTime = System.currentTimeMillis();
        for (String key : journalTag.getAllKeys()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            JournalBindings bindings = JournalBindings.load(journalTag.getCompound(key));
            if (maxLifeSpan == 0
                    || curTime - bindings.lastAccess <
                    TimeUnit.DAYS.toMillis(maxLifeSpan)
            ) {
                data.journalBindings.put(uuid, bindings);
            } else {
                changed = true;
            }

        }
        if (changed) data.setDirty();
        return data;
    }
}
