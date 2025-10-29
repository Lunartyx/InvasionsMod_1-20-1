package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftBoltEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RiftBoltEntityRenderer extends EntityRenderer<RiftBoltEntity> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/lightning.png");

    public RiftBoltEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(RiftBoltEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // Visual handled via particles in the entity tick; nothing to render here.
    }

    @Override
    public Identifier getTexture(RiftBoltEntity entity) {
        return TEXTURE;
    }
}
