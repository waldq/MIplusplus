package dev.waldq.mipp.item.pospector.gui;

import brachy.modularui.drawable.UITexture;

import dev.waldq.mipp.MIPP;

public class MapHelper {
    public static UITexture BACKGROUND_INVERSE = UITexture.builder()
            .location(MIPP.id("textures/gui/base/background_inverse"))
            .imageSize(16, 16)
            .adaptable(4)
            .tiled()
            .name("background_inverse")
            .build();
}
