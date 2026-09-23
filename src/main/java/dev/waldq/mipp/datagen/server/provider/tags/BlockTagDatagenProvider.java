package dev.waldq.mipp.datagen.server.provider.tags;


import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPBlocks;
import dev.waldq.mipp.MIPPTags;
import dev.waldq.mipp.utils.ColorUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;


public final class BlockTagDatagenProvider extends BlockTagsProvider {
	public BlockTagDatagenProvider(
			PackOutput output,
			CompletableFuture<HolderLookup.Provider> lookupProvider,
			ExistingFileHelper existingFileHelper
	) {
		super(output, lookupProvider, MIPP.MODID, existingFileHelper);
	}

	private void addSupportsSampleTags() {
		this.tag(MIPPTags.Blocks.SUPPORTS_SAMPLE)
				.add(
						Blocks.BASALT,
						Blocks.CRIMSON_NYLIUM,
						Blocks.GRAVEL,
						Blocks.SOUL_SAND,
						Blocks.SOUL_SOIL,
						Blocks.WARPED_NYLIUM
				)
				.addTag(BlockTags.DIRT)
				.addTag(BlockTags.SAND)
				.addTag(MIPPTags.Blocks.STONES)
				.addTag(BlockTags.TERRACOTTA);

		this.tag(MIPPTags.Blocks.STONES)
				.add(Blocks.STONE)
				.add(Blocks.ANDESITE)
				.add(Blocks.GRANITE)
				.add(Blocks.DIORITE)
				.add(Blocks.TUFF);

		this.tag(MIPPTags.Blocks.NETHER_REPLACEABLE)
				.add(Blocks.NETHERRACK)
				.add(Blocks.BASALT)
				.add(Blocks.BLACKSTONE)
				.add(Blocks.MAGMA_BLOCK);
	}

	private void addCasingsTags() {
		for (ColorUtils.ColorEntry color : ColorUtils.COLORS) {
			String id = color.id();
			var casingBase = MIPPBlocks.COLORED_CASINGS.get(id).get();
			var casingPipe = MIPPBlocks.COLORED_CASINGS_PIPE.get(id).get();
			var casingSpecial = MIPPBlocks.COLORED_SPECIAL_CASINGS.get(id).get();

			this.tag(MIPPTags.Blocks.CASINGS_BASE)
					.add(casingBase);

			this.tag(MIPPTags.Blocks.CASINGS_PIPE)
					.add(casingPipe);

			this.tag(MIPPTags.Blocks.CASINGS_SPECIAL)
					.add(casingSpecial);

			var colorTag = MIPPTags.Blocks.casingColor(id);
			this.tag(colorTag)
					.add(
							casingBase,
							casingPipe,
							casingSpecial
					);
			this.tag(MIPPTags.Blocks.CASINGS_COLOR)
					.addTag(colorTag);
		}

		this.tag(MIPPTags.Blocks.CASINGS_BASE)
				.add(MIPPBlocks.BASIC_CASING.get());

		this.tag(MIPPTags.Blocks.CASINGS_PIPE)
				.add(MIPPBlocks.BASIC_CASING_PIPE.get());

		this.tag(MIPPTags.Blocks.CASINGS_SPECIAL)
				.add(MIPPBlocks.BASIC_SPECIAL_CASING.get());

		var basic = MIPPTags.Blocks.casingColor("none");

		this.tag(basic)
				.add(
						MIPPBlocks.BASIC_CASING.get(),
						MIPPBlocks.BASIC_CASING_PIPE.get(),
						MIPPBlocks.BASIC_SPECIAL_CASING.get()
				);

		this.tag(MIPPTags.Blocks.CASINGS)
				.addTag(MIPPTags.Blocks.CASINGS_BASE)
				.addTag(MIPPTags.Blocks.CASINGS_PIPE)
				.addTag(MIPPTags.Blocks.CASINGS_SPECIAL);
	}


	
	@Override
	protected void addTags(HolderLookup.Provider registries) {
		for(BlockHolder<?> block : MIPPBlocks.values().stream().sorted(Comparator.comparing((block) -> block.identifier().id())).toList()) {
			for(var tag : block.tags()) {
				this.tag(tag).add(block.get());
			}
		}
		this.addSupportsSampleTags();
		this.addCasingsTags();
	}
	
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
