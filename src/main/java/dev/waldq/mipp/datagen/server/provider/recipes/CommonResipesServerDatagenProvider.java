package dev.waldq.mipp.datagen.server.provider.recipes;

import aztech.modern_industrialization.MI;
import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPBlocks;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.MIPPTags;
import dev.waldq.mipp.utils.ColorUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.swedz.tesseract.neoforge.compat.mi.recipe.MIMachineRecipeBuilder;
import net.swedz.tesseract.neoforge.compat.vanilla.recipe.ShapedRecipeBuilder;
import net.swedz.tesseract.neoforge.compat.vanilla.recipe.ShapelessRecipeBuilder;
import net.swedz.tesseract.neoforge.compat.vanilla.recipe.StonecutterRecipeBuilder;

import java.util.Objects;
import java.util.function.Consumer;

public class CommonResipesServerDatagenProvider extends RecipesServerDatagenProvider {
    public CommonResipesServerDatagenProvider(GatherDataEvent event) { super(event); }

    private static void addBasicCraftingRecipes(
            String path, String name,
            boolean assembler,
            ItemLike result, int resultCount,
            Consumer<ShapedRecipeBuilder> crafting,
            RecipeOutput output
    ) {
        ShapedRecipeBuilder shapedRecipeBuilder = new ShapedRecipeBuilder();
        crafting.accept(shapedRecipeBuilder);
        shapedRecipeBuilder.output(result, resultCount);
        shapedRecipeBuilder.offerTo(output, MIPP.id(path + "/craft/" + name));

        if(assembler) {
            MIMachineRecipeBuilder.fromShapedToAssembler(shapedRecipeBuilder).offerTo(output, MIPP.id(path + "/assembler/" + name));
        }
    }

    private static void casings(RecipeOutput output) {
        addBasicCraftingRecipes(
                "casings/base", "basic", true,
                MIPPBlocks.BASIC_CASING.get(), 1,
                r -> r
                        .define('P', "modern_industrialization:iron_plate")
                        .pattern(" P ")
                        .pattern("P P")
                        .pattern(" P "),
                output
        );

        stonecutterGroup(
                output, "basic",
                MIPPBlocks.BASIC_CASING.get(),
                MIPPBlocks.BASIC_CASING_PIPE.get(),
                MIPPBlocks.BASIC_SPECIAL_CASING.get()
        );

        String[] ids = new String[]{"base", "pipe", "special"};

        for (var color : ColorUtils.COLORS) {
            var dye = color.id() + "_dye";
            addBasicCraftingRecipes(
                    "casings", "base/%s/direct".formatted(color.id()), false,
                    MIPPBlocks.COLORED_CASINGS.get(color.id()).get(), 1,
                    r -> r
                            .define('P', MI.id("iron_plate"))
                            .define('d', "minecraft:%s".formatted(dye))
                            .pattern(" P ")
                            .pattern("PdP")
                            .pattern(" P "),
                    output
            );

            for (var id : ids) {
                coloredGroup(output, id, color.id());
            }

            interchangeableCasings(output, color.id());
        }
    }

    private static void tools(RecipeOutput output) {
        addBasicCraftingRecipes(
                "tools", "electric_prospector", true,
                MIPPItems.ELECTRIC_PROSPECTOR.asItem(), 1,
                r -> r
                        .define('G', Items.GLOWSTONE_DUST)
                        .define('I', "#c:plates/iron")
                        .define('B', MI.id("analog_circuit"))
                        .define('c', MI.id("capacitor"))
                        .define('i', MI.id("inductor"))
                        .define('r', MI.id("resistor"))
                        .pattern(" G ")
                        .pattern("IBI")
                        .pattern("cri"),
                output
        );
    }

    private static void interchangeableCasings(RecipeOutput output, String color) {
        var casingBase = MIPPBlocks.COLORED_CASINGS.get(color).get();
        var casingPipe = MIPPBlocks.COLORED_CASINGS_PIPE.get(color).get();
        var casingSpecial = MIPPBlocks.COLORED_SPECIAL_CASINGS.get(color).get();

        stonecutterGroup(output, color, casingBase, casingPipe, casingSpecial);
    }

    private static void stonecutterGroup(
            RecipeOutput output,
            String color,
            ItemLike... items
    ) {
        Ingredient input = Ingredient.of(items);

        for (ItemLike result : items) {
            String name = BuiltInRegistries.ITEM.getKey(result.asItem()).getPath();

            new StonecutterRecipeBuilder()
                    .input(input)
                    .output(result, 1)
                    .offerTo(output, MIPP.id("casings/stonecutter/%s/%s".formatted(color, name)));
        }
    }

    private static void coloredGroup(
            RecipeOutput output,
            String result,
            String color
    ) {
        var dye = color + "_dye";

        var out = switch (result) {
            case "base" -> "casing";
            case "pipe" -> "casing_pipe";
            case "special" -> "special_casing";
            default -> result;
        };

        Item item = BuiltInRegistries.ITEM.get(MIPP.id("%s_%s".formatted(color, out)));

        ShapelessRecipeBuilder builder = new ShapelessRecipeBuilder()
                .with("#" + MIPP.id("casings/%s".formatted(result)))
                .with(dye)
                .output(item, 1);
        builder.offerTo(output, MIPP.id("casings/craft/%s/%s/casings_1".formatted(result, color)));

        addBasicCraftingRecipes(
                "casings", "%s/%s/casings_8".formatted(result, color), false,
                item, 8,
                r -> r
                        .define('c', "#" + MIPP.id("casings/%s".formatted(result)))
                        .define('d', "minecraft:%s".formatted(dye))
                        .pattern("ccc")
                        .pattern("cdc")
                        .pattern("ccc"),
                output
        );

    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        casings(output);
        tools(output);
    }
}
