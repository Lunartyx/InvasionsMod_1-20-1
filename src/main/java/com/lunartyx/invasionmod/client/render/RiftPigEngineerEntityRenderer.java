package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.entity.custom.RiftPigEngineerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class RiftPigEngineerEntityRenderer extends MobEntityRenderer<RiftPigEngineerEntity, SinglePartEntityModel<RiftPigEngineerEntity>> {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_pig_engineer.png");

    public RiftPigEngineerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, castModel(new PiglinEntityModel(context.getPart(EntityModelLayers.ZOMBIFIED_PIGLIN))), 0.5F);
    }

    @Override
    public Identifier getTexture(RiftPigEngineerEntity entity) {
        return TEXTURE;
    }

    @SuppressWarnings("unchecked")
    private static SinglePartEntityModel<RiftPigEngineerEntity> castModel(SinglePartEntityModel<?> model) {
        return (SinglePartEntityModel<RiftPigEngineerEntity>) model;
    }
}
