package dev.waldq.mipp.datagen.client.provider.models;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPBlocks;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;

public final class BlockModelsDatagenProvider extends BlockStateProvider {
	public BlockModelsDatagenProvider(GatherDataEvent event) {
		super(event.getGenerator().getPackOutput(), MIPP.MODID, event.getExistingFileHelper());
	}
	
	@Override
	protected void registerStatesAndModels() {
		for(BlockHolder<?> block : MIPPBlocks.values()) {
			if(block.hasModelProvider()) {
				block.modelProvider().accept(this);
			}
		}
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
