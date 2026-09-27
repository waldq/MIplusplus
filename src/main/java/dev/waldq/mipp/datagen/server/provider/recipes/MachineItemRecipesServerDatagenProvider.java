package dev.waldq.mipp.datagen.server.provider.recipes;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MITags;

import dev.waldq.mipp.MIPP;

import dev.waldq.mipp.MIPPTags;
import net.minecraft.data.recipes.RecipeOutput;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.swedz.tesseract.neoforge.compat.mi.recipe.MIMachineRecipeBuilder;
import net.swedz.tesseract.neoforge.compat.vanilla.recipe.ShapedRecipeBuilder;
import net.swedz.tesseract.neoforge.compat.vanilla.recipe.ShapelessRecipeBuilder;

import java.util.function.Consumer;

public final class MachineItemRecipesServerDatagenProvider extends RecipesServerDatagenProvider
{
	public MachineItemRecipesServerDatagenProvider(GatherDataEvent event)
	{
		super(event);
	}
	
	private static String machine(String machine, String tier)
	{
		return "%s:%s".formatted(MIPP.ID, tier == null ? machine : "%s_%s".formatted(tier, machine));
	}
	
	private static String machineBronze(String machine)
	{
		return machine(machine, "bronze");
	}
	
	private static String machineSteel(String machine)
	{
		return machine(machine, "steel");
	}
	
	private static void addBasicCraftingMachineRecipes(String machineName, String machineTier, Consumer<ShapedRecipeBuilder> crafting, boolean assembler, RecipeOutput output)
	{
		String recipeId = machineTier == null ? "" : "/%s".formatted(machineTier);
		
		ShapedRecipeBuilder builder = new ShapedRecipeBuilder();
		crafting.accept(builder);
		builder.output(machine(machineName, machineTier), 1);
		builder.offerTo(output, MIPP.id("machines/%s/craft%s".formatted(machineName, recipeId)));
		
		if(assembler)
		{
			MIMachineRecipeBuilder.fromShapedToAssembler(builder).offerTo(output, MIPP.id("machines/%s/assembler%s".formatted(machineName, recipeId)));
		}
	}
	
	private static void addBasicCraftingMachineRecipes(String machineName, Consumer<ShapedRecipeBuilder> crafting, boolean assembler, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machineName, null, crafting, assembler, output);
	}
	
	private static void addBasicCraftingMachineRecipes(String machineName, String machineTier, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machineName, machineTier, crafting, true, output);
	}
	
	private static void addBasicCraftingMachineRecipes(String machineName, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machineName, null, crafting, output);
	}
	
	private static void addBronzeMachineRecipes(String machine, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machine, "bronze", crafting, output);
	}
	
	private static void addSteelMachineRecipes(String machine, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machine, "steel", crafting, output);
	}
	
	private static void addElectricMachineRecipes(String machine, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(machine, "electric", crafting, output);
	}
	
	private static void addSteelUpgradeMachineRecipes(String machine, RecipeOutput output)
	{
		ShapelessRecipeBuilder builder = new ShapelessRecipeBuilder()
				.with(machineBronze(machine))
				.with(MIItem.STEEL_UPGRADE)
				.output(machineSteel(machine), 1);
		builder.offerTo(output, MIPP.id("machines/%s/craft/upgrade_steel".formatted(machine)));
		
		MIMachineRecipeBuilder.fromShapelessToPacker(builder).offerTo(output, MIPP.id("machines/%s/packer/upgrade_steel".formatted(machine)));
		
		MIMachineRecipeBuilder.fromShapelessToUnpackerAndFlip(builder).offerTo(output, MIPP.id("machines/%s/unpacker/downgrade_steel".formatted(machine)));
	}
	
	private static void addBronzeAndSteelMachineRecipes(String machine, Consumer<ShapedRecipeBuilder> crafting, RecipeOutput output)
	{
		addBronzeMachineRecipes(machine, crafting, output);
		addSteelUpgradeMachineRecipes(machine, output);
	}
	
	private static void addInterchangeableMachinesRecipes(String machineA, String machineB, RecipeOutput output)
	{
		String recipeBId = "from_%s".formatted(machineA);
		new ShapelessRecipeBuilder()
				.with(machine(machineA, null))
				.output(machine(machineB, null), 1)
				.offerTo(output, MIPP.id("machines/%s/craft/%s".formatted(machineB, recipeBId)));
		
		String recipeAId = "from_%s".formatted(machineB);
		new ShapelessRecipeBuilder()
				.with(machine(machineB, null))
				.output(machine(machineA, null), 1)
				.offerTo(output, MIPP.id("machines/%s/craft/%s".formatted(machineA, recipeAId)));
	}

	private static void chunkLoader(RecipeOutput output)
	{
		addBasicCraftingMachineRecipes(
				"lv_chunk_loader",
				(builder) -> builder
						.define('R', "modern_industrialization:robot_arm")
						.define('C', "modern_industrialization:analog_circuit")
						.define('H', "modern_industrialization:basic_machine_hull")
						.define('c', "modern_industrialization:tin_cable")
						.define('M', "modern_industrialization:motor")
						.pattern("cCc")
						.pattern("RHR")
						.pattern("CMC"),
				output
		);
		
		addBasicCraftingMachineRecipes(
				"mv_chunk_loader",
				(builder) -> builder
						.define('L', "mipp:lv_chunk_loader")
						.define('C', "modern_industrialization:electronic_circuit")
						.define('H', "modern_industrialization:advanced_machine_hull")
						.define('c', "modern_industrialization:electrum_cable")
						.define('M', "modern_industrialization:large_motor")
						.pattern("cCc")
						.pattern("LHL")
						.pattern("CMC"),
				output
		);
		
		addBasicCraftingMachineRecipes(
				"hv_chunk_loader",
				(builder) -> builder
						.define('L', "mipp:mv_chunk_loader")
						.define('C', "modern_industrialization:digital_circuit")
						.define('H', "modern_industrialization:turbo_machine_hull")
						.define('c', "modern_industrialization:aluminum_cable")
						.define('M', "modern_industrialization:advanced_motor")
						.pattern("cCc")
						.pattern("LHL")
						.pattern("CMC"),
				output
		);
	}

	
	@Override
	protected void buildRecipes(RecipeOutput output)
	{
		chunkLoader(output);
	}
}
