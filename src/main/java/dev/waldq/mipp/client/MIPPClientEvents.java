package dev.waldq.mipp.client;

import dev.waldq.mipp.MIPP;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = MIPP.ID, value = Dist.CLIENT)
public class MIPPClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(MIPPItemProperties::register);
    }
}