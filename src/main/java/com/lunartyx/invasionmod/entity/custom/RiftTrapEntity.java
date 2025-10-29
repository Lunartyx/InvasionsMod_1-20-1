package com.lunartyx.invasionmod.entity.custom;

import com.lunartyx.invasionmod.item.custom.ProbeItem;
import com.lunartyx.invasionmod.item.custom.TrapItem;
import com.lunartyx.invasionmod.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Stationary trap entity that burns or snares attackers depending on its type.
 * Engineers deploy traps as support tools around the Nexus.
 */
public class RiftTrapEntity extends Entity {
    public static final int ARM_TIME = 60;

    private static final TrackedData<Integer> TRAP_TYPE =
            DataTracker.registerData(RiftTrapEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> EMPTY =
            DataTracker.registerData(RiftTrapEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int emptyTicks;

    public RiftTrapEntity(EntityType<? extends RiftTrapEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public RiftTrapEntity(EntityType<? extends RiftTrapEntity> type, World world, BlockPos pos, int trapType) {
        this(type, world);
        Vec3d center = Vec3d.ofBottomCenter(pos);
        this.refreshPositionAndAngles(center.x, center.y, center.z, 0.0F, 0.0F);
        setTrapType(trapType);
        setEmpty(false);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TRAP_TYPE, TrapItem.TrapType.EMPTY.id());
        this.dataTracker.startTracking(EMPTY, false);
    }

    @Override
    public void tick() {
        super.tick();

        World world = this.getWorld();
        if (world.isClient) {
            return;
        }

        if (!canStayAtCurrentPosition()) {
            dropEmptyTrap();
            discard();
            return;
        }

        if (isEmpty()) {
            emptyTicks++;
            return;
        }

        emptyTicks = 0;
        if (age <= ARM_TIME) {
            return;
        }

        Box area = getBoundingBox();
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            if (!entity.isAlive()) {
                continue;
            }

            if (applyTrapEffect(entity)) {
                setEmpty(true);
                break;
            }
        }
    }

    private boolean canStayAtCurrentPosition() {
        BlockPos pos = getBlockPos();
        World world = this.getWorld();
        BlockState stateBelow = world.getBlockState(pos.down());
        BlockState current = world.getBlockState(pos);
        if (!(current.isAir() || current.isOf(Blocks.FIRE))) {
            return false;
        }
        return stateBelow.isSideSolidFullSquare(world, pos.down(), Direction.UP);
    }

    public boolean canPlaceAt(BlockPos pos) {
        World world = this.getWorld();
        BlockState target = world.getBlockState(pos);
        if (!target.isAir()) {
            return false;
        }
        BlockState below = world.getBlockState(pos.down());
        if (!below.isSideSolidFullSquare(world, pos.down(), Direction.UP)) {
            return false;
        }

        Box placement = getType().getDimensions().getBoxAt(Vec3d.ofBottomCenter(pos));
        return world.getEntitiesByClass(RiftTrapEntity.class, placement.expand(0.01D),
                other -> other != this && !other.isRemoved()).isEmpty();
    }

    public int getTrapType() {
        return dataTracker.get(TRAP_TYPE);
    }

    public void setTrapType(int trapType) {
        dataTracker.set(TRAP_TYPE, TrapItem.TrapType.byId(trapType).id());
    }

    public boolean isEmpty() {
        return dataTracker.get(EMPTY);
    }

    public void setEmpty(boolean empty) {
        dataTracker.set(EMPTY, empty);
        if (empty) {
            emptyTicks = 0;
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1D, getZ(), 4,
                        0.2D, 0.0D, 0.2D, 0.01D);
            }
        }
    }

    private boolean applyTrapEffect(LivingEntity trigger) {
        TrapItem.TrapType type = TrapItem.TrapType.byId(getTrapType());
        if (type == TrapItem.TrapType.EMPTY) {
            return false;
        }

        switch (type) {
            case RIFT -> {
                float damage = trigger instanceof PlayerEntity ? 12.0F : 38.0F;
                trigger.damage(getDamageSources().magic(), damage);
                Box expanded = getBoundingBox().expand(1.9D, 1.0D, 1.9D);
                List<LivingEntity> others = this.getWorld().getNonSpectatingEntities(LivingEntity.class, expanded);
                for (LivingEntity other : others) {
                    if (other == trigger || !other.isAlive()) {
                        continue;
                    }
                    other.damage(getDamageSources().magic(), 8.0F);
                    other.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                }
                World world = this.getWorld();
                world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_GLASS_BREAK,
                        SoundCategory.HOSTILE, 1.5F, 0.7F + world.getRandom().nextFloat() * 0.3F);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 0.25D, getZ(), 50,
                            1.0D, 0.25D, 1.0D, 0.2D);
                }
                return true;
            }
            case FLAME -> {
                World world = this.getWorld();
                world.playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_FIRECHARGE_USE,
                        SoundCategory.HOSTILE, 1.0F, 1.1F);
                igniteSurroundings();
                Box fireArea = getBoundingBox().expand(1.1D, 1.0D, 1.1D);
                List<LivingEntity> victims = world.getNonSpectatingEntities(LivingEntity.class, fireArea);
                for (LivingEntity victim : victims) {
                    if (!victim.isAlive()) {
                        continue;
                    }
                    victim.setOnFireFor(8);
                    victim.damage(getDamageSources().onFire(), 8.0F);
                }
                return true;
            }
        }
        return false;
    }

    private void igniteSurroundings() {
        BlockPos origin = getBlockPos();
        World world = this.getWorld();
        for (BlockPos target : BlockPos.iterate(origin.add(-1, 0, -1), origin.add(1, 1, 1))) {
            BlockState state = world.getBlockState(target);
            if (state.isAir()) {
                world.setBlockState(target, Blocks.FIRE.getDefaultState(), 3);
            }
        }
    }

    private void dropEmptyTrap() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        ItemStack stack = new ItemStack(ModItems.IM_TRAP);
        TrapItem.setTrapType(stack, TrapItem.TrapType.EMPTY);
        ItemEntity itemEntity = new ItemEntity(serverWorld, getX(), getY(), getZ(), stack);
        itemEntity.setToDefaultPickupDelay();
        serverWorld.spawnEntity(itemEntity);
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        World world = this.getWorld();
        if (world.isClient || !isEmpty() || emptyTicks <= 30) {
            return;
        }

        ItemStack stack = new ItemStack(ModItems.IM_TRAP);
        TrapItem.setTrapType(stack, TrapItem.TrapType.EMPTY);
        if (player.getInventory().insertStack(stack)) {
            world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS, 0.3F, 1.5F);
            discard();
        } else {
            dropEmptyTrap();
            discard();
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.PROBE)) {
            return ActionResult.PASS;
        }

        World world = this.getWorld();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (isEmpty()) {
            return ActionResult.CONSUME;
        }

        if (ProbeItem.getMode(stack) != ProbeItem.Mode.MATERIAL) {
            return ActionResult.CONSUME;
        }

        TrapItem.TrapType type = TrapItem.TrapType.byId(getTrapType());
        ItemStack trapStack = new ItemStack(ModItems.IM_TRAP);
        TrapItem.setTrapType(trapStack, type);
        if (!player.getInventory().insertStack(trapStack)) {
            player.dropItem(trapStack, false);
        }
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_LEVER_CLICK,
                SoundCategory.PLAYERS, 0.3F, 1.5F);
        discard();
        return ActionResult.CONSUME;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setTrapType(nbt.getInt("TrapType"));
        setEmpty(nbt.getBoolean("Empty"));
        emptyTicks = nbt.getInt("EmptyTicks");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("TrapType", getTrapType());
        nbt.putBoolean("Empty", isEmpty());
        nbt.putInt("EmptyTicks", emptyTicks);
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
