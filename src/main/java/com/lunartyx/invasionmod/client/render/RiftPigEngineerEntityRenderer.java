package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombifiedPiglinEntityRenderer;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.util.Identifier;

public class RiftPigEngineerEntityRenderer extends ZombifiedPiglinEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_pig_engineer.png");

    public RiftPigEngineerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ZombifiedPiglinEntity zombifiedPiglinEntity) {
        return TEXTURE;
    }
}
