package dev.waldq.mipp.client.renderer;

import aztech.modern_industrialization.client.util.RenderHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.client.ClientPipeScan;
import dev.waldq.mipp.client.ThroughputColor;
import dev.waldq.mipp.item.analyzer.AnalyzerItem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.swedz.tesseract.neoforge.helper.CubeOverlayRenderHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = MIPP.ID, value = Dist.CLIENT)
public final class PipeScanRenderer {
    private static final double scanRenderDistance = 32.0;
    private static final int scanMaxLabels = 100;

    private static final MultiBufferSource.BufferSource IMMEDIATE = MultiBufferSource.immediate(new ByteBufferBuilder(256));
    private static final VertexSorting REVERSE_SORTING = RenderHelper.reverseVertexSorting(VertexSorting.DISTANCE_TO_ORIGIN);
    private static final float LINE_HEIGHT = 10f;

    private record Visible(ClientPipeScan.Label label, double distSq) {}

    private static boolean shouldRender(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        if (ClientPipeScan.labels.isEmpty()) return false;
        if (!AnalyzerItem.isHolding(mc.player)) return false;
        return ClientPipeScan.dimension != null && mc.level.dimension().equals(ClientPipeScan.dimension);
    }

    private static List<Visible> collect(Vec3 camPos) {
        double maxDistSq = scanRenderDistance * scanRenderDistance;
        List<Visible> result = new ArrayList<>();
        for (var label : ClientPipeScan.labels) {
            double dx = label.pos().getX() + 0.5 - camPos.x;
            double dy = label.pos().getY() + 0.5 - camPos.y;
            double dz = label.pos().getZ() + 0.5 - camPos.z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d <= maxDistSq) result.add(new Visible(label, d));
        }
        if (result.size() > scanMaxLabels) {
            result.sort(Comparator.comparingDouble(Visible::distSq));
            result = result.subList(0, scanMaxLabels);
        }
        return result;
    }

    /** Fires last in the level-rendering pass. Drawing both passes here, in order, guarantees
     *  text is painted after the colored cubes regardless of stage-firing assumptions. */
    @SubscribeEvent
    private static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        if (!shouldRender(mc)) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        List<Visible> visible = collect(camPos);
        if (visible.isEmpty()) return;

        renderOverlays(event, camera, camPos, visible);
        renderText(event, mc, camera, camPos, visible);
    }

    private static void renderOverlays(RenderLevelStageEvent event, Camera camera, Vec3 camPos, List<Visible> visible) {
        // Clear depth so the cubes aren't occluded by walls drawn earlier this frame.
        RenderSystem.clear(256 /* GL_DEPTH_BUFFER_BIT */, Minecraft.ON_OSX);

        PoseStack matrices = event.getPoseStack();
        matrices.pushPose();
        matrices.mulPose(event.getModelViewMatrix());

        for (Visible v : visible) {
            var pos = v.label().pos();
            ThroughputColor color = v.label().color();

            matrices.pushPose();
            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;
            matrices.translate(x, y, z);
            // Shrink slightly (not grow) so touching neighbor cubes get a small gap
            // instead of an overlapping, z-fighting seam.
            matrices.translate(0.002, 0.002, 0.002);
            matrices.scale(0.996f, 0.996f, 0.996f);

            CubeOverlayRenderHelper.render(matrices, IMMEDIATE, color.rf(), color.gf(), color.bf(), color.af(), OverlayTexture.NO_OVERLAY);

            matrices.popPose();
        }

        matrices.popPose();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix(), REVERSE_SORTING);
        IMMEDIATE.endBatch(); // flushes the cubes to the GPU now, before text is drawn below
        RenderSystem.restoreProjectionMatrix();
    }

    private static void renderText(RenderLevelStageEvent event, Minecraft mc, Camera camera, Vec3 camPos, List<Visible> visible) {
        PoseStack ps = event.getPoseStack();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        ps.pushPose();
        ps.mulPose(event.getModelViewMatrix()); // AFTER_LEVEL needs this reapplied, unlike AFTER_PARTICLES

        for (Visible v : visible) {
            var pos = v.label().pos();
            Component line = v.label().line();
            int color = v.label().color().getHex();

            ps.pushPose();
            double x = pos.getX() + 0.5 - camPos.x;
            double y = pos.getY() + 0.5 - camPos.y;
            double z = pos.getZ() + 0.5 - camPos.z;
            ps.translate(x, y, z);
            ps.mulPose(camera.rotation());          // billboard: face the camera
            ps.scale(0.025f, -0.025f, 0.025f);

            float w = font.width(line);
            font.drawInBatch(line, -w / 2f, -LINE_HEIGHT / 2f, color, false,
                    ps.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);

            ps.popPose();
        }

        ps.popPose();
        buffers.endBatch();
    }

    @SubscribeEvent
    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPipeScan.clear();
    }
}