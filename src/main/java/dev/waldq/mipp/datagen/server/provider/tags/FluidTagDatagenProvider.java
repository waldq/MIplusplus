//package dev.waldq.mipp.datagen.server.provider.tags;
//
//import helper.minecraft.core.HolderLookup;
//import helper.minecraft.data.tags.FluidTagsProvider;
//import helper.minecraft.tags.TagKey;
//import helper.minecraft.world.level.material.Fluid;
//import helper.neoforged.neoforge.data.event.GatherDataEvent;
//
//import helper.swedz.tesseract.neoforge.registry.holder.FluidHolder;
//
//import java.util.Comparator;
//
//public final class FluidTagDatagenProvider extends FluidTagsProvider
//{
//	public FluidTagDatagenProvider(GatherDataEvent event)
//	{
//		super(event.getGenerator().getPackOutput(), event.getLookupProvider(), MIPP.ID, event.getExistingFileHelper());
//	}
//
//	@Override
//	protected void addTags(HolderLookup.Provider provider)
//	{
//		for(FluidHolder<?, ?, ?, ?> fluid : MIPPFluids.values().stream().sorted(Comparator.comparing((fluid) -> fluid.identifier().id())).toList())
//		{
//			for(TagKey<Fluid> tag : fluid.tags())
//			{
//				this.tag(tag).add(fluid.get());
//			}
//		}
//	}
//
//	@Override
//	public String getName()
//	{
//		return this.getClass().getSimpleName();
//	}
//}
