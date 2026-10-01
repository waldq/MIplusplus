package dev.waldq.mipp.network;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.network.packet.PipeScanPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.swedz.tesseract.neoforge.packet.PacketRegistry;

public class MIPPPackets {
    private static final PacketRegistry<MIPPCustomPacket> REGISTRY = PacketRegistry.create(MIPP.ID);

    public static CustomPacketPayload.Type<MIPPCustomPacket> getType(Class<? extends MIPPCustomPacket> packetClass) {
        return REGISTRY.getType(packetClass);
    }

    public static void init(RegisterPayloadHandlersEvent event)
    {
        REGISTRY.registerAll(event);
    }

    static {
        create("pipe_scan", PipeScanPacket.class, PipeScanPacket.STREAM_CODEC);
    }

    private static <P extends MIPPCustomPacket> void create(String id, Class<P> packetClass, StreamCodec<? super RegistryFriendlyByteBuf, P> packetCodec) {
        REGISTRY.create(id, packetClass, packetCodec);
    }
}
