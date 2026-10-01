package dev.waldq.mipp;

import aztech.modern_industrialization.client.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.client.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;

import dev.waldq.mipp.client.MIPPItemProperties;
import dev.waldq.mipp.item.pipenetworkanalyzer.PipeNetworkAnalyzerItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.swedz.tesseract.api.Assert;
import net.swedz.tesseract.config.ConfigManager;
import net.swedz.tesseract.neoforge.config.ModConfigFileAccess;

@Mod(value = MIPP.ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MIPP.ID, value = Dist.CLIENT)
public final class MIPPClient {
    public MIPPClient(IEventBus bus, ModContainer container) {
        setupConfig(bus, container);

        NeoForge.EVENT_BUS.addListener(InputEvent.MouseScrollingEvent.class, (event) -> {
            if(Screen.hasShiftDown()) {
                Minecraft mc = Minecraft.getInstance();
                var player = mc.player;

                if (player == null) return;
                if (mc.screen != null) return;
                if (!PipeNetworkAnalyzerItem.isHolding(player)) return;

                var delta = event.getScrollDeltaY();
                if (delta == 0) return;

                double newDist = PipeNetworkAnalyzerItem.adjustDistance(Math.signum(delta));
                event.setCanceled(true);

                player.displayClientMessage(
                        MIPP.text().pipeNetworkAnalyzerRange(
                                Component.literal(String.valueOf((int) newDist))
                        ),
                        true
                );

            }
        });
    }


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

    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(MIPPItemProperties::register);
    }

    private static MIPPClientConfig CONFIG;

    public static MIPPClientConfig config() {
        Assert.notNull(CONFIG, "Config not yet loaded");
        return CONFIG;
    }

    private static void setupConfig(IEventBus bus, ModContainer container) {
        var file = new ModConfigFileAccess(container, ModConfig.Type.CLIENT);
        var instance = new ConfigManager(file)
                .build(MIPPClientConfig.class)
                .load();
        bus.addListener(FMLCommonSetupEvent.class, (event) -> instance.load(false));
        CONFIG = instance.config();
    }
}
