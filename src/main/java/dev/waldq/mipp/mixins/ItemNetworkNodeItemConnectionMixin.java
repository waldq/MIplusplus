package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
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

    @Shadow
    PipeEndpointType type;

    @Unique int lastInserted;
    @Unique int lastExtracted;

    @Override
    public Direction mipp$getDirection() {
        return this.direction;
    }

    @Override
    public PipeEndpointType mipp$getPipeEndpointType() {
        return this.type;
    }

    @Override
    public int mipp$getExtracted() { return this.lastExtracted; }

    @Override
    public void mipp$addExtracted(int amount) { this.lastExtracted += amount; }

    @Override
    public int mipp$getInserted() { return this.lastInserted; }

    @Override
    public void mipp$addInserted(int amount) { this.lastInserted += amount; }

    @Override
    public void mipp$reset() {
        this.lastInserted = 0;
        this.lastExtracted = 0;
    }
}
