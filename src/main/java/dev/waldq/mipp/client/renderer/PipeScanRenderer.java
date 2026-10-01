package dev.waldq.mipp.client.renderer;

import aztech.modern_industrialization.client.util.RenderHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPClient;
import dev.waldq.mipp.client.ClientPipeScan;
import dev.waldq.mipp.client.ThroughputColor;
import dev.waldq.mipp.client.helper.CubeOverlayRenderHelper;
import dev.waldq.mipp.item.pipenetworkanalyzer.PipeNetworkAnalyzerItem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = MIPP.ID, value = Dist.CLIENT)
public final class PipeScanRenderer {

    private static final MultiBufferSource.BufferSource IMMEDIATE = MultiBufferSource.immediate(new ByteBufferBuilder(256));
    private static final VertexSorting REVERSE_SORTING = RenderHelper.reverseVertexSorting(VertexSorting.DISTANCE_TO_ORIGIN);
    private static final float LINE_HEIGHT = 10f;

    private static final double CAMERA_MOVE_THRESHOLD_SQ = 4.0 * 4.0; // 4 blocks

    private record Visible(ClientPipeScan.Label label, double distSq) {}

    private static List<Visible> cachedVisible = List.of();
    private static Vec3 cachedCamPos = Vec3.ZERO;
    private static List<ClientPipeScan.Label> cachedSourceLabels = null;
    private static double cachedDistance = -1;
    private static int cachedMaxLabels = -1;

    private static boolean shouldRender(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        if (ClientPipeScan.labels.isEmpty()) return false;
        if (!PipeNetworkAnalyzerItem.isHolding(mc.player)) return false;
        return mc.level.dimension().equals(ClientPipeScan.dimension);
    }

    private static List<Visible> collect(Vec3 camPos) {
        List<ClientPipeScan.Label> labels = ClientPipeScan.labels;
        double distance = PipeNetworkAnalyzerItem.scanRenderDistance;
        int maxLabels = 200;

        boolean dataChanged = labels != cachedSourceLabels;
        boolean settingsChanged = distance != cachedDistance || maxLabels != cachedMaxLabels;
        boolean cameraMoved = camPos.distanceToSqr(cachedCamPos) > CAMERA_MOVE_THRESHOLD_SQ;

        if (!dataChanged && !settingsChanged && !cameraMoved) {
            return cachedVisible;
        }

        double maxDistSq = distance * distance;
        List<Visible> result = new ArrayList<>();
        for (var label : labels) {
            double dx = label.pos().getX() + 0.5 - camPos.x;
            double dy = label.pos().getY() + 0.5 - camPos.y;
            double dz = label.pos().getZ() + 0.5 - camPos.z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d <= maxDistSq) result.add(new Visible(label, d));
        }
        if (result.size() > maxLabels) {
            result.sort(Comparator.comparingDouble(Visible::distSq));
            result = result.subList(0, maxLabels);
        }

        cachedVisible = result;
        cachedCamPos = camPos;
        cachedSourceLabels = labels;
        cachedDistance = distance;
        cachedMaxLabels = maxLabels;
        return result;
    }

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
        RenderSystem.clear(256, Minecraft.ON_OSX);

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
            matrices.translate(0.002, 0.002, 0.002);
            matrices.scale(0.996f, 0.996f, 0.996f);

            var renderType = MIPPClient.config().pipeNetworkAnalyzer().useFullBlockOverlay() ? CubeOverlayRenderHelper.CUBE_OVERLAY : CubeOverlayRenderHelper.CUBE_OUTLINE;

            CubeOverlayRenderHelper.render(matrices, IMMEDIATE, renderType, color.rf(), color.gf(), color.bf(), color.af(), OverlayTexture.NO_OVERLAY);

            matrices.popPose();
        }

        matrices.popPose();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix(), REVERSE_SORTING);
        IMMEDIATE.endBatch();
        RenderSystem.restoreProjectionMatrix();
    }

    private static void renderText(RenderLevelStageEvent event, Minecraft mc, Camera camera, Vec3 camPos, List<Visible> visible) {
        PoseStack ps = event.getPoseStack();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        ps.pushPose();
        ps.mulPose(event.getModelViewMatrix());

        for (Visible v : visible) {
            var pos = v.label().pos();
            var line = v.label().line();
            int color = 0xFFFFFFFF;

            ps.pushPose();
            double x = pos.getX() + 0.5 - camPos.x;
            double y = pos.getY() + 0.5 - camPos.y;
            double z = pos.getZ() + 0.5 - camPos.z;
            ps.translate(x, y, z);
            ps.mulPose(camera.rotation());
            ps.scale(0.025f, -0.025f, 0.025f);

            float w = font.width(line);
            font.drawInBatch(
                    line,
                    -w / 2f,
                    -LINE_HEIGHT / 2f,
                    color,
                    false,
                    ps.last().pose(),
                    buffers,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);

            ps.popPose();
        }

        ps.popPose();
        buffers.endBatch();
    }

    @SubscribeEvent
    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPipeScan.clear();
        cachedVisible = List.of();
        cachedSourceLabels = null;
    }
}