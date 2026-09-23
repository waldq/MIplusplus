package dev.waldq.mipp.datagen.client.provider.models;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPItems;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

public final class ItemModelsDatagenProvider extends ItemModelProvider {
	public ItemModelsDatagenProvider(GatherDataEvent event) {
		super(event.getGenerator().getPackOutput(), MIPP.MODID, event.getExistingFileHelper());
	}
	
	@Override
	protected void registerModels() {
		for(ItemHolder item : MIPPItems.values()) {
			if(item.hasModelProvider()) {
				item.modelProvider().accept(this);
			}
		}
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
