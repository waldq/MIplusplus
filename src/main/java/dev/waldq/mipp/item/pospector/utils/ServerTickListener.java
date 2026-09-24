package dev.waldq.mipp.item.pospector.utils;

import brachy.modularui.factory.PlayerInventoryUIFactory;
import dev.waldq.mipp.MIPP;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MIPP.ID)
public class ServerTickListener {

    private static class ScanData {
        ScannerLocal scanner;
        InteractionHand hand;
        WeakReference<Player> playerRef;
        Set<BlockState>[] foundOres;
        Set<BlockState> uniqueOres;
        boolean guiOpened = false;

        ScanData(ScannerLocal scanner, InteractionHand hand, Player player) {
            this.scanner = scanner;
            this.hand = hand;
            this.playerRef = new WeakReference<>(player);
        }
    }

    private static final Map<UUID, ScanData> ACTIVE_SCANS = new ConcurrentHashMap<>();

    public static Set<BlockState>[] getFoundOres(Player player) {
        ScanData data = ACTIVE_SCANS.get(player.getUUID());
        return data != null ? data.foundOres : null;
    }

    public static Set<BlockState> getUniqueOres(Player player) {
        ScanData data = ACTIVE_SCANS.get(player.getUUID());
        return data != null ? data.uniqueOres : null;
    }

    public static ScannerLocal getActiveScanner(Player player) {
        ScanData data = ACTIVE_SCANS.get(player.getUUID());
        return data != null ? data.scanner : null;
    }

    public static boolean isScanning(Player player) {
        ScanData data = ACTIVE_SCANS.get(player.getUUID());
        return data != null && data.scanner != null && data.scanner.isScanning();
    }

    public static void startScan(Player player, ScannerLocal scanner, InteractionHand hand) {
        ACTIVE_SCANS.put(player.getUUID(), new ScanData(scanner, hand, player));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_SCANS.isEmpty()) return;

        ACTIVE_SCANS.entrySet().removeIf(entry -> {
            ScanData data = entry.getValue();
            Player player = data.playerRef.get();

            if (player == null || !player.isAlive()) {
                return true;
            }

            if (data.scanner != null && data.scanner.isScanning()) {
                data.scanner.onUse();
                return false;
            }

            if (!data.guiOpened && data.scanner != null) {
                data.foundOres = data.scanner.getFoundOres();
                data.uniqueOres = data.scanner.getUniqueOres();
                data.guiOpened = true;

                if (data.hand != null) {
                    PlayerInventoryUIFactory.INSTANCE.openFromHand(player, data.hand);
                }

                return false;
            }

            return data.guiOpened;
        });
    }
}