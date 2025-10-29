package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftSpawnProxyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

@SuppressWarnings("rawtypes")
public class RiftSpawnProxyEntityRenderer extends MobEntityRenderer<RiftSpawnProxyEntity, ZombieEntityModel> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/zombie/zombie.png");

    public RiftSpawnProxyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel(context.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public Identifier getTexture(RiftSpawnProxyEntity entity) {
        return TEXTURE;
    }

    private static EntityModel<RiftSpawnProxyEntity> createModel(EntityRendererFactory.Context context) {
        ZombieEntityModel model = new ZombieEntityModel(context.getPart(EntityModelLayers.ZOMBIE));
        @SuppressWarnings("unchecked")
        EntityModel<RiftSpawnProxyEntity> casted = (EntityModel<RiftSpawnProxyEntity>) (EntityModel<?>) model;
        return casted;
    }
}
