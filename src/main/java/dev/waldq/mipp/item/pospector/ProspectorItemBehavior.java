package dev.waldq.mipp.item.pospector;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.util.TextHelper;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPConfig;
import dev.waldq.mipp.item.ElectricItem;
import dev.waldq.mipp.item.IItemUIHolder;
import dev.waldq.mipp.item.pospector.gui.MapHelper;
import dev.waldq.mipp.item.pospector.gui.ProspectorMapWidget;
import dev.waldq.mipp.item.pospector.utils.ScannerLarge;
import dev.waldq.mipp.item.pospector.utils.ScannerLocal;
import dev.waldq.mipp.item.pospector.utils.ServerTickListener;
import dev.waldq.mipp.worldgen.veins.OreVeinConfig;
import dev.waldq.mipp.worldgen.veins.OreVeinConfigLoader;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ProspectorItemBehavior extends ElectricItem implements IItemUIHolder {

    private int getEnergyCost() {
        return MIPP.config().prospectorPerameters().prospectorEnergyCost();
    }

    private int getLargeChunkRadius() {
        return MIPP.config().prospectorPerameters().prospectorLargeScanR();
    }

    private int getLocalChunkRadius() {
        return MIPP.config().prospectorPerameters().prospectorLocalScanR();
    }

    public ProspectorItemBehavior(Properties properties, long capacity) {
        super(properties, capacity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!player.isCreative() && getStoredEnergy(stack) < getEnergyCost()) {
            if (!level.isClientSide()) {
                var amount = TextHelper.getAmount(getEnergyCost());
                player.displayClientMessage(MIPP.text().prospectorEnergyNotice(MIText.Eu.text(amount.digit(), amount.unit())), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            if (level instanceof ServerLevel serverLevel) {
                if (!player.isCreative()) {
                    tryUseEnergy(stack, getEnergyCost());
                }

                ResourceLocation dim = serverLevel.getLevel().dimension().location();

                List<OreVeinConfig> activeVeins = OreVeinConfigLoader.getVeinsForDimension(dim);

                if (player.isShiftKeyDown()) {
                    scanLocal(serverLevel, player, usedHand, activeVeins);
                } else {
                    scanLarge(serverLevel, player, dim, activeVeins);
                }
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void scanLarge(ServerLevel level, Player player, ResourceLocation dim, List<OreVeinConfig> activeVeins) {
        ChunkPos centerChunk = player.chunkPosition();
        long seed = level.getSeed();

        Map<OreVeinConfig, ChunkPos> foundVeins = ScannerLarge.scanArea(
                level,
                seed,
                dim,
                centerChunk,
                getLargeChunkRadius(),
                activeVeins
        );

        foundVeins.entrySet().stream()
                .map(entry -> {
                    ChunkPos chunk = entry.getValue();
                    int distance = (int) Math.hypot(
                            chunk.getMiddleBlockX() - player.blockPosition().getX(),
                            chunk.getMiddleBlockZ() - player.blockPosition().getZ()
                    );
                    return Map.entry(entry.getKey(), distance);
                })
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> {
                    player.displayClientMessage(
                            MIPP.text().prospectorFoundVein(
                                    Component.translatable("text.mipp.veins.%s".formatted(entry.getKey().id().getPath())),
                                    Component.literal(String.valueOf(entry.getValue()))),
                            false
                    );
                });
    }

    private void scanLocal(ServerLevel level, Player player, InteractionHand usedHand, List<OreVeinConfig> activeVeins) {
        ScannerLocal scanner = new ScannerLocal(level, player, getLocalChunkRadius());
        scanner.collectChunks(activeVeins);

        ServerTickListener.startScan(player, scanner, usedHand);
    }

    // This is adapted code originally part of GregTech:CEu, hosted at https://github.com/GregTechCEu/GregTech-Modern
    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new ModularScreen(MIPP.ID, mainPanel);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager panelSyncManager, UISettings settings) {
        int mapSize = (getLocalChunkRadius() * 2 + 1) * 16 + 1;

        StringValue searchValue = new StringValue("");
        DynamicWidget<?> searchListWidget = new DynamicWidget<>();

        ProspectorMapWidget mapWidget = getMapWidget(data.getPlayer(), panelSyncManager, searchValue, searchListWidget);

        return ModularPanel.defaultPanel("prospector_scanner", mapSize + 162, mapSize+23)
                .margin(4)
                .child(new ToggleButton()
                        .size(18)
                        .top(4).leftRelAnchor(0f, 1f)
                        .decoration()
                        .value(new BoolValue.Dynamic(mapWidget::isDarkMode,
                                mapWidget::setDarkMode))
                        .excludeAreaInRecipeViewer())
                .child(Flow.row()
                        .margin(6)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(mapWidget
                                .size(mapSize + 11, mapSize + 11)
                                .margin(0, 6, 0, 0)
                        )
                        .child(Flow.col()
                                .coverChildrenWidth(136)
                                .top(0).right(0)
                                .child(new TextFieldWidget()
                                        .value(searchValue)
                                        .widthRel(1f).height(16)
                                        .margin(0, 0, 0, 3)
                                        .autoUpdateOnChange(true))
                                .child(Flow.col()
                                        .coverChildrenWidth(136)
                                        .expanded()
                                        .widthRel(1f)
                                        .background(MapHelper.BACKGROUND_INVERSE)
                                        .padding(0, 0, 3, 3)
                                        .child(
                                                searchListWidget
                                                        .margin(4, 0, 0, 0)
                                                        .expanded()
                                                        .widthRel(1f)
                                        )
                                )
                        )
                );
    }

    @SuppressWarnings("unchecked")
    private ProspectorMapWidget getMapWidget(Player player, PanelSyncManager panelSyncManager, StringValue searchValue, DynamicWidget<?> searchListWidget) {
        Set<BlockState>[] rawFoundOres = ServerTickListener.getFoundOres(player);
        Set<BlockState> rawUniqueOres = ServerTickListener.getUniqueOres(player);

        Set<BlockState>[] foundOres =
                (rawFoundOres != null) ? rawFoundOres : (Set<BlockState>[]) new Set[0];
        Set<BlockState> uniqueOres =
                (rawUniqueOres != null) ? rawUniqueOres : Collections.emptySet();

        return new ProspectorMapWidget(
                getLocalChunkRadius(),
                foundOres,
                uniqueOres,
                searchValue,
                searchListWidget,
                panelSyncManager
        );
    }
}
