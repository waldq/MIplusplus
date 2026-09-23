package dev.waldq.mipp.datagen.server;

import dev.waldq.mipp.datagen.server.provider.datamaps.DataMapDatagenProvider;
import dev.waldq.mipp.datagen.server.provider.loottable.BlockLootTableDatagenProvider;
import dev.waldq.mipp.datagen.server.provider.recipes.CommonResipesServerDatagenProvider;
import dev.waldq.mipp.datagen.server.provider.tags.BlockTagDatagenProvider;
import dev.waldq.mipp.datagen.server.provider.tags.ItemTagDatagenProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class DatagenDelegatorServer {
	public static void configure(GatherDataEvent event) {
		add(event, DataMapDatagenProvider::new);
		addLootTable(event, BlockLootTableDatagenProvider::new);
		add(event, CommonResipesServerDatagenProvider::new);

//		add(event, DamageTypeTagDatagenProvider::new);
//		add(event, FluidTagDatagenProvider::new);

		event.createBlockAndItemTags(
				(output, lookupProvider) -> new BlockTagDatagenProvider(output, lookupProvider, event.getExistingFileHelper()),
				(output, lookupProvider, blockTags) -> new ItemTagDatagenProvider(output, lookupProvider, blockTags, event.getExistingFileHelper())
		);
	}
	
	private static void add(GatherDataEvent event, Function<GatherDataEvent, DataProvider> providerCreator) {
		event.getGenerator().addProvider(event.includeServer(), providerCreator.apply(event));
	}
	
	private static void addLootTable(GatherDataEvent event, Function<HolderLookup.Provider, LootTableSubProvider> providerCreator) {
		event.getGenerator().addProvider(
				event.includeServer(),
				new LootTableProvider(
						event.getGenerator().getPackOutput(),
						Set.of(),
						List.of(new LootTableProvider.SubProviderEntry(providerCreator, LootContextParamSets.BLOCK)),
						event.getLookupProvider()
				)
		);
	}
}
