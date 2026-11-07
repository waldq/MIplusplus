package dev.waldq.mipp.blocks.machine;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

import com.google.common.collect.Maps;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.blocks.machine.chunkloader.ChunkLoaderBlock;
import dev.waldq.mipp.blocks.machine.blockentities.ElectricChunkLoaderBlockEntity;

import net.minecraft.resources.ResourceLocation;

import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.MachineRecipeTypesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.MultiblockMachinesMIHookContext;
import net.swedz.tesseract.neoforge.compat.mi.hook.context.listener.SingleBlockSpecialMachinesMIHookContext;

import java.util.Map;
import java.util.function.Function;

public final class MIPPMachines {
    public static final class RecipeTypes {
        public static MachineRecipeType TURBINE;

        private static final Map<MachineRecipeType, String> RECIPE_TYPE_NAMES = Maps.newHashMap();

        public static Map<MachineRecipeType, String> getRecipeTypeNames() { return RECIPE_TYPE_NAMES; }

        private static MachineRecipeType create(
                MachineRecipeTypesMIHookContext hook,
                String englishName,
                String id,
                Function<ResourceLocation, MachineRecipeType> creator
        ) {
            MachineRecipeType recipeType = hook.create(id, creator);
            RECIPE_TYPE_NAMES.put(recipeType, englishName);
            return recipeType;
        }

        private static MachineRecipeType create(
                MachineRecipeTypesMIHookContext hook,
                String englishName,
                String id
        ) {
            return create(hook, englishName, id, MachineRecipeType::new);
        }

    }

    public static void recipeTypes(MachineRecipeTypesMIHookContext hook) {
//        RecipeTypes.TURBINE = RecipeTypes.create(hook, "Turbine", "turbine").withFluidInputs().withFluidOutputs();
    }

    public static void singleBlockSpecial(SingleBlockSpecialMachinesMIHookContext hook) {
//        GeneratorRecipeMachineBlockEntity.InventoryLayout layout = new GeneratorRecipeMachineBlockEntity.InventoryLayout(
//                167,
//                0, 0,
//                1, 4,
//                new SlotPositions.Builder().build(),
//                new SlotPositions.Builder().addSlot(26, 37).addSlots(88, 28, 2, 2).build(),
//                150, 40,
//                56, 35
//        );
//
//        REICategories.registerTurbineREICategory();


        for(CableTier tier : new CableTier[]{CableTier.LV, CableTier.MV, CableTier.HV})
        {
//            String name = "%s_turbine".formatted(tier.name);
//            String englishName = "%s Turbine".formatted(tier.shortEnglishName);
//            String overlayFolder = "steam_turbine";
//            hook.builder(name, englishName, (bep) -> new GeneratorRecipeMachineBlockEntity(bep, MIPP.id(name), tier, RecipeTypes.TURBINE, layout))
//                    .builtinModel(tier.casing, overlayFolder, (model) -> model.top(false).side(false).front(true).active(true).outputTextureEnergy())
//                    .registrator(MachineBlockEntity::registerItemApi)
//                    .registrator(MachineBlockEntity::registerFluidApi)
//                    .registrator(GeneratorRecipeMachineBlockEntity::registerEnergyApi)
//                    .gui(SteamMode.NEITHER, RecipeTypes.TURBINE, MachineGUIHelper::getTurbineGUI)
//                    .registerAsWorkstationFor(MIPP.id("turbine"))
//                    .registerMachine();

            hook.builder("%s_chunk_loader".formatted(tier.name), "%s Chunk Loader".formatted(tier.shortEnglishName), (bep) -> new ElectricChunkLoaderBlockEntity(bep, MIPP.id("%s_chunk_loader".formatted(tier.name)), tier))
                    .creator(((ctor, properties) -> new ChunkLoaderBlock(ctor, properties, tier)))
                    .builtinModel(tier.casing, "chunk_loader", (model) -> model.top(true).side(true).front(true).active(false).outputTextureDefault())
                    .registrator(ElectricChunkLoaderBlockEntity::registerEnergyApi)
                    .registerMachine();
        }


    }
    
    public static void multiblockMachines(MultiblockMachinesMIHookContext hook) {
//        hook.builder(LargeTurbineMultiblockBlockEntity.ID, LargeTurbineMultiblockBlockEntity.NAME, LargeTurbineMultiblockBlockEntity::new)
//            .builtinModel(MachineCasings.CLEAN_STAINLESS_STEEL, "steam_turbine")
//                .registerMultiblockShape(LargeTurbineMultiblockBlockEntity.getShape())
//                .gui(SteamMode.NEITHER, RecipeTypes.TURBINE, MachineGUIHelper::getTurbineGUI)
//                .registerAsWorkstationFor(MIPP.id("turbine"))
//                .registerMachine();
    }

}
