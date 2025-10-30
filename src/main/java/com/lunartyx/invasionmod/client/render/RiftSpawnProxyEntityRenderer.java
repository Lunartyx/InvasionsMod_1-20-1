package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftSpawnProxyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

public class RiftSpawnProxyEntityRenderer extends MobEntityRenderer<RiftSpawnProxyEntity, BipedEntityModel<RiftSpawnProxyEntity>> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/zombie/zombie.png");

    public RiftSpawnProxyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, castModel(new ZombieEntityModel(context.getPart(EntityModelLayers.ZOMBIE))), 0.5F);
    }

    @Override
    public Identifier getTexture(RiftSpawnProxyEntity entity) {
        return TEXTURE;
    }

    @SuppressWarnings("unchecked")
    private static BipedEntityModel<RiftSpawnProxyEntity> castModel(BipedEntityModel<?> model) {
        return (BipedEntityModel<RiftSpawnProxyEntity>) model;
    }
}
