package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CasingComponent;

import dev.waldq.mipp.item.debugjournal.GlobalJournal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CasingComponent.class, remap = false)
public class CasingComponentMixin {
    @Shadow
    private CableTier currentTier;

    @Inject(
            method = "onUse",
            at = @At("RETURN"),
            remap = false
    )
    private void mipp$onCasingChanged(
            MachineBlockEntity be,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        var level = be.getLevel();
        if (level == null || level.isClientSide()) return;
        if (!cir.getReturnValue().consumesAction()) return;

        var blocks = GlobalJournal.JOURNAL.get(level.dimension());
        if (blocks == null || blocks.isEmpty()) return;

        var entry = blocks.get(be.getBlockPos().asLong());
        if (entry == null || !entry.isValid()) return;

        entry.setCableTier(currentTier);
    }
}
