package dev.waldq.mipp.item.debugjournal;

import dev.waldq.mipp.MIPP;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = MIPP.ID)
public final class GlobalJournal {
    public static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<JournalEntry>> JOURNAL = new HashMap<>();
    public static final Object2ObjectOpenHashMap<GlobalLongChunkPos, LongOpenHashSet> CHUNK_BLOCKS = new Object2ObjectOpenHashMap<>();
    public static final LinkedHashSet<GlobalLongChunkPos> CHUNKS_TO_CLEAN = new LinkedHashSet<>();


    @SubscribeEvent
    private static void onServerStop(ServerStoppedEvent event) {
        JOURNAL.clear();
        CHUNK_BLOCKS.clear();
        CHUNKS_TO_CLEAN.clear();
    }

    @SubscribeEvent
    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            GlobalLongChunkPos key = new GlobalLongChunkPos(level.dimension(), event.getChunk().getPos().toLong());
            CHUNKS_TO_CLEAN.remove(key);
        }
    }

    @SubscribeEvent
    private static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            GlobalLongChunkPos key = new GlobalLongChunkPos(level.dimension(), event.getChunk().getPos().toLong());
            CHUNKS_TO_CLEAN.add(key);
        }
    }

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Post event) {
        var iterator = CHUNKS_TO_CLEAN.iterator();
        if (!iterator.hasNext()) return;

        GlobalLongChunkPos key = iterator.next();
        iterator.remove();

        ServerLevel level = event.getServer().getLevel(key.dimension());
        ChunkPos pos = new ChunkPos(key.chunkPos());
        if (level != null) { // TODO potentially make the number of chunks cleared configurable
            cleanChunk(level, key, level.hasChunk(pos.x, pos.z));
        }
    }

    private static void cleanChunk(ServerLevel level, GlobalLongChunkPos key, boolean isLoaded) {
        LongOpenHashSet blockPositions = isLoaded ? CHUNK_BLOCKS.get(key) : CHUNK_BLOCKS.remove(key);
        if (blockPositions == null || blockPositions.isEmpty()) return;

        Long2ObjectOpenHashMap<JournalEntry> map = JOURNAL.get(key.dimension());
        if (map == null) {
            CHUNK_BLOCKS.remove(key);
            return;
        }

        var iterator = blockPositions.iterator();
        while (iterator.hasNext()) {
            long pos = iterator.nextLong();

            JournalEntry entry = map.remove(pos);
            if (entry == null) {
                iterator.remove();
                continue;
            }

            if (isLoaded) {
                var be = level.getBlockEntity(BlockPos.of(pos));
                if (be != null && entry.isValid() && be.getBlockState().getBlock() == entry.getMachine()) {
                    map.put(pos, entry);
                } else {
                    entry.invalidate();
                    iterator.remove();
                }
            } else {
                entry.invalidate();
            }
        }

        if (map.isEmpty()) JOURNAL.remove(key.dimension());
        if (blockPositions.isEmpty()) CHUNK_BLOCKS.remove(key);
    }

    public record GlobalLongChunkPos(ResourceKey<Level> dimension, long chunkPos) {}

    private GlobalJournal() {}
}
