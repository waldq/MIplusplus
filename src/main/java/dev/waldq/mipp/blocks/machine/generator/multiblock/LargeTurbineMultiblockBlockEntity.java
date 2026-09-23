package dev.waldq.mipp.blocks.machine.generator.multiblock;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.blocks.machine.MIPPMachines;

import static aztech.modern_industrialization.machines.multiblocks.HatchTypes.*;

public class LargeTurbineMultiblockBlockEntity extends MIPPGeneratorMultiblockBlockEntity {
    public static String ID = "large_turbine";
    public static String NAME = "Large Turbine";
    public static ShapeTemplate getShape() {
        final SimpleMember CASING = SimpleMember.forBlockId(MI.id("clean_stainless_steel_machine_casing"));
        final SimpleMember PIPE = SimpleMember.forBlockId(MI.id("stainless_steel_machine_casing_pipe"));

        String[][] SHAPE = {
                {"CCC", "CEC", "CCC"},
                {"PPP", "PPP", "PPP"},
                {"PPP", "PPP", "PPP"},
                {"HHH", "H#H", "HHH"}
        };

        var largeTurbineShape = new ShapeTemplate.LayeredBuilder(MachineCasings.CLEAN_STAINLESS_STEEL, SHAPE)
                .key('C', CASING, null)
                .key('H', CASING, new HatchFlags.Builder().with(HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT).build())
                .key('E', CASING, new HatchFlags.Builder().with(ENERGY_OUTPUT).build())
                .key('P', PIPE, null)
                .build();
        return largeTurbineShape;
    }

    public LargeTurbineMultiblockBlockEntity(BEP bep) {
        this(bep, new ShapeTemplate[] { getShape() } );
    }
    private LargeTurbineMultiblockBlockEntity(BEP bep, ShapeTemplate[] largeTurbineShape) {
        super(bep, MIPP.id("large_turbine"), largeTurbineShape);
    }

    @Override
    public MachineRecipeType recipeType() { return MIPPMachines.RecipeTypes.TURBINE; }



}
