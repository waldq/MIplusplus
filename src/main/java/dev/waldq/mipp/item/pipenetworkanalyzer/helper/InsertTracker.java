package dev.waldq.mipp.item.pipenetworkanalyzer.helper;

import dev.waldq.mipp.item.pipenetworkanalyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class InsertTracker {
    private static final Long2ObjectOpenHashMap<ItemNetworkNodeItemConnectionAccessor[]> CONNECTIONS = new Long2ObjectOpenHashMap<>();

    private InsertTracker() {}

    public static void clear() {
        CONNECTIONS.clear();
    }

    public static void register(BlockPos pos, ItemNetworkNodeItemConnectionAccessor[] connections) {
        CONNECTIONS.put(pos.asLong(), connections);
    }

    public static void addInserted(BlockPos pos, Direction direction, int amount) {
        var conns = CONNECTIONS.get(pos.asLong());
        if (conns != null) {
            var conn = conns[direction.get3DDataValue()];
            if (conn != null) {
                conn.mipp$addInserted(amount);
            }
        }
    }
}