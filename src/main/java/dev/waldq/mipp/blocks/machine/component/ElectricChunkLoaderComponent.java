package dev.waldq.mipp.blocks.machine.component;

import aztech.modern_industrialization.machines.MachineComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ElectricChunkLoaderComponent implements MachineComponent.ServerOnly {
    private int time = 100;
    public boolean isEnabled = false;

    public void resetTime() { time = 100; }

    public void decreaseTime() { time--; }

    public int getTime() { return time; }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("mipp_cl_enabled", isEnabled);
        tag.putInt("mipp_cl_time", time);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        time = tag.getInt("mipp_cl_time");
        isEnabled = tag.getBoolean("mipp_cl_enabled");
    }
}
