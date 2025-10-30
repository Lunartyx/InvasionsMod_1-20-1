package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.entity.custom.RiftPigEngineerEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.util.Identifier;

public class RiftPigEngineerEntityRenderer
        extends MobEntityRenderer<RiftPigEngineerEntity, PiglinEntityModel<RiftPigEngineerEntity>> {

    private static final Identifier TEXTURE =
            new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_pig_engineer.png");

    public RiftPigEngineerEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new PiglinEntityModel<>(context.getPart(EntityModelLayers.ZOMBIFIED_PIGLIN)),
                0.5f
        );
    }

    @Override
    public Identifier getTexture(RiftPigEngineerEntity entity) {
        return TEXTURE;
    }
}
