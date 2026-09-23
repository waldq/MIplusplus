package dev.waldq.mipp.blocks.machine.blockentities;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.component.EnergyAccess;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;

import dev.waldq.mipp.MIPPAttachments;
import dev.waldq.mipp.MIPPConfig;
import dev.waldq.mipp.blocks.machine.utils.CableTierUtils;
import dev.waldq.mipp.blocks.machine.components.ElectricChunkLoaderComponent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.UUID;

import static dev.waldq.mipp.MIPPAttachments.OWNER_UUID;


public class ElectricChunkLoaderBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {
    private final MIInventory inventory;
    private final EnergyComponent energy;
    private final MIEnergyStorage insertable;
    private final CableTier cableTier;

    private boolean isEnabled = false;
    private int timer = 100;
    private final int chunkRadius;

    private final ElectricChunkLoaderComponent loader = new ElectricChunkLoaderComponent();

    private UUID ownerUUID;

    public void setOwnerUUID(UUID ownerUUID) {
        if (ownerUUID != null) {
            this.setData(OWNER_UUID, ownerUUID);
            this.setChanged();
        }
    }

    public UUID getOwnerUUID() {
        return this.getData(OWNER_UUID);
    }

    public ElectricChunkLoaderBlockEntity(
            BEP bep,
            ResourceLocation blockId,
            CableTier cableTier
    ) {
        super(bep,
                new MachineGuiParameters.Builder(blockId, true).backgroundHeight(180).build(),
                new OrientationComponent.Params(false, false, false));

        this.inventory = new MIInventory(
                List.of(), List.of(),
                new SlotPositions.Builder().build(), new SlotPositions.Builder().build());
        this.energy = new EnergyComponent(this, cableTier.getEu() * 100);
        this.cableTier = cableTier;
        this.insertable = energy.buildInsertable(otherCableTier -> otherCableTier == cableTier);
        registerGuiComponent(new EnergyBar(new EnergyBar.Params(87,37), energy::getEu, energy::getCapacity));
        this.registerComponents(energy, loader);
        this.chunkRadius = CableTierUtils.CableNumbers.get(cableTier).chunkRadius();

    }

    public boolean getIsEnabled() { return isEnabled; }

    @Override
    public void tick() {
        if (level == null || level.isClientSide)
            return;

        if (energy.getEu() >= CableTierUtils.CableNumbers.get(cableTier).cost()) {
            energy.consumeEu(CableTierUtils.CableNumbers.get(cableTier).cost(), Simulation.ACT);
            timer = 100;
            if (!isEnabled) {
                isEnabled = true;
                loader.setChunks(level, this.getBlockPos(), CableTierUtils.CableNumbers.get(cableTier).chunkRadius(), true);
            }

        } else {
            shouldUnloadChunk();
        }
    }

    @Override
    public void setRemoved() {
        if (isEnabled && level != null && !level.isClientSide) {
            loader.setChunks(level, this.getBlockPos(), this.chunkRadius, false);
            isEnabled = false;
        }
        super.setRemoved();
    }

    private void shouldUnloadChunk() {
        if (!isEnabled) return;
        if (timer == 0) {
            timer = 100;
            isEnabled = false;
            loader.setChunks(level, this.getBlockPos(), this.chunkRadius, false);
        } else {
            --timer;
        }
    }

    public int getChunkAmount() {
        return CableTierUtils.CableNumbers.get(cableTier).chunkAmount();
    }

    @Override
    public MIInventory getInventory() { return inventory; }

    @Override
    public MachineModelClientData getMachineModelData() {
        return new MachineModelClientData(cableTier.casing);
    }

    @Override
    public EnergyAccess getEnergyComponent() { return energy; }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> ((ElectricChunkLoaderBlockEntity) be).insertable);
        });
    }
}
