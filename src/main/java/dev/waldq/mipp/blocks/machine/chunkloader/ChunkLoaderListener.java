package dev.waldq.mipp.blocks.machine.chunkloader;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.blocks.machine.blockentity.ElectricChunkLoaderBlockEntity;
import dev.waldq.mipp.saveddata.ChunkLoaderSavedData;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import static dev.waldq.mipp.MIPPAttachments.CHUNK_AMOUNT;

@EventBusSubscriber(modid = MIPP.ID)
public class ChunkLoaderListener {
    @SubscribeEvent
    private static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        if (event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK
                && event.getUseOnContext().getItemInHand().getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ChunkLoaderBlock clb) {

            int current = player.getData(CHUNK_AMOUNT);
            int maxChunkAmount = clb.getMaxChunksAmount();

            if (current + clb.getChunksAmount() > maxChunkAmount) {
                if (!event.getLevel().isClientSide()) {
                    player.displayClientMessage(
                            MIPP.text().chunkLoader().tooMuch(
                                    Component.literal(String.valueOf(current))
                            ),
                            true
                    );
                }

                event.cancelWithResult(ItemInteractionResult.FAIL);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel() instanceof ServerLevel level) {
            if (level.getBlockEntity(event.getPos()) instanceof ElectricChunkLoaderBlockEntity be) {
                ChunkLoaderSavedData.get(level).addLoader(
                        level,
                        player.getUUID(),
                        event.getPos(),
                        be.getCableTier()
                );
            }
        }
    }

    @SubscribeEvent
    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ChunkLoaderSavedData data = ChunkLoaderSavedData.get(player.level());
            data.syncPlayerChunks(player.getServer(), player.getUUID());
        }
    }

    private ChunkLoaderListener() {}
}
