package dev.waldq.mipp.datagen.client;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.datagen.client.provider.LanguageDatagenProvider;
import dev.waldq.mipp.datagen.client.provider.models.BlockModelsDatagenProvider;
import dev.waldq.mipp.datagen.client.provider.models.ItemModelsDatagenProvider;
import net.minecraft.data.DataProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.swedz.tesseract.neoforge.datagen.mi.MIDatagenHooks;

import java.util.function.Function;

public final class DatagenDelegatorClient
{
	public static void configure(GatherDataEvent event)
	{
		MIDatagenHooks.Client.includeMISprites(event);
		
//		MIDatagenHooks.Client.addTexturesHook(event, MIPP.MODID, MIPPFluids.values());
		MIDatagenHooks.Client.addMachineCasingModelsHook(event, MIPP.MODID);
		
		add(event, BlockModelsDatagenProvider::new);
		add(event, ItemModelsDatagenProvider::new);
		add(event, LanguageDatagenProvider::new);
	}
	
	private static void add(GatherDataEvent event, Function<GatherDataEvent, DataProvider> providerCreator)
	{
		event.getGenerator().addProvider(event.includeClient(), providerCreator.apply(event));
	}
}
