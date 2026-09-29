package dev.waldq.mipp.item.analyzer;

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeAccessor;
import dev.waldq.mipp.item.analyzer.miaccessors.ItemNetworkNodeItemConnectionAccessor;
import dev.waldq.mipp.item.analyzer.net.PipeScanner;
import dev.waldq.mipp.mixins.ItemNetworkNodeMixin;
import dev.waldq.mipp.mixins.PipeNetworkNodeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.common.util.FakePlayer;

public class AnalyzerItem extends Item {
    public AnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    static boolean isTargetingItemPipe(UseOnContext context) {
        BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());

        PipeVoxelShape hitPart = PipeBlock.getHitPart(context.getLevel(), context.getClickedPos(), hitResult);
        if (hitPart == null) return false;
        PipeNetworkType type = hitPart.type;
        assert type != null;
        return type.getNodeCtor().get() instanceof ItemNetworkNode;
    }

    public static boolean isHolding(Player p) {
        return p.getMainHandItem().getItem() instanceof AnalyzerItem
                || p.getOffhandItem().getItem() instanceof AnalyzerItem;
    }

    static ItemNetwork getItemNetwork(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipeEntity)) {
            return null;
        }

        BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
        PipeVoxelShape hitPart = PipeBlock.getHitPart(level, pos, hitResult);
        PipeNetworkType type = hitPart.type;
        assert type != null;
        if (!(type.getNodeCtor().get() instanceof ItemNetworkNode)) return null;

        for (var node : pipeEntity.getNodes()) {
            if (node.getType() == hitPart.type && node instanceof ItemNetworkNode itemNode) {
                if (((PipeNetworkNodeAccessor) itemNode).getNetwork() instanceof ItemNetwork net) {
                    return net;
                }
            }
        }

        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = ctx.getLevel();
        if (!level.isClientSide && player instanceof ServerPlayer sPlayer) {
            ItemNetwork network = getItemNetwork(ctx);

            if (network == null) {
                PipeScanner.clear(sPlayer);
                return InteractionResult.CONSUME;
            }

            PipeScanner.scan(sPlayer, network);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);

        if (!level.isClientSide && player instanceof ServerPlayer sPlayer) {
            PipeScanner.clear(sPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        Level level = context.getLevel();
//        BlockPos pos = context.getClickedPos();
//        Player player = context.getPlayer();
//
//        if (player == null || player instanceof FakePlayer || level.isClientSide) {
//            return InteractionResult.PASS;
//        }
//
//        ItemNetworkNode node = getItemNetworkNode(context);
//        if (node == null) {
//            return InteractionResult.PASS;
//        }
//
//        var nodeAccessor = (ItemNetworkNodeAccessor) node;
//        var connections = nodeAccessor.mipp$getItemConnections(pos);
//        boolean foundAny = false;
//        player.sendSystemMessage(Component.literal("§e--- Pipe Analysis (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") ---"));
//
//        for (var connection : connections) {
//            if (connection != null) {
//                foundAny = true;
//                int extracted = connection.mipp$getExtracted();
//                int inserted = connection.mipp$getInserted();
//
//                player.sendSystemMessage(Component.literal(
//                        String.format("§7Dir: §f%-5s §7Type: §f%-10s §7| Extracted: §a%d§7 | Inserted: §b%d",
//                                connection.mipp$getDirection(),
//                                connection.mipp$getPipeEndpointType(),
//                                extracted,
//                                inserted
//                        )
//                ));
//            }
//        }
//
//        if (!foundAny) {
//            player.sendSystemMessage(Component.literal("§cNo active connections on this pipe node."));
//        }
//
//        return InteractionResult.SUCCESS;
//    }
}
