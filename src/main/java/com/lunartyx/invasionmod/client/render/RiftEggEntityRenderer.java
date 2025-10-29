package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.entity.custom.RiftEggEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.util.Identifier;

public class RiftEggEntityRenderer extends MobEntityRenderer<RiftEggEntity, SlimeEntityModel<RiftEggEntity>> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/slime/slime.png");

    public RiftEggEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SlimeEntityModel<>(context.getPart(EntityModelLayers.SLIME)), 0.25F);
    }

    @Override
    public Identifier getTexture(RiftEggEntity entity) {
        return TEXTURE;
    }
}
