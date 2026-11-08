package dev.waldq.mipp;

import com.mojang.authlib.GameProfile;
import dev.waldq.mipp.blocks.machine.utils.CableTierUtils;
import dev.waldq.mipp.item.component.BlockTracker;
import dev.waldq.mipp.item.pospector.ProspectorItemBehavior;
import dev.waldq.mipp.saveddata.ChunkLoaderSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;
import static net.minecraft.commands.arguments.GameProfileArgument.gameProfile;
import static net.minecraft.commands.arguments.GameProfileArgument.getGameProfiles;

@EventBusSubscriber(modid = MIPP.ID)
public class MIPPCommands {
    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("mipp")
                .then(literal("track")
                        .then(argument("pos", blockPos())
                                .executes(context -> trackBlock(context.getSource(), getBlockPos(context, "pos")))))

                .then(literal("untrack")
                        .executes(context -> untrackBlock(context.getSource())))

                .then(literal("chunkloaders")
                        .requires(s -> s.hasPermission(2))
                        .then(literal("get")
                                .then(argument("players", gameProfile())
                                        .suggests((context, builder) -> {
                                            var loaders = ownerProfiles(context.getSource()).stream()
                                                    .filter(profile -> !Objects.equals(profile.getName(), profile.getId().toString()))
                                                    .map(GameProfile::getName);
                                            return SharedSuggestionProvider.suggest(loaders, builder);
                                        })
                                        .executes(context -> getPlayersChunkLoaders(context.getSource(), getGameProfiles(context, "players")))
                                )
                                .then(literal("all")
                                        .executes(context -> {
                                            var profiles = ownerProfiles(context.getSource());
                                            if (profiles.isEmpty()) {
                                                context.getSource().sendFailure(MIPP.text().chunkLoader().none());
                                                return 0;
                                            }
                                            return getPlayersChunkLoaders(context.getSource(), profiles);
                                        })
                                )
                        )
                )
        );
    }

    private static int trackBlock(CommandSourceStack source, BlockPos pos) {
        var player = source.getPlayer();
        if (player == null) return 0;

        var level = source.getLevel();

        var offStack = player.getOffhandItem();
        var mainStack = player.getMainHandItem();

        ItemStack targetStack = ItemStack.EMPTY;

        if (mainStack.getItem() instanceof ProspectorItemBehavior) {
            targetStack = mainStack;
        } else if (offStack.getItem() instanceof ProspectorItemBehavior) {
            targetStack = offStack;
        }

        if (targetStack.isEmpty()) return 0;

        var is_scanning = targetStack.get(MIPPComponents.IS_SCANNING);
        if (is_scanning != null && is_scanning) {
            targetStack.set(MIPPComponents.IS_SCANNING.get(), false);
        }

        var global = new GlobalPos(level.dimension(), pos);

        BlockTracker tracker = new BlockTracker(Optional.of(global), true);

        targetStack.set(MIPPComponents.BLOCK_TRACKER, tracker);
        return 1;
    }

    private static int untrackBlock(CommandSourceStack source) {
        var player = source.getPlayer();
        if (player == null) return 0;

        var offStack = player.getOffhandItem();
        var mainStack = player.getMainHandItem();

        ItemStack targetStack = ItemStack.EMPTY;

        if (mainStack.getItem() instanceof ProspectorItemBehavior) {
            targetStack = mainStack;
        } else if (offStack.getItem() instanceof ProspectorItemBehavior) {
            targetStack = offStack;
        }

        if (targetStack.isEmpty()) return 0;

        targetStack.remove(MIPPComponents.BLOCK_TRACKER);
        return 1;
    }

    private static List<GameProfile> ownerProfiles(CommandSourceStack source) {
        var cache = source.getServer().getProfileCache();

        return ChunkLoaderSavedData.get(source.getLevel()).getAllLoaders().values().stream()
                .map(ChunkLoaderSavedData.LoaderInfo::owner)
                .distinct()
                .map(id -> {
                    var profile = cache == null ? Optional.<GameProfile>empty() : cache.get(id);
                    return profile.orElseGet(() -> new GameProfile(id, id.toString()));
                })
                .toList();

    }

    private static int getPlayersChunkLoaders(CommandSourceStack source, Collection<GameProfile> players) {
        int combined = 0;
        source.sendSuccess(() -> MIPP.text().chunkLoader().found(), false);
        for (GameProfile profile : players) {
            var playerLoaders = ChunkLoaderSavedData.get(source.getLevel()).getLoadersByPlayer(profile.getId());
            combined += playerLoaders.size();
            showLoaderStats(source, playerLoaders, profile.getName());
        }
        return combined;
    }

    private static void showLoaderStats(CommandSourceStack source, List<ChunkLoaderSavedData.LoaderInfo> loaders, String nickname) {
        if (loaders.isEmpty()) {
            source.sendSuccess(() -> MIPP.text().chunkLoader().noLoaders(Component.literal(nickname)), false);
            return;
        }
        source.sendSuccess(() -> MIPP.text().chunkLoader().line(Component.literal(nickname)), false);

        for (var info : loaders) {
            source.sendSuccess(() -> MIPP.text().chunkLoader().info(
                    Component.literal(info.dimension().toString()),
                    wrap(info.dimension().toString(), info.pos()),
                    Component.literal(info.tier().name),
                    Component.literal(String.valueOf(CableTierUtils.CableNumbers.get(info.tier()).partialChunkAmount()))
            ), false);
        }
    }

    private static Component wrap(String dim, BlockPos pos) {
        return ComponentUtils.wrapInSquareBrackets(Component.literal("%d %d %d".formatted(pos.getX(), pos.getY(), pos.getZ())))
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "/execute in %s run tp @s %d %d %d".formatted(dim, pos.getX(), pos.getY() + 1, pos.getZ())
                        ))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, MIPP.text().chunkLoader().clickToTp())))
                .withStyle(ChatFormatting.GREEN);
    }
}
