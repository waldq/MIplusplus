package dev.waldq.mipp.datagen.server.provider.datamaps;

import aztech.modern_industrialization.MI;

import dev.waldq.mipp.MIPPDataMaps;
import dev.waldq.mipp.datamaps.OreMapData;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;

public class DataMapDatagenProvider extends DataMapProvider {
    public DataMapDatagenProvider(GatherDataEvent event) {
        super(event.getGenerator().getPackOutput(), event.getLookupProvider());
    }

    @Override
    protected void gather(HolderLookup.Provider registries) {
        this.addOreMapData(Tags.Blocks.ORES_COAL, 0x000000, Blocks.COAL_ORE);
        this.addOreMapData(Tags.Blocks.ORES_COPPER, 0xe65d0e, Blocks.COPPER_ORE);
        this.addOreMapData(Tags.Blocks.ORES_DIAMOND, 0x10d7de, Blocks.DIAMOND_ORE);
        this.addOreMapData(Tags.Blocks.ORES_EMERALD, 0x75e069, Blocks.EMERALD_ORE);
        this.addOreMapData(Tags.Blocks.ORES_GOLD, 0xd4c00d, Blocks.GOLD_ORE);
        this.addOreMapData(Tags.Blocks.ORES_IRON, 0xd4bca7, Blocks.IRON_ORE);
        this.addOreMapData(Tags.Blocks.ORES_LAPIS, 0x3843e0, Blocks.LAPIS_ORE);
        this.addOreMapData(Tags.Blocks.ORES_QUARTZ, 0xe8c4b5, BuiltInRegistries.BLOCK.get(MI.id("quartz_ore")));
        this.addOreMapData(Tags.Blocks.ORES_REDSTONE, 0xd91a1a, Blocks.REDSTONE_ORE);

        TagKey<Block> tagAntimony = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/antimony"));
        TagKey<Block> tagBauxite = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/bauxite"));
        TagKey<Block> tagIridium = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/iridium"));
        TagKey<Block> tagLead = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/lead"));
        TagKey<Block> tagLignite = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/lignite_coal"));
        TagKey<Block> tagMonazite = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/monazite"));
        TagKey<Block> tagNickel = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/nickel"));
        TagKey<Block> tagPlatinum = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/platinum"));
        TagKey<Block> tagSalt = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/salt"));
        TagKey<Block> tagTin = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/tin"));
        TagKey<Block> tagTitanium = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/titanium"));
        TagKey<Block> tagTungsten = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/tungsten"));
        TagKey<Block> tagUranium = TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores/uranium"));

        this.addOreMapData(tagAntimony, 0xdcdcf0, BuiltInRegistries.BLOCK.get(MI.id("antimony_ore")));
        this.addOreMapData(tagBauxite, 0xc86400, BuiltInRegistries.BLOCK.get(MI.id("bauxite_ore")));
        this.addOreMapData(tagIridium, 0xe1e6f5, BuiltInRegistries.BLOCK.get(MI.id("iridium_ore")));
        this.addOreMapData(tagLead, 0x6a76bc, BuiltInRegistries.BLOCK.get(MI.id("lead_ore")));
        this.addOreMapData(tagLignite, 0x644646, BuiltInRegistries.BLOCK.get(MI.id("lignite_coal_ore")));
        this.addOreMapData(tagMonazite, 0x96248e, BuiltInRegistries.BLOCK.get(MI.id("monazite_ore")));
        this.addOreMapData(tagNickel, 0xfafac8, BuiltInRegistries.BLOCK.get(MI.id("nickel_ore")));
        this.addOreMapData(tagPlatinum, 0xffe5ba, BuiltInRegistries.BLOCK.get(MI.id("platinum_ore")));
        this.addOreMapData(tagSalt, 0xc7d6c5, BuiltInRegistries.BLOCK.get(MI.id("salt_ore")));
        this.addOreMapData(tagTin, 0xc0bcd0, BuiltInRegistries.BLOCK.get(MI.id("tin_ore")));
        this.addOreMapData(tagTitanium, 0xdca0f0, BuiltInRegistries.BLOCK.get(MI.id("titanium_ore")));
        this.addOreMapData(tagTungsten, 0x8760ad, BuiltInRegistries.BLOCK.get(MI.id("tungsten_ore")));
        this.addOreMapData(tagUranium, 0x39e600, BuiltInRegistries.BLOCK.get(MI.id("uranium_ore")));
    }

    private void addOreMapData(TagKey<Block> tag, int color, Block mainBlock) {
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(mainBlock);
        this.addOreMapData(tag, color, loc);
    }

    private void addOreMapData(ResourceLocation block, int color) {
        this.builder(MIPPDataMaps.ORE_COLOR).add(block, new OreMapData(color, Optional.empty()), false);
    }

    private void addOreMapData(TagKey<Block> tag, int color, ResourceLocation mainBlock) {
        this.builder(MIPPDataMaps.ORE_COLOR).add(tag, new OreMapData(color, Optional.ofNullable(mainBlock)), false);
    }

}
