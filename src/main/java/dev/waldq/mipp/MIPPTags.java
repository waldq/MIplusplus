package dev.waldq.mipp;

import com.google.common.collect.Maps;

import dev.waldq.mipp.utils.ColorUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;

import java.util.Collections;
import java.util.Map;

public final class MIPPTags {
    private static final Map<TagKey<Item>, String> TRANSLATIONS = Maps.newHashMap();

    public static Map<TagKey<Item>, String> translations()
    {
        return Collections.unmodifiableMap(TRANSLATIONS);
    }

    public interface Items {
        TagKey<Item> CASINGS = item("casings");
        TagKey<Item> CASINGS_BASE = item("casings/base");
        TagKey<Item> CASINGS_PIPE = item("casings/pipe");
        TagKey<Item> CASINGS_SPECIAL = item("casings/special");

        static TagKey<Item> casingColor(String color) {
            return item("casings/color/" + color);
        }
    }

    public interface Blocks {

        TagKey<Block> STONES = block("stones");
        TagKey<Block> NETHER_REPLACEABLE = block("nether_replaceable");
        TagKey<Block> SUPPORTS_SAMPLE = block("supports_sample");
        TagKey<Block> CASINGS = block("casings");
        TagKey<Block> CASINGS_BASE = block("casings/base");
        TagKey<Block> CASINGS_PIPE = block("casings/pipe");
        TagKey<Block> CASINGS_SPECIAL = block("casings/special");
        TagKey<Block> CASINGS_COLOR = block("casings/color");

        static TagKey<Block> casingColor(String color) {
            return block("casings/color/" + color);
        }
    }

    public static TagKey<Item> item(String path, String englishName) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, MIPP.id(path));
        TRANSLATIONS.put(tag, englishName);
        return tag;
    }

    public static TagKey<Item> item(String path) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, MIPP.id(path));
        return tag;
    }

    public static TagKey<Item> itemCommon(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static TagKey<Fluid> fluid(String path) {
        return TagKey.create(Registries.FLUID, MIPP.id(path));
    }

    public static TagKey<Fluid> fluidCommon(String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, MIPP.id(path));
    }

    public static TagKey<Block> blockCommon(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static TagKey<DimensionType> dimensionType(String path) {
        return TagKey.create(Registries.DIMENSION_TYPE, MIPP.id(path));
    }

    public static TagKey<DamageType> damageType(String path) {
        return TagKey.create(Registries.DAMAGE_TYPE, MIPP.id(path));
    }

    public static TagKey<MobEffect> mobEffect(String path) {
        return TagKey.create(Registries.MOB_EFFECT, MIPP.id(path));
    }

    public static TagKey<Item> asItemTag(TagKey<Block> blockTag) {
        return TagKey.create(Registries.ITEM, blockTag.location());
    }
}
