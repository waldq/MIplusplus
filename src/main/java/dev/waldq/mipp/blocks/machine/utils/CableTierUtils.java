package dev.waldq.mipp.blocks.machine.utils;

import aztech.modern_industrialization.api.energy.CableTier;

import java.util.Map;

public class CableTierUtils {
    public record CableData(long cost, int chunkRadius, int chunkAmount) {}

    public static final Map<CableTier, CableData> CableNumbers = Map.ofEntries(
            Map.entry(CableTier.LV, new CableData(32L, 0, 1)),
            Map.entry(CableTier.MV, new CableData(128L, 1, 9)),
            Map.entry(CableTier.HV, new CableData(512L, 2, 25))
    );

}