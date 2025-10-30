package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.mixin.TntEntityAccessor;
import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RiftPrimedTntEntity extends TntEntity {

    public RiftPrimedTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
    }

    public RiftPrimedTntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        // WICHTIG: eigener EntityType
        this(ModEntityTypes.RIFT_PRIMED_TNT, world);

        this.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
        this.setFuse(40);

        // Owner ins private Vanilla-Feld schreiben (über Accessor-Mixin)
        ((TntEntityAccessor) (Object) this).invasionmod$setCausingEntity(igniter);
    }
}
