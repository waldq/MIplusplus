package dev.waldq.mipp;

import aztech.modern_industrialization.client.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = MIPP.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MIPP.MODID, value = Dist.CLIENT)
public final class MIPPClient {
    @SubscribeEvent
    private static void registerBlockEntityRenderers(FMLClientSetupEvent event) {
        for(var blockDef : MIPPBlocks.Registry.BLOCKS.getEntries()) {
            if(blockDef.get() instanceof MachineBlock machine) {
                try {
                    var blockEntity = machine.getBlockEntityInstance();
                    var type = blockEntity.getType();

                    BlockEntityRendererProvider provider = switch (blockEntity) {
                        case MultiblockMachineBlockEntity __ -> MultiblockMachineBER::new;
                        default -> MachineBlockEntityRenderer::new;
                    };
                    BlockEntityRenderers.register(type, provider);
                }
                catch (Exception ex) {
                    throw new RuntimeException("Failed to register BER for %s".formatted(blockDef.getId()), ex);
                }
            }
        }
    }
}
