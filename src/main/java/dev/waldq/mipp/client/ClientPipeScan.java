package dev.waldq.mipp.client;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.network.packet.PipeScanPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ClientPipeScan {
    public record Label(BlockPos pos, ThroughputColor color, Component line) {}

    public static volatile List<Label> labels = List.of();
    public static volatile ResourceKey<Level> dimension = null;

    public static void handle(PipeScanPacket packet, ResourceKey<Level> dim) {
        if (packet.entries().isEmpty()) {
            clear();
            return;
        }

        List<Label> list = new ArrayList<>(packet.entries().size());

        for (var entry : packet.entries()) {
            var inserted = entry.inserted();
            var extracted = entry.extracted();

            String line = null;

            if (inserted > 0 && extracted > 0) {
                line = "+%d | -%d".formatted(inserted, extracted);
            } else if (inserted > 0) {
                line = "+%s".formatted(inserted);
            } else if (extracted > 0) {
                line = "-%s".formatted(extracted);
            }

            ThroughputColor color = ThroughputColor.fromValue(extracted, inserted);

            list.add(new Label(entry.pos(), color,  line != null ? Component.literal(line) : Component.literal("")));
        }
        labels = list;
        dimension = dim;
    }

    public static void clear() {
        labels = List.of();
        dimension = null;
    }
}
