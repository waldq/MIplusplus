package dev.waldq.mipp.mixins;

import brachy.modularui.network.ModularNetworkSide;
import brachy.modularui.network.packets.SyncHandlerPacket;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ModularNetworkSide.class, remap = false)
public abstract class ModularNetworkSideMixin {
    @WrapMethod(method = "receivePacket")
    private void mipp$releasePacket(Player player, SyncHandlerPacket packet, Operation<Void> original) {
        try {
            original.call(player, packet);
        } finally {
            if (packet.packet() != null) packet.packet().release();
        }
    }
}
