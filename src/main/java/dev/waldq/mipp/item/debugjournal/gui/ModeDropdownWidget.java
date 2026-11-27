package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.menu.DropdownWidget;
import brachy.modularui.widgets.menu.Menu;
import dev.waldq.mipp.item.debugjournal.utils.session.ITextOwner;

public class ModeDropdownWidget<T extends Enum<T> & ITextOwner, W extends ModeDropdownWidget<T, W>> extends DropdownWidget<T, ModeDropdownWidget<T, W>> {
    public ModeDropdownWidget(String panelName, Class<T> modeType) {
        super(panelName, modeType);
    }

    @Override
    protected Menu<?> createMenu() {
        Menu<?> menu = super.createMenu()
                .width(120)
                .background(GuiTextures.MC_BACKGROUND)
                .padding(4);
        var list = menu.getChildren().getFirst();

        for (var child : list.getChildren()) {
            if (child instanceof ButtonWidget<?> button) {
                button.background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED);
            }
        }
        return menu;
    }
}
