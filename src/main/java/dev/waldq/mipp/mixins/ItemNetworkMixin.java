package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.pipes.item.ItemNetwork;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeAccessor;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ItemNetwork.class)
public abstract class ItemNetworkMixin {

    @Inject(method = "doNetworkTransfer", at = @At("HEAD"))
    private void mipp$resetCounters(ServerLevel world, CallbackInfo ci) {
        ItemNetwork network = (ItemNetwork) (Object) this;
        for (var entry : network.iterateTickingNodes()) {
            if (entry.getNode() instanceof ItemNetworkNodeAccessor nodeAccessor) {
                for (Object connection : nodeAccessor.mipp$getConnections()) {
                    if (connection instanceof ItemNetworkNodeItemConnectionAccessor conn) {
                        conn.mipp$reset();
                    }
                }
            }
        }
    }

    @WrapOperation(
            method = "doNetworkTransfer",
            at = @At(value = "INVOKE",
                    target = "Laztech/modern_industrialization/pipes/item/ItemNetworkNode$ItemConnection;getMoves()I"),
            remap = false)
    private int mipp$captureExtractConn(@Coerce Object connection, Operation<Integer> original,
                                        @Share("extractConn") LocalRef<Object> ref) {
        ref.set(connection);
        return original.call(connection);
    }

    @WrapOperation(
            method = "doNetworkTransfer",
            at = @At(value = "INVOKE",
                    target = "Laztech/modern_industrialization/pipes/item/ItemNetwork;moveAll(Lnet/minecraft/server/level/ServerLevel;Laztech/modern_industrialization/pipes/item/ExtractionSource;Ljava/util/List;Ljava/util/function/Predicate;I)I"),
            remap = false)
    private int mipp$trackExtractedItems(ServerLevel world, @Coerce Object target, List<?> sinks,
                                         Predicate<?> filter, int maxToMove, Operation<Integer> original,
                                         @Share("extractConn") LocalRef<Object> ref) {
        int moved = original.call(world, target, sinks, filter, maxToMove);
        if (moved > 0 && ref.get() instanceof ItemNetworkNodeItemConnectionAccessor c) {
            c.mipp$addExtracted(moved);
        }
        return moved;
    }

    @WrapOperation(
            method = "insertTargets",
            at = @At(value = "INVOKE",
                    target = "Laztech/modern_industrialization/pipes/item/ItemNetworkNode$ItemConnection;canStackMoveThrough(Lnet/minecraft/world/item/ItemStack;)Z"),
            remap = false)
    private static boolean mipp$captureInsertConn(@Coerce Object connection, ItemStack stack,
                                                  Operation<Boolean> original,
                                                  @Share("insertConn") LocalRef<Object> ref) {
        ref.set(connection);
        return original.call(connection, stack);
    }

    @WrapOperation(
            method = "insertTargets",
            at = @At(value = "INVOKE",
                    target = "Laztech/modern_industrialization/pipes/item/ItemSink$HandlerWrapper;moveAll(Lnet/minecraft/server/level/ServerLevel;Laztech/modern_industrialization/pipes/item/ExtractionSource;II)I"),
            remap = false)
    private static int mipp$trackInsertedItems(@Coerce Object handlerWrapper, ServerLevel world,
                                               @Coerce Object source, int sourceSlot, int maxToMove,
                                               Operation<Integer> original,
                                               @Share("insertConn") LocalRef<Object> ref) {
        int moved = original.call(handlerWrapper, world, source, sourceSlot, maxToMove);
        if (moved > 0 && ref.get() instanceof ItemNetworkNodeItemConnectionAccessor c) {
            c.mipp$addInserted(moved);
        }
        return moved;
    }
}
