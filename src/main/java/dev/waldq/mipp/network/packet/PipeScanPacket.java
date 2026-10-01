package dev.waldq.mipp.network.packet;

import dev.waldq.mipp.client.ClientPipeScan;
import dev.waldq.mipp.network.MIPPCustomPacket;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.swedz.tesseract.neoforge.packet.PacketContext;

import java.util.List;

public record PipeScanPacket(List<PipeScanEntry> entries) implements MIPPCustomPacket {
    public static final StreamCodec<ByteBuf, PipeScanPacket> STREAM_CODEC = PipeScanEntry.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(PipeScanPacket::new, PipeScanPacket::entries);

    public record PipeScanEntry(BlockPos pos, int extracted, int inserted) {
        public static final StreamCodec<ByteBuf, PipeScanEntry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, PipeScanEntry::pos,
                ByteBufCodecs.VAR_INT, PipeScanEntry::extracted,
                ByteBufCodecs.VAR_INT, PipeScanEntry::inserted,
                PipeScanEntry::new
        );
    }

    @Override
    public void handle(PacketContext context) {
        context.assertClientbound();
        ClientPipeScan.handle(this, context.getPlayer().level().dimension());
    }
}

