package dev.waldq.mipp.mixins;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.util.Simulation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.waldq.mipp.item.debugjournal.GlobalJournal;
import dev.waldq.mipp.item.debugjournal.JournalEntry;
import dev.waldq.mipp.item.debugjournal.MachineState;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrafterComponent.class, remap = false)
public class CrafterComponentMixin {
    @Shadow
    private boolean matchesMultipleRecipes;
    @Shadow
    @Final
    private CrafterComponent.Behavior behavior;
    @Shadow
    private long recipeMaxEu;

    @Unique
    private MachineBlockEntity mipp$machine;
    @Unique
    private JournalEntry mipp$entry;
    @Unique
    private boolean mipp$outputsBlocked;
    @Unique
    private boolean mipp$conditionsMatch;

    @Inject(
            method = "<init>",
            at = @At("RETURN"),
            remap = false
    )
    private void mipp$init(
            MachineBlockEntity blockEntity,
            CrafterComponent.Inventory inventory,
            CrafterComponent.Behavior behavior,
            CallbackInfo ci
    ) {
        this.mipp$machine = blockEntity;
    }

    @ModifyExpressionValue(
            method = "canStartRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/machines/components/CrafterComponent;putItemOutputs(Laztech/modern_industrialization/machines/recipe/MachineRecipe;ZZ)Z"
            ),
            remap = false
    )
    private boolean mipp$capturePutItemOutput(boolean original) {
        if (!original) mipp$outputsBlocked = true;
        return original;
    }

    @ModifyExpressionValue(
            method = "canStartRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/machines/components/CrafterComponent;putFluidOutputs(Laztech/modern_industrialization/machines/recipe/MachineRecipe;ZZ)Z"
            ),
            remap = false
    )
    private boolean mipp$capturePutFluidOutput(boolean original) {
        if (!original) mipp$outputsBlocked = true;
        return original;
    }

    @ModifyExpressionValue(
            method = {"canStartRecipe", "tickRecipe"},
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/machines/recipe/MachineRecipe;conditionsMatch(Laztech/modern_industrialization/machines/recipe/condition/MachineProcessCondition$Context;)Z"
            ),
            remap = false
    )
    private boolean mipp$captureConditionsMatch(boolean original) {
        mipp$conditionsMatch &= original;
        return original;
    }

    @ModifyExpressionValue(
            method = "updateActiveRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/machines/components/CrafterComponent;areAllOutputSlotsLocked()Z"
            ),
            remap = false
    )
    private boolean mipp$resetFlags(boolean original) {
        mipp$outputsBlocked = false;
        mipp$conditionsMatch = true;
        return original;
    }

    @ModifyReturnValue(
            method = "tickRecipe",
            at = @At(
                    value = "RETURN"
            ),
            remap = false
    )
    private boolean mipp$processFlags(
            boolean original,
            @Local(name = "eu") long eu,
            @Local(name = "finishedRecipe") boolean finishedRecipe
    ) {
        var level = mipp$machine.getLevel();
        if (level == null) return original;
        long currentTick = level.getGameTime();

        JournalEntry entry = mipp$entry;
        if (entry == null || !entry.isValid()) {
            var block = mipp$machine.getBlockState().getBlock();
            var byPos = GlobalJournal.JOURNAL.computeIfAbsent(level.dimension(), k -> new Long2ObjectOpenHashMap<>());
            var bp = mipp$machine.getBlockPos();
            long pos = bp.asLong();
            LongOpenHashSet set = GlobalJournal.CHUNK_BLOCKS.computeIfAbsent(
                    new GlobalJournal.GlobalLongChunkPos(level.dimension(), ChunkPos.asLong(bp)),
                    ck -> new LongOpenHashSet()
            );
            set.add(pos);
            entry = byPos.get(pos);

            if (entry == null || !entry.isValid() || entry.getMachine() != block) {
                CableTier cableTier = mipp$machine instanceof CableTierHolder holder
                        ? holder.getCableTier()
                        : null;
                entry = new JournalEntry(
                        mipp$machine.placedBy.placerId,
                        block,
                        cableTier,
                        false,
                        currentTick
                );
                byPos.put(pos, entry);
            }
            mipp$entry = entry;
        }
        entry.setOverdriving(behavior.isOverdriving());

        int state;

        if (original && eu < recipeMaxEu && !finishedRecipe) {
            state = MachineState.LOW_ENERGY.ordinal();
        } else if (original) {
            state = MachineState.WORKING.ordinal();
        } else if (!behavior.isEnabled()) {
            state = MachineState.REDSTONE.ordinal();
        } else if (matchesMultipleRecipes) {
            state = MachineState.MULTIPLE_RECIPES.ordinal();
        } else if (behavior.consumeEu(1, Simulation.SIMULATE) < 1) {
            state = MachineState.NO_ENERGY.ordinal();
        } else if (!mipp$conditionsMatch) {
            state = MachineState.CONDITION.ordinal();
        } else if (mipp$outputsBlocked) {
            state = MachineState.OUTPUT_FULL.ordinal();
        } else {
            state = MachineState.NO_RECIPE.ordinal();
        }

        entry.updateEntry((byte) state, currentTick);
        return original;
    }
}
