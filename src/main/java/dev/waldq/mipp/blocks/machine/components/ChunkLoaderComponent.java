package dev.waldq.mipp.blocks.machine.components;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.blocks.machine.blockentities.ElectricChunkLoaderBlockEntity;

import net.minecraft.server.level.ServerLevel;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.HashSet;
import java.util.Set;

public class ChunkLoaderComponent {
    private static final TicketController ticketController = new TicketController(MIPP.id("default"), ChunkLoaderComponent::validateTickets);

    public static TicketController getTicketController() { return ticketController; }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ChunkLoaderComponent::registerTicketController);
    }

    private static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(ticketController);
    }

    private static void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
        ticketHelper.getBlockTickets().forEach((pos, chunks) -> {
            MIPP.LOGGER.debug("Validating {} ticking chunk tickets for {}", chunks.ticking().size(), pos);

            if (!(level.getBlockEntity(pos) instanceof ElectricChunkLoaderBlockEntity machine && machine.getIsEnabled())) {
                ticketHelper.removeAllTickets(pos);
                MIPP.LOGGER.debug("Removed ({}) ticking and ({}) non-ticking chunks for block at pos ({}) in dimension ({})",
                        chunks.ticking().size(),
                        chunks.nonTicking().size(),
                        pos,
                        level.dimension().location());
                return;
            }

            Set<Long> toRemoveNonTicking = new HashSet<>(chunks.nonTicking());
            if (!toRemoveNonTicking.isEmpty()) {
                toRemoveNonTicking.forEach(l -> ticketHelper.removeTicket(pos, l, false));
                MIPP.LOGGER.debug("Removed ({}) non-ticking chunks for block at pos ({}) in dimension ({})",
                        chunks.nonTicking().size(),
                        pos,
                        level.dimension().location());
            }
        });
    }
}