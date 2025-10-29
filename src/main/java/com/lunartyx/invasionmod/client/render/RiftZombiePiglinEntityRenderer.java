package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.entity.custom.RiftZombiePiglinEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class RiftZombiePiglinEntityRenderer
        extends MobEntityRenderer<RiftZombiePiglinEntity, PiglinEntityModel<RiftZombiePiglinEntity>> {

    private static final Identifier TEXTURE =
            new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_zombie_piglin.png");

    public RiftZombiePiglinEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new PiglinEntityModel<>(context.getPart(EntityModelLayers.ZOMBIFIED_PIGLIN)),
                0.5f // Schattenradius
        );
    }

    @Override
    public Identifier getTexture(RiftZombiePiglinEntity entity) {
        return TEXTURE;
    }
}
