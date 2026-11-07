package dev.waldq.mipp.blocks.machine.generator;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierHolder;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.AbstractCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.helper.EnergyHelper;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;

import dev.waldq.mipp.blocks.machine.processcondition.EnergyGenerationCondition;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.fluids.FluidType;

import net.swedz.tesseract.neoforge.compat.mi.mixin.accessor.CrafterComponentAccessor;



import java.util.ArrayList;
import java.util.List;


public class GeneratorRecipeMachineBlockEntity extends AbstractCraftingMachineBlockEntity implements EnergyComponentHolder, CableTierHolder {
    public record InventoryLayout(
            int screenHeight,
            int itemInputCount, int itemOutputCount,
            int fluidInputCount, int fluidOutputCount,
            SlotPositions itemPositions,
            SlotPositions fluidPositions,
            int energyBarX, int energyBarY,
            int progressBarX, int progressBarY
    ) {}

    private static MachineInventoryComponent build(InventoryLayout inventoryLayout) {
        long capacity = 16 * FluidType.BUCKET_VOLUME;

        List<ConfigurableItemStack> itemInputStacks = new ArrayList<>();
        for (int i = 0; i < inventoryLayout.itemInputCount(); ++i) {
            itemInputStacks.add(ConfigurableItemStack.standardInputSlot());
        }

        List<ConfigurableItemStack> itemOutputStacks = new ArrayList<>();
        for (int i = 0; i < inventoryLayout.itemOutputCount(); ++i) {
            itemOutputStacks.add(ConfigurableItemStack.standardOutputSlot());
        }


        List<ConfigurableFluidStack> fluidInputStacks = new ArrayList<>();
        for (int i = 0; i < inventoryLayout.fluidInputCount(); ++i) {
            fluidInputStacks.add(ConfigurableFluidStack.standardInputSlot(capacity));
        }

        List<ConfigurableFluidStack> fluidOutputStacks = new ArrayList<>();
        for (int i = 0; i < inventoryLayout.fluidOutputCount(); ++i) {
            fluidOutputStacks.add(ConfigurableFluidStack.standardOutputSlot(capacity));
        }


        return new MachineInventoryComponent(
                itemInputStacks, itemOutputStacks,
                fluidInputStacks, fluidOutputStacks,
                inventoryLayout.itemPositions(), inventoryLayout.fluidPositions()
        );
    }

    public GeneratorRecipeMachineBlockEntity(
            BEP bep,
            ResourceLocation blockId,
            CableTier tier,
            MachineRecipeType machineRecipeType,
            InventoryLayout inventoryLayout
    ) {
        this(bep, blockId, tier,
                machineRecipeType, inventoryLayout, build(inventoryLayout),
                new EnergyBar.Params(inventoryLayout.energyBarX, inventoryLayout.energyBarY),
                MachineTier.UNLIMITED
        );
    }

    private GeneratorRecipeMachineBlockEntity(

            BEP bep,
            ResourceLocation blockId,
            CableTier cableTier,
            MachineRecipeType recipeType,
            InventoryLayout inventoryLayout,
            MachineInventoryComponent inventory,
            EnergyBar.Params energyBarParams,
            MachineTier tier
            ) {
        super(
                bep,
                recipeType,
                inventory,
                new MachineGuiParameters.Builder(blockId, true).backgroundHeight(inventoryLayout.screenHeight).build(),
                new ProgressBar.Params(inventoryLayout.progressBarX, inventoryLayout.progressBarY, "yai_charge"),
                tier);
        this.redstoneControl = new RedstoneControlComponent();
        this.casing = cableTier.casing;
        this.energy = new EnergyComponent(this, cableTier.getEu() * 100);
        this.extractable = energy.buildExtractable(otherCableTier -> otherCableTier == cableTier);
        this.cableTier = cableTier;
        registerGuiComponent(new EnergyBar(energyBarParams, energy::getEu, energy::getCapacity));
//        registerGuiComponent(new RecipeEfficiencyBar(efficiencyBarParams, crafter));
        registerGuiComponent(new SlotPanel(this)
                .withRedstoneControl(redstoneControl));
        this.registerComponents(redstoneControl, energy);
    }

    private final RedstoneControlComponent redstoneControl;
    private final MachineCasing casing;
    private final EnergyComponent energy;
    private final MIEnergyStorage extractable;
    private final CableTier cableTier;

    @Override
    public boolean isEnabled() {
        return redstoneControl.doAllowNormalOperation(this) && energy.getEu() < energy.getCapacity();
    }

    @Override
    public long consumeEu(long max, Simulation simulation) { return max; }

    @Override
    public MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(casing);
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide)
            return;

        super.tick();

        if (!redstoneControl.doAllowNormalOperation(this)) {
            this.isActiveComponent.updateActive(false, this);
        } else {
            this.isActiveComponent.updateActive(this.crafter.hasActiveRecipe(), this);
        }
        EnergyHelper.autoOutput(this, this.orientation, cableTier, extractable);
        this.setChanged();
    }

    @Override
    public void onCraft() {
        var activeRecipe = ((CrafterComponentAccessor) this.crafter).getActiveRecipe();

        activeRecipe.value().conditions.stream()
                .filter(EnergyGenerationCondition.class::isInstance)
                .map(EnergyGenerationCondition.class::cast)
                .findFirst()
                .ifPresent(condition -> {
                    this.energy.insertEu(condition.amount(), Simulation.ACT);
                });
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        var result = super.useItemOn(player, hand, face);
        if (!result.consumesAction()) {
            result = redstoneControl.onUse(this, player, hand);
        }
        return result;
    }

    @Override
    public long getMaxRecipeEu() {
        return  1L;
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    public CableTier getCableTier() {
        return cableTier;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) ->
                    direction == ((GeneratorRecipeMachineBlockEntity) be).orientation.outputDirection ? ((GeneratorRecipeMachineBlockEntity) be).extractable : null);
        });
    }
}
