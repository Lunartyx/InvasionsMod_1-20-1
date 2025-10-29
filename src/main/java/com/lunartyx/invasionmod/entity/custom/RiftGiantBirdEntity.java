package com.lunartyx.invasionmod.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.world.World;

/**
 * The giant bird is a bulkier variant that soaks additional damage and hits
 * harder. Behaviour mirrors {@link RiftBirdEntity}; only stats differ.
 */
public class RiftGiantBirdEntity extends RiftBirdEntity {
    public RiftGiantBirdEntity(EntityType<? extends RiftGiantBirdEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 12;
    }

    public static DefaultAttributeContainer.Builder createGiantBirdAttributes() {
        return RiftBirdEntity.createBirdAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 28.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7D);
    }
}
