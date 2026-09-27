package dev.waldq.mipp.blocks.machine.processcondition;


import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.worldgen.veins.OreVeinConfig;
import dev.waldq.mipp.worldgen.veins.OreVeinConfigLoader;
import dev.waldq.mipp.worldgen.veins.VeinGenHelpers;
import dev.waldq.mipp.worldgen.veins.VeinPlacerHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record OreVeinInChunkCondition(ResourceLocation veinId) implements MachineProcessCondition {

    public static final MapCodec<OreVeinInChunkCondition> CODEC = RecordCodecBuilder.mapCodec(
            (g) -> g.group(
                    ResourceLocation.CODEC.fieldOf("veinId").forGetter(OreVeinInChunkCondition::veinId)
            ).apply(g, OreVeinInChunkCondition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OreVeinInChunkCondition> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.cast(),
            OreVeinInChunkCondition::veinId,
            OreVeinInChunkCondition::new
    );

    @Override
    public boolean canProcessRecipe(MachineProcessCondition.Context context, MachineRecipe recipe) {
        var blockEntity = context.getBlockEntity();

        ServerLevel level = context.getLevel();
        ResourceLocation dim = level.dimension().location();
        long seed = level.getSeed();
        BlockPos pos = blockEntity.getBlockPos();
        ChunkPos cPos = new ChunkPos(pos);


        RandomSource random = VeinPlacerHelper.seededXoroshiroRandomSource(
                seed,
                cPos.x,
                cPos.z,
                dim
        );

        BlockPos center = new BlockPos(pos.getX(), level.getSeaLevel(), pos.getZ());

        Holder<Biome> biome = level.getBiome(center);

        List<OreVeinConfig> filteredVeins = OreVeinConfigLoader.getVeinsForBiome(biome, dim);

        OreVeinConfig vein = VeinGenHelpers.selectVein(filteredVeins, random);
        if (vein == null) return false;

        return vein.id().equals(veinId) && vein.bounds().maxY() <= pos.getY();
    }

    @Override
    public MapCodec<? extends MachineProcessCondition> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends MachineProcessCondition> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void appendDescription(List<Component> lines) {
        lines.add(MIPP.text().veinUnderneathTooltip(Component.translatable("text.mipp.veins.%s".formatted(veinId))));
    }

    @Override
    public ItemStack icon() {return new ItemStack(MIPPItems.ELECTRIC_PROSPECTOR);}

}
