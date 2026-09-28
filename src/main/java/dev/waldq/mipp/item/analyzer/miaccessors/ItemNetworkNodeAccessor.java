package dev.waldq.mipp.item.analyzer.miaccessors;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface ItemNetworkNodeAccessor {
    ItemNetworkNodeItemConnectionAccessor[] mipp$getItemConnections(BlockPos pos);
    List<?> mipp$getConnections();
}
