package dev.waldq.mipp;

import dev.waldq.mipp.item.component.BlockTracker;

import dev.waldq.mipp.item.pospector.ProspectorItemBehavior;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.*;

@EventBusSubscriber(modid = MIPP.ID)
public class MIPPCommands {
    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("mipp")
                .then(literal("track")
                        .then(argument("pos", blockPos())
                                .executes(context -> trackBlock(context.getSource(), getBlockPos(context, "pos"))))));

        event.getDispatcher().register(literal("mipp")
                .then(literal("untrack")
                        .executes(context -> untrackBlock(context.getSource()))));
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
}
