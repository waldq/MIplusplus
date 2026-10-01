package dev.waldq.mipp.network;

import net.swedz.tesseract.neoforge.packet.CustomPacket;

public interface MIPPCustomPacket extends CustomPacket
{
    @Override
    default Type<MIPPCustomPacket> type()
    {
        return MIPPPackets.getType(this.getClass());
    }
}