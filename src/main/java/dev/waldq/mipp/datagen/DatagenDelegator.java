package dev.waldq.mipp.datagen;

import dev.waldq.mipp.datagen.client.DatagenDelegatorClient;
import dev.waldq.mipp.datagen.server.DatagenDelegatorServer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;


public final class DatagenDelegator
{
	@SubscribeEvent
	public void gatherData(GatherDataEvent event)
	{
		DatagenDelegatorClient.configure(event);
		DatagenDelegatorServer.configure(event);
	}
}
