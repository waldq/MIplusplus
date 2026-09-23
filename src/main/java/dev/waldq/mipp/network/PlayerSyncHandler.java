package dev.waldq.mipp.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class PlayerSyncHandler implements AttachmentSyncHandler<Integer> {

    @Override
    public void write(RegistryFriendlyByteBuf buf, Integer attachment, boolean initialSync) {
        buf.writeVarInt(attachment);
    }

    @Override
    public Integer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, Integer previousValue) {
        return buf.readVarInt();
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }
}
