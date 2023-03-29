package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(PoseStack poseStack, Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        poseStack.pushPose();
        poseStack.translate(x, y, 350);
        poseStack.scale(1, -1, 1);
        poseStack.scale((float)scale, (float)scale, (float)scale);
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();
        Lighting.setupForFlatItems();

        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));

        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);

        poseStack.translate(-0.5, -0.5, -0.5);
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, true);
        renderModel(model, state, poseStack, bufferSource.getBuffer(renderType), renderType);

        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    private static void renderModel(BakedModel model, BlockState state, PoseStack poseStack, VertexConsumer buffer, RenderType renderType){
        RandomSource random = RandomSource.create();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(state, direction, random));
        }

        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(state, null, random));
    }

    private static void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads){
        PoseStack.Pose matrix = poseStack.last();

        for(BakedQuad bakedquad : quads)
            buffer.putBulkData(matrix, bakedquad, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
