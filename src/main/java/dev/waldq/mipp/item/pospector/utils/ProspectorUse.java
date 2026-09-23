//package dev.waldq.mipp.item.pospector.utils;
//
//import aztech.modern_industrialization.MIText;
//import aztech.modern_industrialization.util.TextHelper;
//import dev.waldq.mipp.MIPP;
//import dev.waldq.mipp.worldgen.generator.veins.OreVeinConfig;
//import dev.waldq.mipp.worldgen.generator.veins.OreVeinConfigLoader;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.Level;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//public class ProspectorUse {
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
//        ItemStack stack = player.getItemInHand(usedHand);
//
//        if (!player.isCreative() && getStoredEnergy(stack) < ENERGY_COST) {
//            if (!level.isClientSide()) {
//                var amount = TextHelper.getAmount(ENERGY_COST);
//                player.displayClientMessage(MIPP.text().prospectorEnergyNotice(MIText.Eu.text(amount.digit(), amount.unit())), true);
//            }
//            return InteractionResultHolder.fail(stack);
//        }
//
//        if (!level.isClientSide()) {
//            if (level instanceof ServerLevel serverLevel) {
//                if (!player.isCreative()) {
//                    tryUseEnergy(stack, ENERGY_COST);
//                }
//
//                ChunkPos centerChunk = player.chunkPosition();
//                List<OreVeinConfig> activeVeins = OreVeinConfigLoader.getVeinsForDimension(level.dimension().location());
//
//                player.sendSystemMessage(Component.literal(
//                        "§7[Сканер] Поиск жил в радиусе " + chunkRadius + " чанков (" + (chunkRadius * 16) + " м)..."
//                ));
//
//                long startTime = System.nanoTime();
//
//                long seed = serverLevel.getSeed();
//                ResourceLocation dim = level.dimension().location();
//
//                List<Map.Entry<OreVeinConfig, Integer>> discoveredVeinsInfo = new ArrayList<>();
//
//                CompletableFuture.supplyAsync(() -> ScannerLarge.scanArea(
//                        seed,
//                        dim,
//                        player,
//                        centerChunk,
//                        this.chunkRadius,
//                        activeVeins,
//                        (vein, distance) -> discoveredVeinsInfo.add(Map.entry(vein, distance))
//                ), SCANNER_THREAD_POOL).thenAcceptAsync(foundVeins -> {
//
//                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//
//                    serverLevel.getServer().execute(() -> {
//                        if (player.isAlive()) {
//                            for (var entry : discoveredVeinsInfo) {
//                                player.displayClientMessage(
//                                        MIPP.text().prospectorFoundVein(
//                                                Component.literal(entry.getKey().englishName()),
//                                                Component.literal(String.valueOf(entry.getValue()))
//                                        ),
//                                        false
//                                );
//                            }
//
//                            player.sendSystemMessage(Component.literal(
//                                    "§a[Сканер] Завершено за §e" + durationMs + " мс§a. Найдено уникальных жил: §e" +
//                                            foundVeins.size() + "/" + activeVeins.size()
//                            ));
//                        }
//                    });
//                });
//            }
//
//            return InteractionResultHolder.consume(stack);
//        }
//
//        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
//    }
//}
