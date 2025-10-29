package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftBirdEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.util.Identifier;

@SuppressWarnings("rawtypes")
public class RiftBirdEntityRenderer extends MobEntityRenderer<RiftBirdEntity, ParrotEntityModel> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/parrot/parrot_blue.png");

    public RiftBirdEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ParrotEntityModel(context.getPart(EntityModelLayers.PARROT)), 0.4F);
    }

    @Override
    public Identifier getTexture(RiftBirdEntity entity) {
        return TEXTURE;
    }

    private static EntityModel<RiftBirdEntity> createModel(EntityRendererFactory.Context context) {
        ParrotEntityModel model = new ParrotEntityModel(context.getPart(EntityModelLayers.PARROT));
        @SuppressWarnings("unchecked")
        EntityModel<RiftBirdEntity> casted = (EntityModel<RiftBirdEntity>) (EntityModel<?>) model;
        return casted;
    }
}
