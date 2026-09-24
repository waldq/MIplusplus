package dev.waldq.mipp.item.pospector.utils;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPConfig;
import dev.waldq.mipp.worldgen.veins.OreVeinConfig;
import dev.waldq.mipp.worldgen.veins.VeinPlacerHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import net.neoforged.neoforge.common.Tags;

import java.util.*;

public class ScannerLocal  {
    private final Player player;

    private final List<SectionPos> pendingChunkSections = new ArrayList<>();
    private final ServerLevel level;
    private int currentChunkSectionIndex = 0;

    private final Set<BlockState>[] foundOres;
    private final Set<BlockState> uniqueOres = new HashSet<>();
    private boolean isScanning = false;

    private final int worldSideLen;
    private final int chunkRadius;
    private int minRegX;
    private int minRegZ;


    @SuppressWarnings("unchecked")
    public ScannerLocal(ServerLevel level, Player player, int chunkRadius) {
        this.level = level;
        this.player = player;
        this.chunkRadius = chunkRadius;
        this.worldSideLen = (chunkRadius * 2 + 1) * 16;
        this.foundOres = (Set<BlockState>[]) new Set[worldSideLen * worldSideLen];
    }

    // Collects chunk sections around the player in range for the scanning later
    public void collectChunks(List<OreVeinConfig> activeVeins) {
        BlockPos centerPos = player.blockPosition();
        this.pendingChunkSections.clear();
        this.currentChunkSectionIndex = 0;

        this.uniqueOres.clear();
        Arrays.fill(this.foundOres, null);

        long seed = this.level.getSeed();
        ResourceLocation dim = this.level.dimension().location();

        int centerChunkX = centerPos.getX() >> 4;
        int centerChunkZ = centerPos.getZ() >> 4;

        this.minRegX = (centerChunkX - chunkRadius) << 4;
        this.minRegZ = (centerChunkZ - chunkRadius) << 4;

        int minSectionY = this.level.getMinSection();
        int maxSectionY = this.level.getMaxSection();

        for (int curX = -this.chunkRadius; curX <= this.chunkRadius; curX++) {
            for (int curZ = -this.chunkRadius; curZ <= this.chunkRadius; curZ++) {
                int chunkX = centerChunkX + curX;
                int chunkZ = centerChunkZ + curZ;

                RandomSource random = VeinPlacerHelper.seededXoroshiroRandomSource(
                        seed,
                        chunkX,
                        chunkZ,
                        dim
                        );
                // These lines will make scanning faster,
                // but also will prevent prospector from seeing non-MI++ generated ores.
//                OreVeinConfig vein = VeinGenHelpers.selectVein(activeVeins, random);
//                if (vein == null) continue;

                if (!this.level.hasChunk(chunkX, chunkZ)) continue;

                for (int sectionY = minSectionY; sectionY < maxSectionY; sectionY++) {
                    pendingChunkSections.add(SectionPos.of(chunkX, sectionY, chunkZ));
                }
            }
        }

        this.isScanning = true;
    }

    // Scanning is spread across multiple server ticks to reduce per-tick load
    public void onUse() {
        if (!isScanning || pendingChunkSections.isEmpty()) {
            isScanning = false;
            return;
        }

        final int sectionsToProcess = MIPP.config().prospectorPerameters().chunksPerTick();

        for (int i = 0; i < sectionsToProcess; i++) {
            if (currentChunkSectionIndex >= pendingChunkSections.size()) {
                isScanning = false;
                break;
            }

            SectionPos sectionPos = pendingChunkSections.get(currentChunkSectionIndex);
            currentChunkSectionIndex++;
            processSection(sectionPos);
        }
    }


    private void processSection(SectionPos sectionPos) {
        int chunkX = sectionPos.x();
        int chunkZ = sectionPos.z();
        int sectionY = sectionPos.y();

        // Prevents processing not yet generated chunks
        LevelChunk chunk = this.level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) return;

        int sectionIndex = chunk.getSectionIndexFromSectionY(sectionY);
        LevelChunkSection[] sections = chunk.getSections();

        if (sectionIndex < 0 || sectionIndex >= sections.length) return;

        LevelChunkSection section = sections[sectionIndex];
        if (section == null || section.hasOnlyAir()) return;

        PalettedContainer<BlockState> palette = section.getStates();

        // Fast check if a section has any ore at all
        if (!palette.maybeHas(state ->
                !state.isAir() && state.is(Tags.Blocks.ORES))) return;

        int originX = sectionPos.minBlockX();
        int originZ = sectionPos.minBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int relX = (originX + x) - this.minRegX;
                int relZ = (originZ + z) - this.minRegZ;

                // Easier to work with ore positions without Y coordinate
                int arrayIndex = relX + relZ * this.worldSideLen;

                for (int y = 0; y < 16; y++) {
                    final BlockState state = palette.get(x, y, z);

                    if (state.isAir() || !state.is(Tags.Blocks.ORES)) continue;

                    if (foundOres[arrayIndex] == null) {
                        foundOres[arrayIndex] = new HashSet<>(2);
                    }

                    if (foundOres[arrayIndex].add(state)) {
                        uniqueOres.add(state);
                    }
                }
            }
        }
    }

    public Set<BlockState>[] getFoundOres() { return foundOres; }

    public Set<BlockState> getUniqueOres() { return uniqueOres; }

    public boolean isScanning() { return isScanning; }
}