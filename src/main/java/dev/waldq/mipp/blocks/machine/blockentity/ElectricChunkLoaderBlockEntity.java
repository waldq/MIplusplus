package dev.waldq.mipp.blocks.machine.blockentity;

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
import dev.waldq.mipp.blocks.machine.component.ChunkLoaderComponent;
import dev.waldq.mipp.blocks.machine.component.ElectricChunkLoaderComponent;
import dev.waldq.mipp.blocks.machine.utils.CableTierUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;


public class ElectricChunkLoaderBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {
    private final MIInventory inventory;
    private final EnergyComponent energy;
    private final MIEnergyStorage insertable;
    private final CableTier cableTier;

    private final long energyCost;
    private final int chunkRadius;
    private final int chunkAmount;

    private final ElectricChunkLoaderComponent loader = new ElectricChunkLoaderComponent();

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

        var data = CableTierUtils.CableNumbers.get(cableTier);

        this.energyCost = data.cost();
        this.chunkRadius = data.chunkRadius();
        this.chunkAmount = data.chunkAmount();
    }

    @Override
    public void tick() {
        if (level != null && level instanceof ServerLevel serverLevel) {
            if (placedBy.placerId == null) {
                if (loader.isEnabled) {
                    loader.isEnabled = false;
                    ChunkLoaderComponent.setChunks(serverLevel, this.getBlockPos(), chunkRadius, false);
                    setChanged();
                }
                return;
            }

            if (energy.getEu() >= energyCost) {
                energy.consumeEu(energyCost, Simulation.ACT);
                loader.resetTime();
                if (!loader.isEnabled) {
                    loader.isEnabled = true;
                    ChunkLoaderComponent.setChunks(serverLevel, this.getBlockPos(), chunkRadius, true);
                }

            } else {
                shouldUnloadChunk(serverLevel);
            }
        }
    }

    private void shouldUnloadChunk(ServerLevel level) {
        if (!loader.isEnabled) return;
        if (loader.getTime() == 0) {
            loader.resetTime();
            loader.isEnabled = false;
            setChanged();
            ChunkLoaderComponent.setChunks(level, this.getBlockPos(), this.chunkRadius, false);
        } else {
            loader.decreaseTime();
        }
    }

    public CableTier getCableTier() { return cableTier; }

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
