package dev.waldq.mipp;

import com.mojang.serialization.Codec;
import dev.waldq.mipp.item.component.BlockTracker;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MIPPComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MIPP.ID);

    // for compass
    public static final Supplier<DataComponentType<BlockTracker>> BLOCK_TRACKER = COMPONENTS.registerComponentType(
            "chunk_tracker",
            builder -> builder.persistent(BlockTracker.CODEC).networkSynchronized(BlockTracker.STREAM_CODEC).cacheEncoding()
    );

    // for spinning during prospector scanning
    public static final Supplier<DataComponentType<Boolean>> IS_SCANNING = COMPONENTS.registerComponentType(
            "is_scanning",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static void init(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

    private MIPPComponents() {}
}
