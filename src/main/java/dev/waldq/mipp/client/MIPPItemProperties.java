package dev.waldq.mipp.client;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPComponents;
import dev.waldq.mipp.MIPPItems;
import dev.waldq.mipp.item.ElectricItem;
import dev.waldq.mipp.item.component.BlockTracker;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;

public class MIPPItemProperties {
    public static void register() {
        ClampedItemPropertyFunction compassFunction = new CompassItemPropertyFunction((level, stack, entity) -> {
            BlockTracker tracker = stack.get(MIPPComponents.BLOCK_TRACKER);
            return tracker != null && tracker.target().isPresent() && tracker.tracked() ? tracker.target().get() : null;
        });

        ItemProperties.register(
                MIPPItems.ELECTRIC_PROSPECTOR.get(),
                MIPP.id("angle"),
                (stack, level, entity, seed) -> {
                    boolean hasEnergy = (stack.getItem() instanceof ElectricItem electric && electric.getStoredEnergy(stack) > 0);

                    // raw scanning
                    Boolean isScanning = stack.get(MIPPComponents.IS_SCANNING.get());
                    // scanning when there is energy, so that the hand doesn't appear without energy during scanning
                    boolean activeScan = hasEnergy && Boolean.TRUE.equals(isScanning);

                    float baseAngle = -1.0f;
                    float rorationTime = 20.0f;

                    if (activeScan) {
                        // rotation during scanning
                        long time = level != null ? level.getGameTime() : 0;
                        baseAngle = (time % (int) rorationTime) / rorationTime;
                    } else {
                        // compass hand
                        BlockTracker tracker = stack.get(MIPPComponents.BLOCK_TRACKER.get());
                        if (tracker != null && tracker.tracked() && tracker.target().isPresent()) {
                            baseAngle = compassFunction.unclampedCall(stack, level, entity, seed);
                        }
                    }

                    if (baseAngle >= 0.0f) {
                        baseAngle = Math.clamp(baseAngle, 0.0f, 1.0f);
                        // frames with led blinking
                        return hasEnergy ? baseAngle + 2.0f : baseAngle;
                    }
                    // led blinking
                    return hasEnergy ? 5.0f : -1.0f;
                }
        );
    }
}