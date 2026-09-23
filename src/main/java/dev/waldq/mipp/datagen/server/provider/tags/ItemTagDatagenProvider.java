package dev.waldq.mipp.datagen.server.provider.tags;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.MIPPTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public final class ItemTagDatagenProvider extends ItemTagsProvider {
	public ItemTagDatagenProvider(
			PackOutput output,
			CompletableFuture<HolderLookup.Provider> lookupProvider,
			CompletableFuture<TagLookup<Block>> blockTags,
			ExistingFileHelper existingFileHelper
	) {
		super(output, lookupProvider, blockTags, MIPP.MODID, existingFileHelper);
	}


	
	@Override
	protected void addTags(HolderLookup.Provider provider) {
		for(ItemHolder<?> item : MIPPItems.values().stream().sorted(Comparator.comparing((item) -> item.identifier().id())).toList()) {
			for(TagKey<Item> tag : item.tags()) {
				this.tag(tag).add(item.asItem());
			}
		}

		tag(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS)
				.add(MIPPItems.ENERGY_ZAP.get());

		this.copy(MIPPTags.Blocks.CASINGS, MIPPTags.Items.CASINGS);
		this.copy(MIPPTags.Blocks.CASINGS_BASE, MIPPTags.Items.CASINGS_BASE);
		this.copy(MIPPTags.Blocks.CASINGS_PIPE, MIPPTags.Items.CASINGS_PIPE);
		this.copy(MIPPTags.Blocks.CASINGS_SPECIAL, MIPPTags.Items.CASINGS_SPECIAL);
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
