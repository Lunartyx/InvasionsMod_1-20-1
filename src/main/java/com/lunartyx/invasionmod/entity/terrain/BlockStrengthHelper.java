package com.lunartyx.invasionmod.entity.terrain;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.config.InvasionConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralises the legacy block-strength table so digging logic, engineer
 * scaffolding, and the probe item all respect configuration overrides. Strength
 * values roughly mirror the Forge-era {@code EntityIMLiving} defaults.
 */
public final class BlockStrengthHelper {
    private static final Map<Block, Float> STRENGTHS = new HashMap<>();
    private static final float DEFAULT_STRENGTH = 2.5F;
    private static final float STRUCTURAL_THRESHOLD = 1.5F;
    private static final float MINIMUM_DELAY_TICKS = 4.0F;
    private static final float MAXIMUM_DELAY_TICKS = 200.0F;

    private BlockStrengthHelper() {
    }

    public static void initialize(InvasionConfig config) {
        STRENGTHS.clear();
        registerDefaults();
        if (config != null) {
            applyOverrides(config.blockStrengthOverrides());
        }
    }

    private static void registerDefaults() {
        register(Blocks.AIR, 0.01F);
        register(Blocks.STONE, 5.5F);
        register(Blocks.STONE_BRICKS, 5.5F);
        register(Blocks.CRACKED_STONE_BRICKS, 5.5F);
        register(Blocks.CHISELED_STONE_BRICKS, 5.5F);
        register(Blocks.COBBLESTONE, 5.5F);
        register(Blocks.MOSSY_COBBLESTONE, 5.5F);
        register(Blocks.BRICKS, 5.5F);
        register(Blocks.OBSIDIAN, 7.7F);
        register(Blocks.IRON_BLOCK, 7.7F);
        register(Blocks.DIRT, 3.125F);
        register(Blocks.GRASS_BLOCK, 3.125F);
        register(Blocks.SAND, 2.5F);
        register(Blocks.RED_SAND, 2.5F);
        register(Blocks.GRAVEL, 2.5F);
        register(Blocks.GLASS, 2.5F);
        register(Blocks.GLASS_PANE, 2.5F);
        register(Blocks.VINE, 1.25F);
        register(Blocks.IRON_DOOR, 15.4F);
        register(Blocks.SANDSTONE, 5.5F);
        register(Blocks.CUT_SANDSTONE, 5.5F);
        register(Blocks.SMOOTH_SANDSTONE, 5.5F);
        register(Blocks.CHISELED_SANDSTONE, 5.5F);
        register(Blocks.NETHERRACK, 3.85F);
        register(Blocks.NETHER_BRICKS, 5.5F);
        register(Blocks.RED_NETHER_BRICKS, 5.5F);
        register(Blocks.SOUL_SAND, 2.5F);
        register(Blocks.GLOWSTONE, 2.5F);
        register(Blocks.TALL_GRASS, 0.3F);
        register(Blocks.LARGE_FERN, 0.3F);
        register(Blocks.DRAGON_EGG, 15.0F);
        registerTag(BlockTags.LEAVES, 1.25F);
        registerTag(BlockTags.LOGS, 5.5F);
        registerTag(BlockTags.PLANKS, 5.5F);
        registerTag(BlockTags.FENCES, 5.5F);
        registerTag(BlockTags.WOODEN_DOORS, 5.5F);
    }

    private static void register(Block block, float strength) {
        STRENGTHS.put(block, strength);
    }

    private static void registerTag(TagKey<Block> tag, float strength) {
        Registries.BLOCK.stream()
                .filter(block -> block.getDefaultState().isIn(tag))
                .forEach(block -> register(block, strength));
    }

    private static void applyOverrides(Map<String, Float> overrides) {
        if (overrides == null) {
            return;
        }
        overrides.forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }
            Identifier id = Identifier.tryParse(key);
            if (id == null) {
                InvasionMod.LOGGER.warn("Ignoring invalid block strength override key: {}", key);
                return;
            }
            Block block = Registries.BLOCK.getOrEmpty(id).orElse(null);
            if (block == null) {
                InvasionMod.LOGGER.warn("Ignoring unknown block strength override target: {}", key);
                return;
            }
            register(block, Math.max(0.0F, value));
        });
    }

    public static float getBlockStrength(BlockState state, World world, BlockPos pos) {
        if (state == null || state.isAir()) {
            return 0.01F;
        }
        Float override = STRENGTHS.get(state.getBlock());
        if (override != null) {
            return override;
        }
        if (world != null) {
            float hardness = state.getHardness(world, pos);
            if (hardness < 0.0F) {
                return Float.POSITIVE_INFINITY;
            }
            if (hardness == 0.0F) {
                return DEFAULT_STRENGTH;
            }
            return Math.max(DEFAULT_STRENGTH, hardness * 5.0F);
        }
        return DEFAULT_STRENGTH;
    }

    public static boolean isDestructible(BlockState state, World world, BlockPos pos) {
        if (state == null || state.isAir()) {
            return false;
        }
        float strength = getBlockStrength(state, world, pos);
        return !Float.isInfinite(strength) && strength >= 0.0F;
    }

    public static boolean isStructural(BlockState state, World world, BlockPos pos) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (world != null && state.getCollisionShape(world, pos).isEmpty()) {
            return false;
        }
        return getBlockStrength(state, world, pos) >= STRUCTURAL_THRESHOLD;
    }

    public static int getDigDelayTicks(BlockState state, World world, BlockPos pos) {
        float strength = Math.max(0.0F, getBlockStrength(state, world, pos));
        if (Float.isInfinite(strength)) {
            return Integer.MAX_VALUE;
        }
        float ticks = MathHelper.clamp(strength * 20.0F, MINIMUM_DELAY_TICKS, MAXIMUM_DELAY_TICKS);
        return MathHelper.ceil(ticks);
    }
}
