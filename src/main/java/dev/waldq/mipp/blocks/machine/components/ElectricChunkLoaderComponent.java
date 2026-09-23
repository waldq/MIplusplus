package dev.waldq.mipp.blocks.machine.components;

import aztech.modern_industrialization.machines.MachineComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.world.chunk.TicketController;

public class ElectricChunkLoaderComponent implements MachineComponent.ServerOnly {
    private final TicketController ticketController = ChunkLoaderComponent.getTicketController();

    public void setChunks(Level level, BlockPos pos, int range, boolean add) {
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;
        for (int x = centerChunkX - range; x < centerChunkX + range + 1; x++){
            for (int z = centerChunkZ - range; z < centerChunkZ + range + 1; z++){
                ticketController.forceChunk((ServerLevel) level, pos, x, z, add, true);
            }
        }
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {}

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {}
}
