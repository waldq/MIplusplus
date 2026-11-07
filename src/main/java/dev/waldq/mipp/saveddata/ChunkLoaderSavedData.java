package dev.waldq.mipp.saveddata;

import aztech.modern_industrialization.api.energy.CableTier;
import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPAttachments;

import dev.waldq.mipp.blocks.machine.utils.CableTierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ChunkLoaderSavedData extends SavedData {

    public record LoaderInfo(UUID owner, ResourceLocation dimension, BlockPos pos, CableTier tier) {

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Owner", owner);
            tag.putString("Dimension", dimension.toString());
            tag.putInt("X", pos.getX());
            tag.putInt("Y", pos.getY());
            tag.putInt("Z", pos.getZ());
            tag.putString("Tier", tier.name);
            return tag;
        }

        public static LoaderInfo load(CompoundTag tag) {
            UUID owner = tag.getUUID("Owner");
            ResourceLocation dimension = ResourceLocation.parse(tag.getString("Dimension"));
            BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
            CableTier tier;
            try {

                tier = CableTier.getTier(tag.getString("Tier"));
            } catch (NoSuchElementException e) {
                MIPP.LOGGER.warn("Skipping chunk loader at {} in {}: unknown cable tier '{}'",
                        pos, dimension, tag.getString("Tier"));
                return null;
            }

            if (!CableTierUtils.CableNumbers.containsKey(tier)) {
                MIPP.LOGGER.warn("Skipping chunk loader at {} in {}: no loader data for tier '{}'",
                        pos, dimension, tier.name);
                return null;
            }
            return new LoaderInfo(owner, dimension, pos, tier);
        }
    }

    public record LoaderKey(ResourceLocation dimension, BlockPos pos) {}

    private final Map<LoaderKey, LoaderInfo> loaders = new HashMap<>();

    public static ChunkLoaderSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(
                            ChunkLoaderSavedData::new,
                            ChunkLoaderSavedData::load
                    ),
                    "mipp_chunk_loaders"
            );
        }
        throw new IllegalStateException("Cannot access SavedData from client side.");
    }


    public void addLoader(ServerLevel level, UUID owner, BlockPos pos, CableTier tier) {
        LoaderKey key = new LoaderKey(level.dimension().location(), pos.immutable());
        loaders.put(key, new LoaderInfo(owner, level.dimension().location(), pos.immutable(), tier));
        this.setDirty();

        syncPlayerChunks(level.getServer(), owner);
    }


    public void removeLoader(ServerLevel level, BlockPos pos) {
        LoaderKey key = new LoaderKey(level.dimension().location(), pos);
        LoaderInfo removed = loaders.remove(key);
        if (removed != null) {
            this.setDirty();
            syncPlayerChunks(level.getServer(), removed.owner());
        }
    }


    public void syncPlayerChunks(MinecraftServer server, UUID playerUUID) {
        if (server == null) return;
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            int totalChunks = getChunks(playerUUID);
            player.setData(MIPPAttachments.CHUNK_AMOUNT, totalChunks);
        }
    }


    public int getChunks(UUID playerUUID) {
        return loaders.values().stream()
                .filter(info -> info.owner().equals(playerUUID))
                .mapToInt(info -> CableTierUtils.CableNumbers.get(info.tier()).chunkAmount())
                .sum();
    }


    public List<LoaderInfo> getLoadersByPlayer(UUID playerUUID) {
        return loaders.values().stream()
                .filter(info -> info.owner().equals(playerUUID))
                .toList();
    }


    public Map<LoaderKey, LoaderInfo> getAllLoaders() {
        return Collections.unmodifiableMap(loaders);
    }


    public static ChunkLoaderSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ChunkLoaderSavedData data = new ChunkLoaderSavedData();
        ListTag list = tag.getList("Loaders", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            LoaderInfo info = LoaderInfo.load(list.getCompound(i));
            if (info == null) continue;
            data.loaders.put(new LoaderKey(info.dimension(), info.pos()), info);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (LoaderInfo info : loaders.values()) {
            list.add(info.save());
        }
        tag.put("Loaders", list);
        return tag;
    }
}