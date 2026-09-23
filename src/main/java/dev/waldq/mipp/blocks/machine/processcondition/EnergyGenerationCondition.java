package dev.waldq.mipp.blocks.machine.processcondition;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.util.MIExtraCodecs;
import aztech.modern_industrialization.util.TextHelper;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPItems;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EnergyGenerationCondition(long amount) implements MachineProcessCondition {

    public static final MapCodec<EnergyGenerationCondition> CODEC = RecordCodecBuilder.mapCodec(
            (g) -> g.group(
                    MIExtraCodecs.POSITIVE_LONG.fieldOf("amount").forGetter(EnergyGenerationCondition::amount)
            ).apply(g, EnergyGenerationCondition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyGenerationCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            EnergyGenerationCondition::amount,
            EnergyGenerationCondition::new
    );

    @Override
    public boolean canProcessRecipe(Context context, MachineRecipe recipe) {
        var blockEntity = context.getBlockEntity();
        long remainingSpace = getRemainingEnergySpace(blockEntity);

        return remainingSpace >= amount;
    }

    private long getRemainingEnergySpace(Object blockEntity) {
        long remainingSpace = 0L;

        if (blockEntity instanceof EnergyListComponentHolder multiBlock) {
            for (var component : multiBlock.getEnergyComponents()) {
                remainingSpace += component.getCapacity() - component.getEu();
            }
        }

        if (blockEntity instanceof EnergyComponentHolder singleBlock) {
            var component = singleBlock.getEnergyComponent();
            if (component != null) {
                remainingSpace += component.getCapacity() - component.getEu();
            }
        }

        return remainingSpace;
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
        var energyAmount = TextHelper.getAmount(amount);
        lines.add(MIPP.text().energyGenerationTooltip(MIText.Eu.text(energyAmount.digit(), energyAmount.unit())));
    }

    @Override
    public ItemStack icon() {return new ItemStack(MIPPItems.ENERGY_ZAP);}

}
