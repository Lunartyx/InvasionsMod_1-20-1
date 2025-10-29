package com.lunartyx.invasionmod.command;

import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class InvasionCommands {
    private static final SimpleCommandExceptionType NO_FOCUS = new SimpleCommandExceptionType(Text.translatable("commands.invasionmod.invasion.no_focus"));

    private InvasionCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(InvasionCommands::register);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("invasion")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> showHelp(ctx.getSource()))
                .then(CommandManager.literal("help").executes(ctx -> showHelp(ctx.getSource())))
                .then(CommandManager.literal("begin")
                        .executes(ctx -> begin(ctx.getSource(), 1, false))
                        .then(CommandManager.argument("wave", IntegerArgumentType.integer(1))
                                .executes(ctx -> begin(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "wave"), false))))
                .then(CommandManager.literal("continuous")
                        .executes(ctx -> begin(ctx.getSource(), 1, true))
                        .then(CommandManager.argument("wave", IntegerArgumentType.integer(1))
                                .executes(ctx -> begin(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "wave"), true))))
                .then(CommandManager.literal("end").executes(ctx -> end(ctx.getSource())))
                .then(CommandManager.literal("range")
                        .then(CommandManager.argument("radius", IntegerArgumentType.integer(NexusBlockEntity.MIN_SPAWN_RADIUS, NexusBlockEntity.MAX_SPAWN_RADIUS))
                                .executes(ctx -> setRange(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
                .then(CommandManager.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(CommandManager.literal("spawnertest")
                        .executes(ctx -> spawnerTest(ctx.getSource(), 1, 10, false))
                        .then(CommandManager.argument("start_wave", IntegerArgumentType.integer(1))
                                .executes(ctx -> spawnerTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "start_wave"), IntegerArgumentType.getInteger(ctx, "start_wave"), false))
                                .then(CommandManager.argument("end_wave", IntegerArgumentType.integer(1))
                                        .executes(ctx -> spawnerTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "start_wave"), IntegerArgumentType.getInteger(ctx, "end_wave"), false)))))
                .then(CommandManager.literal("spawnertest_continuous")
                        .executes(ctx -> spawnerTest(ctx.getSource(), 1, 10, true))
                        .then(CommandManager.argument("start_wave", IntegerArgumentType.integer(1))
                                .executes(ctx -> spawnerTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "start_wave"), IntegerArgumentType.getInteger(ctx, "start_wave"), true))
                                .then(CommandManager.argument("end_wave", IntegerArgumentType.integer(1))
                                        .executes(ctx -> spawnerTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "start_wave"), IntegerArgumentType.getInteger(ctx, "end_wave"), true)))))
                .then(CommandManager.literal("pointcontainertest")
                        .executes(ctx -> pointTest(ctx.getSource(), NexusBlockEntity.MIN_SPAWN_RADIUS, 32))
                        .then(CommandManager.argument("radius", IntegerArgumentType.integer(8, 196))
                                .executes(ctx -> pointTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius"), 64))
                                .then(CommandManager.argument("samples", IntegerArgumentType.integer(8, 512))
                                        .executes(ctx -> pointTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius"), IntegerArgumentType.getInteger(ctx, "samples"))))))
                .then(CommandManager.literal("wavebuildertest")
                        .executes(ctx -> waveBuilderTest(ctx.getSource(), 1.0F, 1.0F, 160))
                        .then(CommandManager.argument("difficulty", FloatArgumentType.floatArg(0.0F))
                                .executes(ctx -> waveBuilderTest(ctx.getSource(), FloatArgumentType.getFloat(ctx, "difficulty"), 1.0F, 160))
                                .then(CommandManager.argument("tier", FloatArgumentType.floatArg(0.0F))
                                        .executes(ctx -> waveBuilderTest(ctx.getSource(), FloatArgumentType.getFloat(ctx, "difficulty"), FloatArgumentType.getFloat(ctx, "tier"), 160))
                                        .then(CommandManager.argument("length", IntegerArgumentType.integer(30, 600))
                                                .executes(ctx -> waveBuilderTest(ctx.getSource(), FloatArgumentType.getFloat(ctx, "difficulty"), FloatArgumentType.getFloat(ctx, "tier"), IntegerArgumentType.getInteger(ctx, "length"))))))));

        dispatcher.register(root);
    }

    private static int showHelp(ServerCommandSource source) {
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.1"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.2"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.3"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.4"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.5"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.6"), false);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.help.7"), false);
        return 1;
    }

    private static int begin(ServerCommandSource source, int wave, boolean continuous) throws CommandSyntaxException {
        FocusedNexusContext context = requireFocusedNexus(source);
        NexusBlockEntity nexus = context.nexus();
        ServerWorld world = context.world();
        boolean started = nexus.commandStart(world, wave, continuous);
        if (!started) {
            source.sendError(Text.translatable("commands.invasionmod.invasion.begin.failed"));
            return 0;
        }
        if (continuous) {
            source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.begin.continuous", wave), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.begin.standard", wave), true);
        }
        return 1;
    }

    private static int end(ServerCommandSource source) throws CommandSyntaxException {
        FocusedNexusContext context = requireFocusedNexus(source);
        boolean stopped = context.nexus().commandStop(true);
        if (!stopped) {
            source.sendError(Text.translatable("commands.invasionmod.invasion.end.none"));
            return 0;
        }
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.end.success"), true);
        return 1;
    }

    private static int setRange(ServerCommandSource source, int radius) throws CommandSyntaxException {
        FocusedNexusContext context = requireFocusedNexus(source);
        NexusBlockEntity nexus = context.nexus();
        if (nexus.getMode().isActive()) {
            source.sendError(Text.translatable("commands.invasionmod.invasion.range.active"));
            return 0;
        }
        nexus.setSpawnRadius(radius);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.range.success", radius), true);
        return 1;
    }

    private static int status(ServerCommandSource source) throws CommandSyntaxException {
        FocusedNexusContext context = requireFocusedNexus(source);
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            context.nexus().debugStatus(player);
        }
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.status"), false);
        return 1;
    }

    private static int spawnerTest(ServerCommandSource source, int startWave, int endWave, boolean continuous) {
        new InvasionTester().runSpawnerTest(startWave, endWave, continuous);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.test.spawner", startWave, endWave, continuous), true);
        return 1;
    }

    private static int waveBuilderTest(ServerCommandSource source, float difficulty, float tier, int length) {
        new InvasionTester().runWaveBuilderTest(difficulty, tier, length);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.test.wavebuilder", difficulty, tier, length), true);
        return 1;
    }

    private static int pointTest(ServerCommandSource source, int radius, int samples) {
        new InvasionTester().runSpawnPointSelectionTest(radius, samples);
        source.sendFeedback(() -> Text.translatable("commands.invasionmod.invasion.test.points", radius, samples), true);
        return 1;
    }

    private static FocusedNexusContext requireFocusedNexus(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            throw NO_FOCUS.create();
        }
        return NexusCommandTracker.getFocus(player.getUuid())
                .flatMap(focus -> {
                    ServerWorld world = source.getServer().getWorld(focus.dimension());
                    if (world == null) {
                        NexusCommandTracker.clearFocus(player.getUuid());
                        return java.util.Optional.empty();
                    }
                    BlockPos pos = focus.pos();
                    if (!(world.getBlockEntity(pos) instanceof NexusBlockEntity nexus)) {
                        NexusCommandTracker.clearFocus(player.getUuid());
                        return java.util.Optional.empty();
                    }
                    return java.util.Optional.of(new FocusedNexusContext(world, nexus));
                })
                .orElseThrow(NO_FOCUS::create);
    }

    private record FocusedNexusContext(ServerWorld world, NexusBlockEntity nexus) {
    }
}
