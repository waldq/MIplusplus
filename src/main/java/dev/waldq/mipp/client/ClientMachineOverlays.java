package dev.waldq.mipp.client;

import dev.waldq.mipp.item.debugjournal.MachineState;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClientMachineOverlays {
    public static class Overlay {
        public final BlockPos pos;
        public MachineState state;

        public Overlay(BlockPos pos, MachineState state) {
            this.pos = pos;
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof Overlay other && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }

    public static volatile Set<Overlay> overlays = new HashSet<>();
    public static volatile ResourceKey<Level> dimension;

    public static boolean addOrRemoveMachine(MachineRow row) {
        if (!row.dimension().equals(dimension)) {
            removeAll();
            dimension = row.dimension();
        }
        var overlay = new Overlay(BlockPos.of(row.pos()), MachineState.values[row.state()]);

        if (overlays.contains(overlay)) return overlays.remove(overlay);
        return overlays.add(overlay);
    }

    public static boolean addMachine(Overlay overlay) {
        return overlays.add(overlay);
    }

    public static boolean removeMachine(Overlay overlay) {
        return overlays.remove(overlay);
    }

    public static void removeAll() {
        overlays.clear();
    }
}
