package dev.waldq.mipp.item.analyzer.helper;

import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

public final class InsertTracker {
    private static final Map<BlockPos, ItemNetworkNodeItemConnectionAccessor[]> CONNECTIONS = new HashMap<>();

    private InsertTracker() {}

    public static void clear() {
        CONNECTIONS.clear();
    }

    public static void register(BlockPos pos, ItemNetworkNodeItemConnectionAccessor[] connections) {
        CONNECTIONS.put(pos.immutable(), connections);
    }

    public static void addInserted(BlockPos pos, Direction direction, int amount) {
        var conns = CONNECTIONS.get(pos);
        if (conns != null) {
            var conn = conns[direction.get3DDataValue()];
            if (conn != null) {
                conn.mipp$addInserted(amount);
            }
        }
    }
}