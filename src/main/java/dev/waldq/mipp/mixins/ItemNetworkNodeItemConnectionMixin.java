package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;

import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;

import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "aztech.modern_industrialization.pipes.item.ItemNetworkNode$ItemConnection", remap = false)
public class ItemNetworkNodeItemConnectionMixin implements ItemNetworkNodeItemConnectionAccessor {

    @Final
    @Shadow
    Direction direction;

    @Unique int mipp$lastInserted;
    @Unique int mipp$lastExtracted;

    @Override
    public Direction mipp$getDirection() {
        return this.direction;
    }

    @Override
    public int mipp$getExtracted() { return this.mipp$lastExtracted; }

    @Override
    public void mipp$addExtracted(int amount) { this.mipp$lastExtracted += amount; }

    @Override
    public int mipp$getInserted() { return this.mipp$lastInserted; }

    @Override
    public void mipp$addInserted(int amount) { this.mipp$lastInserted += amount; }

    @Override
    public void mipp$reset() {
        this.mipp$lastInserted = 0;
        this.mipp$lastExtracted = 0;
    }
}
