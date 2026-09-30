package dev.waldq.mipp.datagen.client.provider;

import com.google.common.collect.Sets;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.MIPPTags;
import dev.waldq.mipp.blocks.machine.MIPPMachines;

import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.swedz.tesseract.neoforge.datagen.mi.MIDatagenHooks;
import net.swedz.tesseract.neoforge.lang.LangInstance;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

import java.util.Map;
import java.util.Set;

public final class LanguageDatagenProvider extends LanguageProvider
{
	private static final Set<LangInstance<?>> INSTANCES = Sets.newHashSet();
	
	public static void include(LangInstance<?> instance) {
		INSTANCES.add(instance);
	}
	
	public LanguageDatagenProvider(GatherDataEvent event) {
		super(event.getGenerator().getPackOutput(), MIPP.ID, "en_us");
	}
	
	@Override
	protected void addTranslations()
	{
		for(var instance : INSTANCES) {
			instance.datagen(this);
		}
		
		for(ItemHolder item : MIPPItems.values())
		{
			this.add(item.asItem(), item.identifier().englishName());
		}
		
//		for(FluidHolder fluid : MIPPFluids.values())
//		{
//			this.add(fluid.block().get(), fluid.identifier().englishName());
//		}

		MIPPMachines.RecipeTypes.getRecipeTypeNames().forEach((recipeType, englishName) -> {
			this.add("rei_categories.%s.%s".formatted(MIPP.ID, recipeType.getPath()), englishName);
		});

		MIPPTags.translations().forEach(this::add);

        Map<String, String> veins = Map.ofEntries(
				Map.entry("text.mipp.veins.mipp.bauxite", "Bauxite Vein"),
				Map.entry("text.mipp.veins.mipp.coal", "Coal Vein"),
				Map.entry("text.mipp.veins.mipp.copper", "Copper Vein"),
				Map.entry("text.mipp.veins.mipp.diamond", "Diamond Vein"),
				Map.entry("text.mipp.veins.mipp.emerald", "Emerald Vein"),
				Map.entry("text.mipp.veins.mipp.gold", "Gold Vein"),
				Map.entry("text.mipp.veins.mipp.gold_nether", "Nether Gold Vein"),
				Map.entry("text.mipp.veins.mipp.iron", "Iron Vein"),
				Map.entry("text.mipp.veins.mipp.lapis", "Lapis Vein"),
				Map.entry("text.mipp.veins.mipp.lead", "Lead Vein"),
				Map.entry("text.mipp.veins.mipp.platinum", "Platinum Vein"),
				Map.entry("text.mipp.veins.mipp.quartz", "Quartz Vein"),
				Map.entry("text.mipp.veins.mipp.quartz_nether", "Nether Quartz Vein"),
				Map.entry("text.mipp.veins.mipp.redstone", "Redstone Vein"),
				Map.entry("text.mipp.veins.mipp.salt", "Salt Vein"),
				Map.entry("text.mipp.veins.mipp.tin", "Tin Vein"),
				Map.entry("text.mipp.veins.mipp.titanium", "Titanium Vein"),
				Map.entry("text.mipp.veins.mipp.uranium", "Uranium Vein")
		);

		veins.forEach(this::add);
		
		MIDatagenHooks.Client.withLanguageHook(this, MIPP.ID);
		
		this.add("itemGroup.%s.%s".formatted(MIPP.ID, MIPP.ID), MIPP.NAME);
	}
}
