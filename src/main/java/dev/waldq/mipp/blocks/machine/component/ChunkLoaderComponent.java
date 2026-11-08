package dev.waldq.mipp.blocks.machine.component;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.saveddata.ChunkLoaderSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.HashSet;
import java.util.Set;

public class ChunkLoaderComponent {
    private static final TicketController ticketController = new TicketController(MIPP.id("default"), ChunkLoaderComponent::validateTickets);

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ChunkLoaderComponent::registerTicketController);
    }

    public static void setChunks(ServerLevel level, BlockPos pos, int range, boolean add) {
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;
        for (int x = centerChunkX - range; x < centerChunkX + range + 1; x++){
            for (int z = centerChunkZ - range; z < centerChunkZ + range + 1; z++){
                ticketController.forceChunk(level, pos, x, z, add, true);
            }
        }
    }

    private static void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
        var loaders = ChunkLoaderSavedData.get(level).getAllLoaders();
        ticketHelper.getBlockTickets().forEach((pos, chunks) -> {
            MIPP.LOGGER.debug("Validating {} ticking chunk tickets for {}", chunks.ticking().size(), pos);

            var key = new ChunkLoaderSavedData.LoaderKey(level.dimension().location(), pos);
            if (!loaders.containsKey(key)) {
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

    private static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(ticketController);
    }
}