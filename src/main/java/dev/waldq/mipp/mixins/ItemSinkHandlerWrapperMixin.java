package dev.waldq.mipp.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.waldq.mipp.item.analyzer.helper.InsertTracker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "aztech.modern_industrialization.pipes.item.ItemSink$HandlerWrapper", remap = false)
public abstract class ItemSinkHandlerWrapperMixin {
    @Shadow public abstract BlockPos pipePos();
    @Shadow public abstract Direction direction();

    @ModifyReturnValue(method = "moveAll", at = @At("RETURN"))
    private int mipp$countInserted(int moved) {
        if (moved > 0) InsertTracker.addInserted(pipePos(), direction(), moved);
        return moved;
    }
}
