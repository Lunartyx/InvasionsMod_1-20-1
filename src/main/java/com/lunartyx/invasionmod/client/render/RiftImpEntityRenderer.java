package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VexEntityRenderer;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.util.Identifier;

public class RiftImpEntityRenderer extends VexEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_imp.png");

    public RiftImpEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(VexEntity vexEntity) {
        return TEXTURE;
    }
}
