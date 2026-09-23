package dev.waldq.mipp.compat.mi;

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

import dev.waldq.mipp.MIPPBlocks;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.MIPPRecipeTypes;
import dev.waldq.mipp.MIPPCreativeTab;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.registries.DeferredRegister;

import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookEntrypoint;
import net.swedz.tesseract.neoforge.compat.mi.hook.MIHookRegistry;
import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

@MIHookEntrypoint
public final class MIPPMIHookRegistry implements MIHookRegistry {
    @Override
    public DeferredRegister.Blocks blockRegistry() { return MIPPBlocks.Registry.BLOCKS; }

    @Override
    public DeferredRegister<BlockEntityType<?>> blockEntityRegistry() { return MIPPBlocks.Registry.BLOCK_ENTITIES; }

    @Override
    public DeferredRegister.Items itemRegistry() { return MIPPItems.Registry.ITEMS; }

    @Override
    public DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry() {return MIPPRecipeTypes.RECIPE_SERIALIZERS; }

    @Override
    public DeferredRegister<RecipeType<?>> recipeTypeRegistry()
    {
        return MIPPRecipeTypes.RECIPE_TYPES;
    }

    @Override
    public void onBlockRegister(BlockHolder blockHolder) { MIPPBlocks.Registry.include(blockHolder); }

    @Override
    public void onBlockEntityRegister(BlockEntityType<?> blockEntityType) {}

    @Override
    public void onItemRegister(ItemHolder itemHolder) { MIPPItems.Registry.include(itemHolder); }

    @Override
    public void onMachineRecipeTypeRegister(MachineRecipeType machineRecipeType) {}

    @Override
    public SortOrder sortOrderMachines() { return MIPPCreativeTab.Order.MACHINES; }
}
