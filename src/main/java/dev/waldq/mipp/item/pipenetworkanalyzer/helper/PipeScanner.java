package dev.waldq.mipp.item.pipenetworkanalyzer.helper;

import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.item.ItemNetwork;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.pipenetworkanalyzer.miaccessors.ItemNetworkNodeAccessor;
import dev.waldq.mipp.network.packet.PipeScanPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class PipeScanner {
    private PipeScanner() {}

    public static void scan(ServerPlayer player, ItemNetwork network) {
        if (network == null) {
            clear(player);
            return;
        }

        Long2ObjectOpenHashMap<int[]> blockStat = new Long2ObjectOpenHashMap<>();

        for (var node : network.iterateTickingNodes()) {
            processNode(node, blockStat);
        }

        if (blockStat.isEmpty()) player.displayClientMessage(MIPP.text().pipeNetworkAnalyzerEmptyNetwork(), true);

        List<PipeScanPacket.PipeScanEntry> entries = new ArrayList<>(blockStat.size());
        blockStat.forEach((longPos, totals) -> entries.add(new PipeScanPacket.PipeScanEntry(BlockPos.of(longPos), totals[0], totals[1])));
        new PipeScanPacket(entries).sendToClient(player);
    }

    public static void scan(ServerPlayer player, PipeNetwork.PosNode node) {
        if (node == null) {
            clear(player);
            return;
        }

        Long2ObjectOpenHashMap<int[]> blockStat = new Long2ObjectOpenHashMap<>();

        processNode(node, blockStat);

        if (blockStat.isEmpty()) player.displayClientMessage(MIPP.text().pipeNetworkAnalyzerEmptyNode(), true);

        List<PipeScanPacket.PipeScanEntry> entries = new ArrayList<>(blockStat.size());
        blockStat.forEach((longPos, totals) -> entries.add(new PipeScanPacket.PipeScanEntry(BlockPos.of(longPos), totals[0], totals[1])));
        new PipeScanPacket(entries).sendToClient(player);
    }

    private static void processNode(PipeNetwork.PosNode node, Long2ObjectOpenHashMap<int[]> blockStat) {
        if (!(node.getNode() instanceof ItemNetworkNodeAccessor itemNetworkNode)) return;

        BlockPos pipePos = node.getPos();

        for (var conn : itemNetworkNode.mipp$getItemConnections(pipePos)) {
            if (conn != null) {
                BlockPos pos = pipePos.relative(conn.mipp$getDirection());

                int[] totals = blockStat.computeIfAbsent(pos.asLong(), otherPos -> new int[2]);

                totals[0] += conn.mipp$getExtracted();
                totals[1] += conn.mipp$getInserted();
            }
        }
    }

    public static void clear(ServerPlayer player) {
        player.displayClientMessage(MIPP.text().pipeNetworkAnalyzerReset(), true);
        new PipeScanPacket(List.of()).sendToClient(player);
    }
}