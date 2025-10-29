package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftTrapEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RiftTrapEntityRenderer extends EntityRenderer<RiftTrapEntity> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/block/iron_bars.png");

    public RiftTrapEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(RiftTrapEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(0.9F, 0.4F, 0.9F);
        matrices.translate(0.0D, 0.5D, 0.0D);
        this.dispatcher.getBlockRenderManager().renderBlockAsEntity(Blocks.IRON_BARS.getDefaultState(), matrices, vertexConsumers, light, 0);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(RiftTrapEntity entity) {
        return TEXTURE;
    }
}
