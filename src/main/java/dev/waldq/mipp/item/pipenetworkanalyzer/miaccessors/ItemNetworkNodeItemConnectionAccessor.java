package dev.waldq.mipp.item.pipenetworkanalyzer.miaccessors;

import net.minecraft.core.Direction;

public interface ItemNetworkNodeItemConnectionAccessor {
    Direction mipp$getDirection();

    int mipp$getExtracted();
    void mipp$addExtracted(int amount);
    int mipp$getInserted();
    void mipp$addInserted(int amount);
    void mipp$reset();
}
