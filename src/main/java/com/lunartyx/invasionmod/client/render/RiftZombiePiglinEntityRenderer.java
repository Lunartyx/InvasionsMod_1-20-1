package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombifiedPiglinEntityRenderer;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.util.Identifier;

public class RiftZombiePiglinEntityRenderer extends ZombifiedPiglinEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_zombie_piglin.png");

    public RiftZombiePiglinEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ZombifiedPiglinEntity zombifiedPiglinEntity) {
        return TEXTURE;
    }
}
