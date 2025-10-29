package com.lunartyx.invasionmod.entity.terrain;

import com.lunartyx.invasionmod.config.InvasionConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates block modifications for a single mob. Requests are processed one
 * at a time with configurable cooldowns so terrain changes remain server
 * friendly and respect the {@code doMobGriefing} gamerule.
 */
public class TerrainModifier {
    private static final int DEFAULT_COOLDOWN = 6;
    private static final int MAX_QUEUE = 16;

    private final LivingEntity actor;
    private final Deque<ModifyBlockRequest> queue = new ArrayDeque<>();
    private int cooldown;

    public TerrainModifier(LivingEntity actor) {
        this.actor = Objects.requireNonNull(actor);
    }

    public boolean submit(Iterable<ModifyBlockRequest> requests) {
        if (!(actor.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }
        if (!isMobGriefingEnabled(serverWorld)) {
            return false;
        }
        List<ModifyBlockRequest> additions = new ArrayList<>();
        for (ModifyBlockRequest request : requests) {
            if (request != null) {
                additions.add(request);
            }
        }
        if (additions.isEmpty()) {
            return false;
        }
        if (queue.size() + additions.size() > MAX_QUEUE) {
            return false;
        }
        queue.addAll(additions);
        return true;
    }

    public void tick() {
        World world = actor.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            queue.clear();
            cooldown = 0;
            return;
        }
        if (!isMobGriefingEnabled(serverWorld)) {
            queue.clear();
            cooldown = 0;
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        ModifyBlockRequest request = queue.peekFirst();
        if (request == null) {
            return;
        }
        if (!request.canExecute(serverWorld)) {
            queue.removeFirst();
            return;
        }
        boolean dropItems = InvasionConfigManager.getConfig().general().destructedBlocksDrop();
        if (request.execute(serverWorld, actor, dropItems)) {
            cooldown = request.getDelayTicks();
        } else {
            cooldown = DEFAULT_COOLDOWN;
        }
        queue.removeFirst();
    }

    public boolean isIdle() {
        return queue.isEmpty() && cooldown <= 0;
    }

    public void clear() {
        queue.clear();
        cooldown = 0;
    }

    public static boolean isMobGriefingEnabled(ServerWorld world) {
        return world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    public LivingEntity getActor() {
        return actor;
    }

    public BlockPos getActorBlockPos() {
        return actor.getBlockPos();
    }
}
