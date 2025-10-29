package com.lunartyx.invasionmod.client.render;

import com.lunartyx.invasionmod.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

public class RiftWolfEntityRenderer extends WolfEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/entity/rift_wolf.png");

    public RiftWolfEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WolfEntity wolfEntity) {
        return TEXTURE;
    }
}
