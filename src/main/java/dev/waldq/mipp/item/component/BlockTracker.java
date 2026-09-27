package dev.waldq.mipp.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 *  @param target actual position compass will point at
 *  @param tracked whether we need to validate the position or not
 */
public record BlockTracker(Optional<GlobalPos> target, boolean tracked) {
    public static final Codec<BlockTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("target").forGetter(BlockTracker::target),
            Codec.BOOL.optionalFieldOf("tracked", true).forGetter(BlockTracker::tracked)
    ).apply(instance, BlockTracker::new));

    public static final StreamCodec<ByteBuf, BlockTracker> STREAM_CODEC;

    public BlockTracker tick(ServerLevel level) {
        if (this.tracked && this.target.isPresent()) {
            if (this.target.get().dimension() != level.dimension()) {
                return this;
            } else {
                BlockPos pos = this.target.get().pos();
                return level.isInWorldBounds(pos) ? this : new BlockTracker(Optional.empty(), true);
            }
        } else {
            return this;
        }
    }

    static {
        STREAM_CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional), BlockTracker::target, ByteBufCodecs.BOOL, BlockTracker::tracked, BlockTracker::new);
    }
}
