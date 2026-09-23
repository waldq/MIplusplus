package dev.waldq.mipp;

import aztech.modern_industrialization.util.TagHelper;


import com.google.common.collect.Sets;
import dev.waldq.mipp.blocks.samples.SampleBlock;
import dev.waldq.mipp.utils.ColorUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.common.CommonLootTableBuilders;
import net.swedz.tesseract.neoforge.registry.common.CommonModelBuilders;
import net.swedz.tesseract.neoforge.registry.holder.BlockHolder;
import net.swedz.tesseract.neoforge.registry.holder.BlockWithItemHolder;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MIPPBlocks {
    public static final class Registry {
        public static final  DeferredRegister.Blocks              BLOCKS         = DeferredRegister.createBlocks(MIPP.MODID);
        public static final  DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MIPP.MODID);
        private static final Set<BlockHolder>                     HOLDERS        = Sets.newHashSet();

        private static void init(IEventBus bus) {
            BLOCKS.register(bus);
            BLOCK_ENTITIES.register(bus);
        }

        public static void include(BlockHolder holder) {
            HOLDERS.add(holder);
        }
    }

    public static void init(IEventBus bus) {
        Registry.init(bus);
    }

    public static final BlockHolder<SampleBlock> SAMPLE_BLOCK = create("sample", "Sample", SampleBlock::new)
            .withProperties(p -> SampleBlock.createProperties())
            .withLootTable(CommonLootTableBuilders::self)
            .withModel(CommonModelBuilders::blockstateOnly)
            .register();

    public static final Map<String, BlockHolder<Block>> COLORED_CASINGS = registerCasings("%s_casing", "%s Casing");

    public static final Map<String, BlockHolder<Block>> COLORED_CASINGS_PIPE = registerCasings("%s_casing_pipe", "%s Casing Pipe");

    public static final Map<String, BlockHolder<Block>> COLORED_SPECIAL_CASINGS = registerCasings("%s_special_casing", "%s Special Casing");

    private static Map<String, BlockHolder<Block>> registerCasings(String id, String englishName) {
        return Arrays.stream(ColorUtils.COLORS)
                .collect(Collectors.toMap(
                        ColorUtils.ColorEntry::id,
                        color -> createSimple(
                                id.formatted(color.id()),
                                englishName.formatted(color.englishName()),
                                MIPPCreativeTab.Order.BLOCKS,
                                color.mapColor(),
                                5.0f,
                                6.0f
                        )
                                .withLootTable(CommonLootTableBuilders::self)
                                .withModel(CommonModelBuilders::blockCubeAll)
                                .register()
                ));
    }

    public static final BlockHolder<Block> BASIC_CASING = createSimple(
            "basic_casing",
            "Basic Casing",
            MIPPCreativeTab.Order.BLOCKS,
            MapColor.NONE,
            5.0f,
            6.0f
    )
            .withLootTable(CommonLootTableBuilders::self)
            .withModel(CommonModelBuilders::blockCubeAll)
            .register();

    public static final BlockHolder<Block> BASIC_CASING_PIPE = createSimple(
            "basic_casing_pipe",
            "Basic Casing Pipe",
            MIPPCreativeTab.Order.BLOCKS,
            MapColor.NONE,
            5.0f,
            6.0f
    )
            .withLootTable(CommonLootTableBuilders::self)
            .withModel(CommonModelBuilders::blockCubeAll)
            .register();

    public static final BlockHolder<Block> BASIC_SPECIAL_CASING = createSimple(
            "basic_special_casing",
            "Basic Special Casing",
            MIPPCreativeTab.Order.BLOCKS,
            MapColor.NONE,
            5.0f,
            6.0f
    )
            .withLootTable(CommonLootTableBuilders::self)
            .withModel(CommonModelBuilders::blockCubeAll)
            .register();

    public static Set<BlockHolder> values() {
        return Set.copyOf(Registry.HOLDERS);
    }

    public static Block get(String id) {
        return Registry.HOLDERS.stream().filter((b) -> b.identifier().id().equals(id)).findFirst().orElseThrow().get();
    }

    public static <BlockType extends Block> BlockHolder<BlockType> create(
            String id, String englishName,
            Function<BlockBehaviour.Properties, BlockType> blockCreator) {
        BlockHolder<BlockType> holder = new BlockHolder<>(
                MIPP.id(id), englishName,
                Registry.BLOCKS, blockCreator
        );
        Registry.include(holder);
        return holder;
    }

    public static <BlockType extends Block, ItemType extends BlockItem> BlockWithItemHolder<BlockType, ItemType> create(
            String id, String englishName,
            Function<BlockBehaviour.Properties, BlockType> blockCreator,
            BiFunction<Block, Item.Properties, ItemType> itemCreator,
            SortOrder sortOrder
    ) {
        BlockWithItemHolder<BlockType, ItemType> holder = new BlockWithItemHolder<>(
                MIPP.id(id), englishName,
                Registry.BLOCKS, blockCreator,
                MIPPItems.Registry.ITEMS, itemCreator
        );
        holder.item().sorted(sortOrder);
        Registry.include(holder);
        MIPPItems.Registry.include(holder.item());
        return holder;
    }

    public static BlockHolder<Block> createSimple(String id, String englishName, SortOrder sortOrder, MapColor mapColor, float destroyTime, float explosionResistance) {
        return create(id, englishName, Block::new, BlockItem::new, sortOrder)
                .withProperties((p) -> p.mapColor(mapColor).destroyTime(destroyTime).explosionResistance(explosionResistance).requiresCorrectToolForDrops())
                .tag(TagHelper.getMiningLevelTag(1))
                .withLootTable(CommonLootTableBuilders::self);
    }
}
