package dev.waldq.mipp.item.pospector.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.GradientUtil;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.waldq.mipp.utils.OreVeinDataMapUtils;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.Set;

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
@OnlyIn(Dist.CLIENT)
public class ProspectorMapBackground extends AbstractTexture implements IDrawable {

    private final ProspectorMapWidget mapWidget;

    private final int imageHeight;
    private final int imageWidth;

    private final Set<BlockState>[] foundOres;
    private boolean isDirty = true;

    public ProspectorMapBackground(ProspectorMapWidget mapWidget) {
        this.mapWidget = mapWidget;
        this.foundOres = mapWidget.getFoundOres();
        int chunkDiameter = mapWidget.getChunkRadius() * 2 + 1;
        this.imageHeight = this.imageWidth = chunkDiameter * 16 + 1;
    }

    private NativeImage getImage() {
        NativeImage image = new NativeImage(this.imageWidth, this.imageHeight, false);

        for (int x = 0; x < this.imageWidth; x++) {
            for (int z = 0; z < this.imageHeight; z++) {
                boolean edge = x == this.imageWidth - 1 || z == this.imageHeight - 1;

                boolean drewColor = false;

                if (!edge) {
                    Set<BlockState> states = this.foundOres[x + z * (this.imageHeight - 1)];
                    if (states != null) {
                        for (BlockState state : states) {
                            BlockState mainState = OreVeinDataMapUtils.getMainBlock(state);
                            if (mapWidget.getSelected() == null
                                    || mapWidget.getSelected().equals(mainState)) {
                                int color = OreVeinDataMapUtils.getCachedOreColor(state);
                                image.setPixelRGBA(x, z, GradientUtil.argbToAbgr(color) | 0xFF000000);

                                drewColor = true;
                                break;
                            }
                        }
                    }
                }
                if (!drewColor) {
                    image.setPixelRGBA(x, z, (mapWidget.isDarkMode() ? 0xFF666666 : 0xFFFFFFFF));
                }
                if (x % 16 == 0 || z % 16 == 0) {
                    image.blendPixel(x, z, 0xFF000000);
                }
            }
        }

        return image;
    }

    public void loadToImage() {
        NativeImage image = getImage();
        TextureUtil.prepareImage(this.getId(), image.getWidth(), image.getHeight());
        // the last parameter is actually autoClose, it's named wrong.
        image.upload(0, 0, 0, true);
        this.isDirty = false;
    }

    public int getImageHeight() { return imageHeight; }

    public int getImageWidth() { return imageWidth; }


    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        // getId() generates a new texture ID if it's NOT_ASSIGNED, so we shouldn't use that.
        if (this.id == NOT_ASSIGNED || isDirty) loadToImage();

        int border = 4;

        int mapX = x + border;
        int mapY = y + border;
        int mapWidth = width - border;
        int mapHeight = height - border;

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, this.getId());
        GuiDraw.drawTexture(context.getLastGraphicsPose(), mapX, mapY, mapWidth, mapHeight, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
        RenderSystem.disableBlend();
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {}


}
