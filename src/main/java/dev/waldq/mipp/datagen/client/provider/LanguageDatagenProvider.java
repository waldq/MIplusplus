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
		super(event.getGenerator().getPackOutput(), MIPP.MODID, "en_us");
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
			this.add("rei_categories.%s.%s".formatted(MIPP.MODID, recipeType.getPath()), englishName);
		});

		MIPPTags.translations().forEach(this::add);

        Map<String, String> veins = Map.ofEntries(
				Map.entry("text.mipp.veins.bauxite", "Bauxite Vein"),
				Map.entry("text.mipp.veins.coal", "Coal Vein"),
				Map.entry("text.mipp.veins.copper", "Copper Vein"),
				Map.entry("text.mipp.veins.diamond", "Diamond Vein"),
				Map.entry("text.mipp.veins.emerald", "Emerald Vein"),
				Map.entry("text.mipp.veins.gold", "Gold Vein"),
				Map.entry("text.mipp.veins.gold_nether", "Nether Gold Vein"),
				Map.entry("text.mipp.veins.iron", "Iron Vein"),
				Map.entry("text.mipp.veins.lapis", "Lapis Vein"),
				Map.entry("text.mipp.veins.lead", "Lead Vein"),
				Map.entry("text.mipp.veins.platinum", "Platinum Vein"),
				Map.entry("text.mipp.veins.quartz", "Quartz Vein"),
				Map.entry("text.mipp.veins.quartz_nether", "Nether Quartz Vein"),
				Map.entry("text.mipp.veins.redstone", "Redstone Vein"),
				Map.entry("text.mipp.veins.salt", "Salt Vein"),
				Map.entry("text.mipp.veins.tin", "Tin Vein"),
				Map.entry("text.mipp.veins.titanium", "Titanium Vein"),
				Map.entry("text.mipp.veins.uranium", "Uranium Vein")
		);

		veins.forEach(this::add);
		
		MIDatagenHooks.Client.withLanguageHook(this, MIPP.MODID);
		
		this.add("itemGroup.%s.%s".formatted(MIPP.MODID, MIPP.MODID), MIPP.NAME);
	}
}
