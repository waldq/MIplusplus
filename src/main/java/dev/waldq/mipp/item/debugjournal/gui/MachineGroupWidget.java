package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.client.ClientMachineOverlays;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.item.debugjournal.utils.session.GroupMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class MachineGroupWidget extends ParentWidget<MachineGroupWidget> implements Interactable {
    private static final int SIZE = 20;

    private final Flow content;
    private final Widget<Flow> groupHeader;
    private final ClearableParentWidget clearableList;
    private JournalListWidget list;


    MachineGroupWidget(
            EnumSyncValue<GroupMode> groupModeValue,
            Object headerKey,
            GenericListSyncHandler<ByteBuf, MachineRow> rowHandler,
            Supplier<List<Integer>> indicesSupplier
    ) {
        widthRel(1f).coverChildrenHeight();

        Widget<Flow> header = groupModeValue.getValue().createGroupHeader(headerKey).size(200, SIZE);

        content = Flow.col()
                .widthRel(1f)
                .coverChildrenHeight();

        child(content);

        clearableList = new ClearableParentWidget()
                .widthRel(1f)
                .height(0);

        groupHeader = createGroupWidget(header, rowHandler, indicesSupplier)
                .height(SIZE);

        content.child(groupHeader);
        content.child(clearableList);
    }

    private Widget<Flow> createGroupWidget(
            Widget<Flow> header,
            GenericListSyncHandler<ByteBuf, MachineRow> rowHandler,
            Supplier<List<Integer>> indicesSupplier
    ) {
        return Flow.row()
                .child(new ButtonWidget<>()
                        .widthRelOffset(1f, -SIZE)
                        .height(SIZE)
                        .child(Flow.row()
                                .child(header)
                                .child(
                                        Text.dynamic(() -> MIPP.text().debugJournal().machinesAmount(
                                                Component.literal(
                                                        String.valueOf(indicesSupplier.get().size())
                                                )
                                        )).asWidget()
                                )
                        )
                        .onMousePressed((ctx, btn) -> {
                            if (list == null) {
                                list = new JournalListWidget(rowHandler, indicesSupplier, false);
                                clearableList.child(list.widthRel(0.99f).right(0));
                                clearableList.coverChildrenHeight();
                            } else {
                                clearableList.clear();
                                clearableList.coverChildrenHeight(-1).height(0);
                                list = null;
                            }
                            scheduleResize();
                            return true;
                        }))
                .child(new ButtonWidget<>()
                        .size(SIZE)
                        .overlay(GuiTextures.VISIBLE)
                        .onMousePressed((context, button) -> {
                            List<MachineRow> all = rowHandler.getValue();
                            List<MachineRow> group = indicesSupplier.get().stream()
                                    .filter(i -> i >= 0 && i < all.size())
                                    .map(all::get)
                                    .toList();
                            return ClientMachineOverlays.addOrRemoveList(group);
                        })
                        .tooltip(r -> r.addLine(MIPP.text().debugJournal().highlights().highlightAll())));
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
