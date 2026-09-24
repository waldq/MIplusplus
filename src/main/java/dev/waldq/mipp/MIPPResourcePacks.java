package dev.waldq.mipp;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = MIPP.ID)
public class MIPPResourcePacks {
    @SubscribeEvent
    public static void registerZedTechFinder(AddPackFindersEvent event) {
        event.addPackFinders(
                MIPP.id("resourcepacks/zedtech"),
                PackType.CLIENT_RESOURCES,
                Component.literal("ZedTech textures for MI++"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }
}