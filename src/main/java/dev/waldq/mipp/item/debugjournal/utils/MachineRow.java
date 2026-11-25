package dev.waldq.mipp.item.debugjournal.utils;

import aztech.modern_industrialization.api.energy.CableTier;
import dev.waldq.mipp.item.debugjournal.MachineState;
import io.netty.buffer.ByteBuf;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Objects;

public record MachineRow(
        ResourceKey<Level> dimension,
        long pos,
        Block machine,
        CableTier cableTier,
        boolean isOverdriving,
        byte state,
        byte[] history,
        byte mainHistoryState,
        short[] workTicks
) {
    public static MachineRow unavailable(ResourceKey<Level> dimension, long pos) {
        return new MachineRow(
                dimension,
                pos,
                Blocks.AIR,
                null,
                false,
                (byte) MachineState.NO_DATA.ordinal(),
                new byte[0],
                (byte) MachineState.NO_DATA.ordinal(),
                new short[0]
        );
    }

    private static final StreamCodec<ByteBuf, short[]> SHORT_ARRAY_STREAM_CODEC = StreamCodec.of(
            (buf, arr) -> {
                VarInt.write(buf, arr.length);
                for (short s : arr) VarInt.write(buf, s);
            },
            buf -> {
                short[] arr = new short[VarInt.read(buf)];
                for (int i = 0; i < arr.length; i++) arr[i] = (short) VarInt.read(buf);
                return arr;
            }
    );

    private static final StreamCodec<ByteBuf, CableTier> CABLE_TIER_STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
                    name -> name.isEmpty() ? null : CableTier.getTier(name),
                    tier -> tier == null ? "" : tier.name
            );

    public static final StreamCodec<ByteBuf, MachineRow> STREAM_CODEC = new StreamCodec<ByteBuf, MachineRow>() {
        @Override
        public MachineRow decode(ByteBuf buf) {
            ResourceKey<Level> dimension = ResourceKey.streamCodec(Registries.DIMENSION).decode(buf);
            long pos                     = ByteBufCodecs.VAR_LONG.decode(buf);
            Block machine                = ByteBufCodecs.idMapper(BuiltInRegistries.BLOCK).decode(buf);
            CableTier cableTier          = CABLE_TIER_STREAM_CODEC.decode(buf);
            boolean isOverdriving        = ByteBufCodecs.BOOL.decode(buf);
            byte state                   = ByteBufCodecs.BYTE.decode(buf);
            byte[] history               = ByteBufCodecs.BYTE_ARRAY.decode(buf);
            byte mainHistoryState        = ByteBufCodecs.BYTE.decode(buf);
            short[] workTicks            = SHORT_ARRAY_STREAM_CODEC.decode(buf);
            return new MachineRow(dimension, pos, machine, cableTier, isOverdriving, state, history, mainHistoryState, workTicks);
        }

        @Override
        public void encode(ByteBuf buf, MachineRow row) {
            ResourceKey.streamCodec(Registries.DIMENSION).encode(buf, row.dimension());
            ByteBufCodecs.VAR_LONG.encode(buf, row.pos());
            ByteBufCodecs.idMapper(BuiltInRegistries.BLOCK).encode(buf, row.machine());
            CABLE_TIER_STREAM_CODEC.encode(buf, row.cableTier());
            ByteBufCodecs.BOOL.encode(buf, row.isOverdriving());
            ByteBufCodecs.BYTE.encode(buf, row.state());
            ByteBufCodecs.BYTE_ARRAY.encode(buf, row.history());
            ByteBufCodecs.BYTE.encode(buf, row.mainHistoryState());
            SHORT_ARRAY_STREAM_CODEC.encode(buf, row.workTicks());
        }
    };

    public MachineState getMainHistoryState() {
        return MachineState.values[mainHistoryState];
    }

    public MachineState getCurrentState() {
        return MachineState.values[state];
    }

    @Override
    public byte[] history() {
        return history.clone();
    }

    @Override
    public short[] workTicks() {
        return workTicks.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MachineRow other)) return false;
        return dimension.equals(other.dimension())
                && pos == other.pos()
                && machine == other.machine()
                && cableTier == other.cableTier()
                && isOverdriving == other.isOverdriving()
                && state == other.state()
                && mainHistoryState == other.mainHistoryState()
                && Arrays.equals(history, other.history())
                && Arrays.equals(workTicks, other.workTicks());
    }

    @Override
    public int hashCode() {
        int h = Objects.hash(
                dimension,
                pos,
                machine,
                cableTier,
                isOverdriving,
                state,
                mainHistoryState
        );
        h = 31 * h + Arrays.hashCode(history);
        h = 31 * h + Arrays.hashCode(workTicks);
        return h;
    }
}
