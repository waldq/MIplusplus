package dev.waldq.mipp;

import com.mojang.serialization.Codec;

import dev.waldq.mipp.network.PlayerSyncHandler;

import net.minecraft.core.UUIDUtil;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

public class MIPPAttachments {
    public static void init(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MIPP.MODID);

    public static final Supplier<AttachmentType<Integer>> CHUNK_AMOUNT = ATTACHMENT_TYPES.register(
            "chunks_amount", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .sync(new PlayerSyncHandler())
                    .build()
    );

    public static final Supplier<AttachmentType<UUID>> OWNER_UUID = ATTACHMENT_TYPES.register(
            "owner_uuid", () -> AttachmentType.<UUID>builder(() -> null)
                    .serialize(UUIDUtil.STRING_CODEC)
                    .build()
    );
}


