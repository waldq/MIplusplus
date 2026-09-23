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
import dev.waldq.mipp.utils.OreVeinDataMapUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLEnvironment;

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
            PanelSyncManager panelSyncManager
    ) {
        this.chunkRadius = chunkRadius;
        this.foundOres = foundOres;
        this.uniqueOres = uniqueOres;

        this.searchValue = searchValue;
        this.dynamicHandler = createListSyncHandler();
        searchListWidget.clientOnlyHandler(this.dynamicHandler);

        if (FMLEnvironment.dist.isClient()) {
            this.mapBack = new ProspectorMapBackground(this);
            background(MapHelper.BACKGROUND_INVERSE,
                    this.mapBack);
            size(this.mapBack.getImageWidth(), this.mapBack.getImageHeight());
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

    public Set<BlockState>[] getFoundOres() { return foundOres; }

    public int getChunkRadius() { return this.chunkRadius; }

    public boolean isDarkMode() { return darkMode; }

    public BlockState getSelected() { return selectedBlockState; }

}
