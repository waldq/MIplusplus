package dev.waldq.mipp.item.pipenetworkanalyzer;

import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;

import dev.waldq.mipp.item.pipenetworkanalyzer.helper.PipeScanner;
import dev.waldq.mipp.mixins.PipeNetworkNodeAccessor;

import net.minecraft.core.BlockPos;
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

public class PipeNetworkAnalyzerItem extends Item {
    public static volatile double scanRenderDistance = 64;

    public static double adjustDistance(double delta) {
        double next = scanRenderDistance + delta * 2;
        scanRenderDistance = Math.clamp(next, 16, 192);
        return scanRenderDistance;
    }

    public PipeNetworkAnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    static boolean isTargetingItemPipe(UseOnContext ctx) {
        BlockHitResult hitResult = new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside());

        PipeVoxelShape hitPart = PipeBlock.getHitPart(ctx.getLevel(), ctx.getClickedPos(), hitResult);
        if (hitPart == null) return false;
        PipeNetworkType type = hitPart.type;
        assert type != null;
        return type.getNodeCtor().get() instanceof ItemNetworkNode;
    }

    public static boolean isHolding(Player p) {
        return p.getMainHandItem().getItem() instanceof PipeNetworkAnalyzerItem
                || p.getOffhandItem().getItem() instanceof PipeNetworkAnalyzerItem;
    }

    static ItemNetworkNode getItemNetworkNode(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipeEntity)) {
            return null;
        }

        BlockHitResult hitResult = new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), pos, ctx.isInside());
        PipeVoxelShape hitPart = PipeBlock.getHitPart(level, pos, hitResult);

        if (hitPart == null) return null;
        PipeNetworkType type = hitPart.type;

        if (type == null) return null;
        if (!(type.getNodeCtor().get() instanceof ItemNetworkNode)) return null;

        for (var node : pipeEntity.getNodes()) {
            if (node.getType() == hitPart.type && node instanceof ItemNetworkNode itemNode) {
                return itemNode;
            }
        }
        return null;
    }

    static ItemNetwork getItemNetwork(ItemNetworkNode node) {
        if (node == null) return null;

        if (((PipeNetworkNodeAccessor) node).getNetwork() instanceof ItemNetwork net) {
            return net;
        }
        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        if (player == null) return InteractionResult.PASS;

        Level level = ctx.getLevel();

        if (!level.isClientSide && player instanceof ServerPlayer sPlayer) {
            var node = getItemNetworkNode(ctx);

            if (node != null) {
                if (player.isShiftKeyDown()) {
                    ItemNetwork network = getItemNetwork(node);
                    PipeScanner.scan(sPlayer, network);
                    return InteractionResult.CONSUME;
                }

                var targetNode = new PipeNetwork.PosNode(pos, node);
                PipeScanner.scan(sPlayer, targetNode);
                return InteractionResult.CONSUME;
            }
            PipeScanner.clear(sPlayer);
            return InteractionResult.PASS;
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
}
