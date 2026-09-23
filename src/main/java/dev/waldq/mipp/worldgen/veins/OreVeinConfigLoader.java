package dev.waldq.mipp.worldgen.veins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.mojang.serialization.JsonOps;

import dev.waldq.mipp.MIPP;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public class OreVeinConfigLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "ore_veins";

    private static final Map<ResourceLocation, OreVeinConfig> VEINS = new HashMap<>();

    public OreVeinConfigLoader() { super(GSON, FOLDER); }

    public static Map<ResourceLocation, OreVeinConfig> getAllVeins() { return Collections.unmodifiableMap(VEINS); }

    public static Optional<OreVeinConfig> getVein(ResourceLocation id) { return Optional.ofNullable(VEINS.get(id)); }

    /**
     *   @param dim the dimension we need veins for.
     *   @return List<OreVeinConfig> of vein for the current dimension.
     */
    public static List<OreVeinConfig> getVeinsForDimension(ResourceLocation dim) {
        return VEINS.values().stream()
                .filter(vein -> vein.enabled() && vein.dimData().dimensions().contains(dim))
                .toList();
    }

    /**
     *   @param biome the biome we need veins for.
     *   @param dim the dimension of veins, which will be used if a vein doesn't have biomes specified.
     *   @return List<OreVeinConfig> of vein for the current biome.
     */
    public static List<OreVeinConfig> getVeinsForBiome(Holder<Biome> biome, ResourceLocation dim) {
        return VEINS.values().stream()
                .filter(vein -> {
                    if (vein.enabled()) {
                        if (vein.dimData().biomes().isEmpty() && vein.dimData().dimensions().contains(dim)) return true;

                        for (String tag : vein.dimData().biomes()) {
                            if (tag.startsWith("#")) {
                                ResourceLocation tagId = ResourceLocation.parse(tag.substring(1));
                                TagKey<Biome> key = TagKey.create(Registries.BIOME, tagId);
                                if (biome.is(key)) return true;
                            } else {
                                ResourceLocation biomeId = ResourceLocation.parse(tag);
                                if (biome.is(ResourceKey.create(Registries.BIOME, biomeId))) return true;
                            }
                        }
                    }
                    return false;
                })
                .toList();
    }

    /**
     *   @param dimActiveVeins list of veins for the dimension, not filtered by any biomes.
     *   @param biome the biome we need veins for.
     *   @return List<OreVeinConfig> of vein for the current biome.
     */
    public static List<OreVeinConfig> filterByBiome(List<OreVeinConfig> dimActiveVeins, Holder<Biome> biome) {
        return dimActiveVeins.stream()
                .filter(vein -> {
                    if (vein.enabled()) {
                        if (vein.dimData().biomes().isEmpty()) return true;

                        for (String tag : vein.dimData().biomes()) {
                            if (tag.startsWith("#")) {
                                ResourceLocation tagId = ResourceLocation.parse(tag.substring(1));
                                TagKey<Biome> key = TagKey.create(Registries.BIOME, tagId);
                                if (biome.is(key)) return true;
                            } else {
                                ResourceLocation biomeId = ResourceLocation.parse(tag);
                                if (biome.is(ResourceKey.create(Registries.BIOME, biomeId))) return true;
                            }
                        }
                    }
                    return false;
                })
                .toList();
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller
    ) {
        VEINS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation fileId = entry.getKey();

            OreVeinConfig.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> MIPP.LOGGER.error("Failed to load ore vein from {}: {}", fileId, error))
                    .ifPresent(config -> {
                        VEINS.put(config.id(), config);
                    });
        }
    }
}
