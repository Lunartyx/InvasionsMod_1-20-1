package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.world.NightSpawnManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Lightweight proxy that immediately spawns a selection of night mobs, mirroring
 * the legacy mob spawner entity that appeared in special waves.
 */
public class RiftSpawnProxyEntity extends HostileEntity {
    public RiftSpawnProxyEntity(EntityType<? extends RiftSpawnProxyEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    @Override
    protected void initGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            NightSpawnManager.handleProxySpawn((ServerWorld) this.getWorld(), this.getBlockPos(), this.random);
            discard();
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
