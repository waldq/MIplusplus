package dev.waldq.mipp;

import dev.waldq.mipp.datamaps.OreMapData;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class MIPPDataMaps {
    public static final DataMapType<Block, OreMapData> ORE_COLOR = DataMapType.builder(
            MIPP.id("ore_color"),
            Registries.BLOCK,
            OreMapData.CODEC
    ).synced(OreMapData.CODEC, true).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ORE_COLOR);
    }
}
