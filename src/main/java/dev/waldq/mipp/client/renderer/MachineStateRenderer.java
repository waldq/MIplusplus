package dev.waldq.mipp.client.renderer;

import aztech.modern_industrialization.client.util.RenderHelper;
import brachy.modularui.utils.Color;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPClient;
import dev.waldq.mipp.client.ClientMachineOverlays;
import dev.waldq.mipp.client.helper.CubeOverlayRenderHelper;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

@EventBusSubscriber(modid = MIPP.ID, value = Dist.CLIENT)
public class MachineStateRenderer {
    private static final MultiBufferSource.BufferSource IMMEDIATE = MultiBufferSource.immediate(new ByteBufferBuilder(256));
    private static final VertexSorting REVERSE_SORTING = RenderHelper.reverseVertexSorting(VertexSorting.DISTANCE_TO_ORIGIN);

    private static final double CAMERA_MOVE_THRESHOLD_SQ = 4.0 * 4.0; // 4 blocks

    private record Visible(ClientMachineOverlays.Overlay overlay, double distSq) {}

    private static List<Visible> cachedVisible = List.of();
    private static Vec3 cachedCamPos = Vec3.ZERO;
    private static Set<ClientMachineOverlays.Overlay> cachedSourceOverlays = null;
    private static double cachedDistance = -1;
    private static int cachedMaxOverlays = -1;

    private static boolean shouldRender(Minecraft mc) {
        if (mc.player == null || mc.level == null) return false;
        if (ClientMachineOverlays.overlays.isEmpty()) return false;
        return mc.level.dimension().equals(ClientMachineOverlays.dimension);
    }

    private static List<Visible> collect(Vec3 camPos) {
        var overlays = ClientMachineOverlays.overlays;
        double distance = 500;
        int maxOverlays = 200; //TODO

        boolean dataChanged = overlays != cachedSourceOverlays;
        boolean cameraMoved = camPos.distanceToSqr(cachedCamPos) > CAMERA_MOVE_THRESHOLD_SQ;

        if (!dataChanged && !cameraMoved) return cachedVisible;

        double maxDistSq = distance * distance;
        List<Visible> result = new ArrayList<>();
        for (var overlay : overlays) {
            double dx = overlay.pos.getX() + 0.5 - camPos.x;
            double dy = overlay.pos.getY() + 0.5 - camPos.y;
            double dz = overlay.pos.getZ() + 0.5 - camPos.z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d <= maxDistSq) result.add(new Visible(overlay, d));
        }
        if (result.size() > maxOverlays) {
            result.sort(Comparator.comparingDouble(Visible::distSq));
            result = result.subList(0, maxOverlays);
        }

        cachedVisible = result;
        cachedCamPos = camPos;
        cachedSourceOverlays = overlays;
        cachedDistance = distance;
        cachedMaxOverlays = maxOverlays;
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

        renderOverlays(event, camPos, visible);
    }

    private static void renderOverlays(RenderLevelStageEvent event, Vec3 camPos, List<Visible> visible) {
        RenderSystem.clear(256, Minecraft.ON_OSX);

        PoseStack matrices = event.getPoseStack();
        matrices.pushPose();
        matrices.mulPose(event.getModelViewMatrix());

        for (Visible v : visible) {
            var pos = v.overlay().pos;
            int color = v.overlay.state.color() & 0x80FFFFFF;
            float af = Color.getAlphaF(color);
            float rf = Color.getRedF(color);
            float gf = Color.getGreenF(color);
            float bf = Color.getBlueF(color);

            matrices.pushPose();
            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;
            matrices.translate(x, y, z);
            matrices.translate(0.002, 0.002, 0.002);
            matrices.scale(0.996f, 0.996f, 0.996f);

            var renderType = MIPPClient.config().debugJournal().useFullBlockOverlay() ? CubeOverlayRenderHelper.CUBE_OVERLAY : CubeOverlayRenderHelper.CUBE_OUTLINE;

            CubeOverlayRenderHelper.render(matrices, IMMEDIATE, renderType, rf, gf, bf, af, OverlayTexture.NO_OVERLAY);

            matrices.popPose();
        }

        matrices.popPose();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(RenderSystem.getProjectionMatrix(), REVERSE_SORTING);
        IMMEDIATE.endBatch();
        RenderSystem.restoreProjectionMatrix();
    }

    @SubscribeEvent
    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientMachineOverlays.removeAll();
        cachedVisible = List.of();
        cachedSourceOverlays = null;
    }
}
