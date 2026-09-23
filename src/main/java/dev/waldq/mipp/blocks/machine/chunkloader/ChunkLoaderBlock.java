package dev.waldq.mipp.blocks.machine.chunkloader;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntity;

import dev.waldq.mipp.MIPPConfig;
import dev.waldq.mipp.blocks.machine.blockentities.ElectricChunkLoaderBlockEntity;
import dev.waldq.mipp.blocks.machine.utils.CableTierUtils;

import dev.waldq.mipp.saveddata.ChunkLoaderSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

import static dev.waldq.mipp.MIPPAttachments.CHUNK_AMOUNT;

public class ChunkLoaderBlock extends MachineBlock {
    private final CableTier cableTier;

    public ChunkLoaderBlock(
            BiFunction<BlockPos, BlockState, MachineBlockEntity> factory,
            BlockBehaviour.Properties properties,
            CableTier cableTier
    ) {
        super(factory, properties);
        this.cableTier = cableTier;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                ChunkLoaderSavedData.get(level).removeLoader(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    public int getChunksAmount() {
        return CableTierUtils.CableNumbers.get(cableTier).chunkAmount();
    }

    public int getMaxChunksAmount() {
        return MIPPConfig.CHUNKS_PER_PLAYER.getAsInt();
    }
}