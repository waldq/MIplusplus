package dev.waldq.mipp.item.analyzer.miaccessors;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import net.minecraft.core.Direction;

public interface ItemNetworkNodeItemConnectionAccessor {
    Direction mipp$getDirection();
    PipeEndpointType mipp$getPipeEndpointType();

    int mipp$getExtracted();
    void mipp$addExtracted(int amount);
    int mipp$getInserted();
    void mipp$addInserted(int amount);
    void mipp$reset();
}
