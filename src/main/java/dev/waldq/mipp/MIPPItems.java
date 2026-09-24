package dev.waldq.mipp;

import com.google.common.collect.Sets;
import dev.waldq.mipp.item.pospector.ProspectorItemBehavior;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.common.CommonModelBuilders;
import net.swedz.tesseract.neoforge.registry.common.MICommonCapabitilies;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

import java.util.Set;
import java.util.function.Function;

public final class MIPPItems {
    public static final class Registry {
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MIPP.ID);
        private static final Set<ItemHolder> HOLDERS = Sets.newHashSet();

        private static void init(IEventBus bus) { ITEMS.register(bus); }

        public static void include(ItemHolder holder) { HOLDERS.add(holder); }
    }

    public static Set<ItemHolder> values() { return Set.copyOf(Registry.HOLDERS); }

    public static ItemHolder valueOf(String id) {
        return MIPPItems.Registry.HOLDERS.stream()
                .filter((holder) -> holder.identifier().id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public static void init(IEventBus bus) { Registry.init(bus); }

    public static <Type extends Item> ItemHolder<Type> create(String id, String englishName, Function<Item.Properties, Type> creator, SortOrder sortOrder) {
        ItemHolder<Type> holder = new ItemHolder<>(MIPP.id(id), englishName, Registry.ITEMS, creator).sorted(sortOrder);
        Registry.include(holder);
        return holder;
    }

    public static final ItemHolder<ProspectorItemBehavior> ELECTRIC_PROSPECTOR = create(
            "electric_prospector", "Electric Prospector", p -> new ProspectorItemBehavior(p, 3200L),
            MIPPCreativeTab.Order.ELECTRIC_PROSPECTOR)
            .withCapabilities(MICommonCapabitilies::simpleEnergyItem)
            .withModelBuilder(CommonModelBuilders::generated)
            .register();

    public static final ItemHolder<Item> ENERGY_ZAP = create(
            "energy_zap", "Energy Zap", Item::new,
            MIPPCreativeTab.Order.HIDDEN)
            .withModelBuilder(CommonModelBuilders::generated)
            .register();

}