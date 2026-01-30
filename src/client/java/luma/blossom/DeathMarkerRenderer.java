package luma.blossom;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DeathMarkerRenderer {
    private static final String SKULL_EMOJI = "☠";

    public static void render(WorldRenderContext context) {
        DeathMarkerManager manager = DeathMarkerManager.getInstance();
        if (!manager.hasMarker()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }

        DeathMarkerData marker = manager.getCurrentMarker();

        if (!player.level().dimension().equals(marker.getDimension())) {
            return;
        }

        DeathMarkerConfig config = DeathMarkerClient.getConfig();
        int skullColor = 0xFF000000 | config.skullColor;
        int timerColor = 0xFF000000 | config.timerColor;
        int distanceColor = 0xFF000000 | config.distanceColor;

        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        BlockPos markerPos = marker.getPosition();

        double worldX = markerPos.getX() + 0.5;
        double worldY = markerPos.getY() + 2.5;
        double worldZ = markerPos.getZ() + 0.5;

        double distance = marker.getDistanceTo(player.getX(), player.getY(), player.getZ());
        int blockDistance = (int) Math.round(distance);
        String timeRemaining = marker.getFormattedTimeRemaining();

        PoseStack poseStack = context.matrixStack();
        poseStack.pushPose();

        poseStack.translate(
            worldX - cameraPos.x,
            worldY - cameraPos.y,
            worldZ - cameraPos.z
        );

        poseStack.mulPose(camera.rotation());

        int shrinkDist = config.shrinkDistance;
        float baseScale = 0.3f;
        float distanceScale;
        if (distance < shrinkDist) {
            distanceScale = (float) (0.3 + 0.7 * (distance / shrinkDist));
        } else {
            distanceScale = (float) (distance / shrinkDist);
        }
        float scale = baseScale * distanceScale;
        poseStack.scale(-scale, -scale, scale);

        Font font = client.font;
        MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.disableDepthTest();

        int skullWidth = font.width(SKULL_EMOJI);
        font.drawInBatch(
            SKULL_EMOJI,
            (float) -skullWidth / 2,
            -font.lineHeight - 4,
            skullColor,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0,
            15728880
        );

        String timeText = timeRemaining;
        int timeWidth = font.width(timeText);
        font.drawInBatch(
            timeText,
            (float) -timeWidth / 2,
            0,
            timerColor,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0,
            15728880
        );

        String distanceText = blockDistance + "m";
        int distWidth = font.width(distanceText);
        font.drawInBatch(
            distanceText,
            (float) -distWidth / 2,
            font.lineHeight,
            distanceColor,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0,
            15728880
        );

        bufferSource.endBatch();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
            GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );

        font.drawInBatch(
            timeText,
            (float) -timeWidth / 2,
            0,
            0xFFFFFFFF,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0,
            15728880
        );

        font.drawInBatch(
            distanceText,
            (float) -distWidth / 2,
            font.lineHeight,
            0xFFFFFFFF,
            false,
            matrix,
            bufferSource,
            Font.DisplayMode.SEE_THROUGH,
            0,
            15728880
        );

        bufferSource.endBatch();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }
}
