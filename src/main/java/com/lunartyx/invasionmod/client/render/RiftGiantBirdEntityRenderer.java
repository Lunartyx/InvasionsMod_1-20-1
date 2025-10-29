package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftGiantBirdEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.util.Identifier;

public class RiftGiantBirdEntityRenderer extends MobEntityRenderer<RiftGiantBirdEntity, PhantomEntityModel<RiftGiantBirdEntity>> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/phantom.png");

    public RiftGiantBirdEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PhantomEntityModel<>(context.getPart(EntityModelLayers.PHANTOM)), 0.9F);
    }

    @Override
    public Identifier getTexture(RiftGiantBirdEntity entity) {
        return TEXTURE;
    }
}
