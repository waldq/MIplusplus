package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeAccessor;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ItemNetworkNode.class)
public abstract class ItemNetworkNodeMixin implements ItemNetworkNodeAccessor {

    @Final
    @Shadow
    List<?> connections;

    @Override
    public List<?> mipp$getConnections() { return this.connections; }

    @Override
    public ItemNetworkNodeItemConnectionAccessor[] mipp$getItemConnections(BlockPos pos) {
        ItemNetworkNodeItemConnectionAccessor[] connections = new ItemNetworkNodeItemConnectionAccessor[6];
        for (Object connection : this.connections) {
            ItemNetworkNodeItemConnectionAccessor conn = (ItemNetworkNodeItemConnectionAccessor) connection;
            connections[conn.mipp$getDirection().get3DDataValue()] = conn;
        }
        return connections;
    }
}
