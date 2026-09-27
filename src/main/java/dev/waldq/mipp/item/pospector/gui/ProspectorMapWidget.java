package dev.waldq.mipp.item.pospector.gui;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.ScrollingTextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;

import com.google.common.base.Strings;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPComponents;
import dev.waldq.mipp.item.component.BlockTracker;
import dev.waldq.mipp.item.pospector.ProspectorItemBehavior;
import dev.waldq.mipp.utils.OreVeinDataMapUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is adapted code originally part of GregTech:CEu, hosted at https://github.com/GregTechCEu/GregTech-Modern
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */
public class ProspectorMapWidget extends Widget<ProspectorMapWidget> implements Interactable {
    private final Set<BlockState>[] foundOres;
    private final int chunkRadius;
    private final StringValue searchValue;
    private final Set<BlockState> uniqueOres;
    private final DynamicHandler dynamicHandler;

    private final Player player;

    private boolean darkMode = true;
    private BlockState selectedBlockState = null;
    private String lastSearch = "";

    private ProspectorMapBackground mapBack;

    public ProspectorMapWidget(
            int chunkRadius,
            Set<BlockState>[] foundOres,
            Set<BlockState> uniqueOres,
            StringValue searchValue,
            DynamicWidget<?> searchListWidget,
            PanelSyncManager panelSyncManager,
            Player player
    ) {
        this.chunkRadius = chunkRadius;
        this.foundOres = foundOres;
        this.uniqueOres = uniqueOres;

        this.searchValue = searchValue;
        this.dynamicHandler = createListSyncHandler();
        searchListWidget.clientOnlyHandler(this.dynamicHandler);

        this.player = player;

        if (FMLEnvironment.dist.isClient()) {
            this.mapBack = new ProspectorMapBackground(this);
            background(MapHelper.BACKGROUND_INVERSE,
                    this.mapBack);
            size(this.mapBack.getImageWidth() + this.mapBack.getBorder() * 2,
                    this.mapBack.getImageHeight() + this.mapBack.getBorder() * 2);

            tooltipAutoUpdate(true);
            tooltipDynamic(tooltip -> {
                tooltip.clearText();

                Map<BlockState, Integer> oresWithCount = getHoveredChunkOresWithCount();
                if (oresWithCount.isEmpty()) return;

                oresWithCount.entrySet().stream()
                        .sorted(Map.Entry.<BlockState, Integer>comparingByValue().reversed())
                        .forEach(entry -> {
                            BlockState blockState = entry.getKey();
                            int count = entry.getValue();

                            Component name = blockState.getBlock().getName();

                            Component line = Component.empty()
                                    .append(name)
                                    .append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY));

                            tooltip.addLine(line);
                        });
            });
        }
    }

    public Set<BlockState> getGroupedMainBlocks() {
        Set<BlockState> mainBlocks = new LinkedHashSet<>();
        for (BlockState state : this.uniqueOres) {
            BlockState main = OreVeinDataMapUtils.getMainBlock(state);
            if (main != null) {
                mainBlocks.add(main);
            }
        }
        return mainBlocks;
    }

    private DynamicHandler createListSyncHandler() {
        return new DynamicHandler()
                .widgetProvider(() -> {
                    if (FMLEnvironment.dist.isClient() && this.mapBack != null) {
                        this.mapBack.loadToImage();
                    }

                    return new ListWidget<>()
                            .collapseDisabledChildren()
                            .expanded()
                            .sizeRel(1f)
                            .disableThemeBackground(true)
                            .disableHoverThemeBackground(true)
                            .onUpdateListener(list -> {
                                String current = searchValue.getStringValue();
                                if (!Objects.equals(current, this.lastSearch)) {
                                    this.lastSearch = current;
                                    list.getScrollData().scrollTo(list.getScrollArea(), 0);
                                }
                            })
                            .child(0, new ButtonWidget<>().widgetTheme(IThemeApi.TOGGLE_BUTTON)
                                    .widthRel(1f).height(18)
                                    .disableThemeBackground(true)
                                    .onMousePressed((context, button) -> {
                                        if (button == 0) {
                                            this.setSelected(null, FMLEnvironment.dist.isClient());
                                            return true;
                                        }
                                        return false;
                                    })
                                    .setEnabledIf(w -> {
                                        String searched = searchValue.getStringValue();
                                        if (Strings.isNullOrEmpty(searched)) {
                                            return true;
                                        } else {
                                            return MIPP.text().prospectorAllRes()
                                                    .getString()
                                                    .toLowerCase()
                                                    .contains(searched.toLowerCase());
                                        }
                                    })
                                    .child(Flow.row()
                                            .sizeRel(1f)
                                            .padding(4, 0)
                                            .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                            .child(new ScrollingTextWidget(Text.of(MIPP.text().prospectorAllRes()))
                                                    .textAlign(Alignment.CenterLeft)
                                                    .verticalCenter()
                                                    .expanded()
                                                    .margin(1, 0)
                                                    .left(20).right(2)
                                            )
                                    )
                            )
                            .children(this.getGroupedMainBlocks(), item -> {
                                Component description = item.getBlock().getName();

                                BoolValue.Dynamic selected = new BoolValue.Dynamic(
                                        () -> this.getSelected() == item,
                                        v -> this.setSelected(v ? item : null, FMLEnvironment.dist.isClient()));

                                return new ToggleButton().widgetTheme(IThemeApi.TOGGLE_BUTTON)
                                        .value(selected)
                                        .widthRel(1f).height(18)
                                        .disableThemeBackground(true)
                                        .setEnabledIf(w -> {
                                            String searched = searchValue.getStringValue();
                                            if (Strings.isNullOrEmpty(searched)) {
                                                return true;
                                            } else {
                                                return description.getString().toLowerCase()
                                                        .contains(searched.toLowerCase());
                                            }
                                        })
                                        .child(Flow.row()
                                                .sizeRel(1f)
                                                .padding(4, 0)
                                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                .child(new ScrollingTextWidget(Text.of(description))
                                                                .textAlign(Alignment.CenterLeft)
                                                                .verticalCenter()
                                                                .expanded()
                                                                .margin(1, 0)
                                                                .left(20).right(2)
                                                                .invisible()
                                                ));
                            });
                });
    }

    public void setSelected(BlockState state, boolean isClient) {
        if (!Objects.equals(this.selectedBlockState, state)) {
            this.selectedBlockState = state;

            if (isClient && this.mapBack != null) {
                this.mapBack.loadToImage();
            }
        }
    }

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode != darkMode) {
            this.darkMode = darkMode;
            if (this.mapBack != null) {
                this.mapBack.loadToImage();
            }
        }
    }

    private Map<BlockState, Integer> getHoveredChunkOresWithCount() {
        if (this.foundOres == null || this.mapBack == null) return Map.of();

        int border = this.mapBack.getBorder();

        int renderWidth = getArea().getWidth() - border * 2;
        int renderHeight = getArea().getHeight() - border * 2;

        if (renderWidth <= 0 || renderHeight <= 0) return Map.of();

        int relX = getContext().getMouseX() - border;
        int relZ = getContext().getMouseY() - border;

        if (relX < 0 || relZ < 0 || relX >= renderWidth || relZ >= renderHeight) {
            return Map.of();
        }

        int mapWidth = (this.chunkRadius * 2 + 1) * 16;

        int blockX = (int) ((double) relX / renderWidth * mapWidth);
        int blockZ = (int) ((double) relZ / renderHeight * mapWidth);

        blockX = Math.clamp(blockX, 0, mapWidth - 1);
        blockZ = Math.clamp(blockZ, 0, mapWidth - 1);

        int chunkX = blockX / 16;
        int chunkZ = blockZ / 16;

        Map<BlockState, Integer> oreCounts = new LinkedHashMap<>();

        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int index = x + z * mapWidth;
                if (index >= 0 && index < this.foundOres.length) {
                    Set<BlockState> states = this.foundOres[index];
                    if (states != null) {
                        for (BlockState state : states) {
                            BlockState mainState = OreVeinDataMapUtils.getMainBlock(state);
                            if (mainState == null) mainState = state;

                            if (this.selectedBlockState == null || Objects.equals(this.selectedBlockState, mainState)) {
                                oreCounts.merge(mainState, 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }

        return oreCounts;
    }

    public Set<BlockState>[] getFoundOres() { return foundOres; }

    public int getChunkRadius() { return this.chunkRadius; }

    public boolean isDarkMode() { return darkMode; }

    public BlockState getSelected() { return selectedBlockState; }

}
