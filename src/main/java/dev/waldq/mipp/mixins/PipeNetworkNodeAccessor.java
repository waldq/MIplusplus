package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PipeNetworkNode.class)
public interface PipeNetworkNodeAccessor {
    @Accessor(remap = false)
    PipeNetwork getNetwork();
}
