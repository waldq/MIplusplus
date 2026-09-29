package dev.waldq.mipp.item.analyzer.net;


import aztech.modern_industrialization.pipes.item.ItemNetwork;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeAccessor;
import dev.waldq.mipp.network.packet.PipeScanPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public final class PipeScanner {
    private PipeScanner() {}

    public static void scan(ServerPlayer player, ItemNetwork network) {
        if (network == null) {
            clear(player);
            return;
        }

        Map<BlockPos, int[]> blockStat = new LinkedHashMap<>();

        for (var node : network.iterateTickingNodes()) {
            if (!(node.getNode() instanceof ItemNetworkNodeAccessor itemNetworkNode)) continue;

            BlockPos pipePos = node.getPos();

            for (var conn : itemNetworkNode.mipp$getItemConnections(pipePos)) {
                if (conn != null) {
                    BlockPos pos = pipePos.relative(conn.mipp$getDirection());

                    int[] totals = blockStat.computeIfAbsent(pos, otherPos -> new int[2]);

                    totals[0] += conn.mipp$getExtracted();
                    totals[1] += conn.mipp$getInserted();
                }
            }
        }

        List<PipeScanPacket.PipeScanEntry> entries = new ArrayList<>(blockStat.size());

        blockStat.forEach((pos, totals) -> entries.add(new PipeScanPacket.PipeScanEntry(pos, totals[0], totals[1])));

        new PipeScanPacket(entries).sendToClient(player);
    }

    public static void clear(ServerPlayer player) {
        new PipeScanPacket(List.of()).sendToClient(player);
    }
}