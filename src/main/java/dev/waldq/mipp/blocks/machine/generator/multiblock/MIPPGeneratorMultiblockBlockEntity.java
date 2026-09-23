package dev.waldq.mipp.blocks.machine.generator.multiblock;

import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.RedstoneControlComponent;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Simulation;

import dev.waldq.mipp.blocks.machine.processcondition.EnergyGenerationCondition;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;

import net.swedz.tesseract.neoforge.compat.mi.mixin.accessor.CrafterComponentAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class MIPPGeneratorMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity implements CrafterComponent.Behavior, EnergyListComponentHolder {
    private final List<EnergyComponent> energyOutputs = new ArrayList<>();
    private final RedstoneControlComponent redstoneControl = new RedstoneControlComponent();

    public MIPPGeneratorMultiblockBlockEntity(BEP bep, ResourceLocation id, ShapeTemplate[] shape) {
        super(bep, id, new OrientationComponent.Params(false, false, false), shape);

        this.registerComponents(redstoneControl);
        this.registerGuiComponent(
                new SlotPanel(this)
                .withRedstoneControl(redstoneControl)
        );
        this.registerGuiComponent(new CraftingMultiblockGui(
                () -> shapeValid.shapeValid,
                crafter::getProgress,
                this.getCrafterComponent(),
                () -> 0
        ));

    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        ItemInteractionResult result = super.useItemOn(player, hand, face);
        if (!result.consumesAction()) {
            result = redstoneControl.onUse(this, player, hand);
        }
        return result;
    }

    @Override
    public void onCraft() {
        var activeRecipe = ((CrafterComponentAccessor) this.crafter).getActiveRecipe();
        if (activeRecipe == null) return;

        activeRecipe.value().conditions.stream()
                .filter(EnergyGenerationCondition.class::isInstance)
                .map(EnergyGenerationCondition.class::cast)
                .findFirst()
                .ifPresent(condition ->{
                    boolean inserted = insertEnergy(condition.amount(), true) > 0;
                    if (inserted) {
                        insertEnergy(condition.amount(), false);
                    }
                    onInsert(inserted);
                });
    }

    public void onInsert(boolean hasInsertedEnergy) {
    }

    public long insertEnergy(long value, boolean simulate) {
        long rem = value;
        long inserted = 0L;
        Simulation sim = (simulate) ? Simulation.SIMULATE : Simulation.ACT;
        for (EnergyComponent e : energyOutputs) {
            if (rem >  0) {
                inserted += e.insertEu(rem, sim);
                rem -= inserted;
            }
        }
        return inserted;
    }

    @Override
    public final void onRematch(ShapeMatcher shapeMatcher) {
        super.onRematch(shapeMatcher);
        if (shapeMatcher.isMatchSuccessful()) {
            energyOutputs.clear();
            shapeMatcher.getMatchedHatches().forEach(hatch -> {
                hatch.appendEnergyOutputs(energyOutputs);
                }
            );
        } else {
            onFailedRematch(shapeMatcher);
        }
    }

    public void onSuccessfulRematch(ShapeMatcher shapeMatcher) {}

    public void onFailedRematch(ShapeMatcher shapeMatcher) {}

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return max;
    }

    @Override
    public long getBaseRecipeEu() { return 0L; }

    @Override
    public long getMaxRecipeEu() { return 1L; }

    @Override
    public ServerLevel getCrafterWorld() { return level instanceof ServerLevel serverLevel ? serverLevel : null; }

    @Override
    public UUID getOwnerUuid() { return placedBy.placerId; }

    @Override
    public CrafterComponent.Behavior getBehavior() { return this; }

    @Override
    public List<EnergyComponent> getEnergyComponents() { return energyOutputs; }

    @Override
    public boolean isEnabled() { return redstoneControl.doAllowNormalOperation(this); }


}
