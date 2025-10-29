package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SilverfishEntityRenderer;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.Identifier;

public class RiftBurrowerEntityRenderer extends SilverfishEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_burrower.png");

    public RiftBurrowerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(SilverfishEntity silverfishEntity) {
        return TEXTURE;
    }
}
