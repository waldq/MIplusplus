package dev.waldq.mipp;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.swedz.tesseract.neoforge.registry.SortOrder;
import net.swedz.tesseract.neoforge.registry.holder.ItemHolder;

import java.util.Comparator;
import java.util.function.Supplier;

public class MIPPCreativeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MIPP.ID);

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TAB.register(MIPP.ID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.%s.%s".formatted(MIPP.ID, MIPP.ID)))
            .icon(() -> MIPPItems.ENERGY_ZAP.asItem().getDefaultInstance())
            .displayItems((params, output) -> {
                Comparator<ItemHolder> compareBySortOrder = Comparator.comparing(ItemHolder::sortOrder);
                Comparator<ItemHolder> compareByName = Comparator.comparing((it) -> it.identifier().id());
                MIPPItems.values().stream()
                        .filter(itemHolder -> !itemHolder.sortOrder().equals(Order.HIDDEN))
                        .sorted(compareBySortOrder.thenComparing(compareByName))
                        .forEach(output::accept);
            })
            .build());

    public static void init(IEventBus bus) {
        CREATIVE_MODE_TAB.register((bus));
    }

    public static class Order {
        public static SortOrder GUIDEBOOK = new SortOrder(-1);
        public static SortOrder ELECTRIC_PROSPECTOR = new SortOrder(0);
        public static SortOrder MACHINES = new SortOrder(1);
        public static SortOrder BLOCKS = new SortOrder(2);

        public static SortOrder HIDDEN = new SortOrder(1000);
    }
}
