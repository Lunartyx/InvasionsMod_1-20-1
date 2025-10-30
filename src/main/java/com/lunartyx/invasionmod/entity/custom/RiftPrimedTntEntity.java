package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.registry.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;

/**
 * Wrapper over the vanilla TNT entity so invasion engineers can prime custom
 * explosives without relying on block updates.
 */
public class RiftPrimedTntEntity extends TntEntity {
    public RiftPrimedTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
    }

    public RiftPrimedTntEntity(World world, double x, double y, double z, LivingEntity igniter) {
        this(ModEntityTypes.RIFT_PRIMED_TNT, world);
        this.setFuse(40);
        this.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
        this.setOwner(igniter);
    }
}
