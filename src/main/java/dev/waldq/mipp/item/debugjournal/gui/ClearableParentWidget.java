package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.widget.ParentWidget;

public class ClearableParentWidget extends ParentWidget<ClearableParentWidget> {
    public boolean clear() {
        return removeAll();
    }
}
