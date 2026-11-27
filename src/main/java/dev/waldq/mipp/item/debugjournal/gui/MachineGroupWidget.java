package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.item.debugjournal.utils.session.GroupMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class MachineGroupWidget extends ParentWidget<MachineGroupWidget> implements Interactable {
    private final Flow content;
    private final ButtonWidget<?> groupHeader;
    private final ClearableParentWidget clearableList;
    private JournalListWidget list;


    MachineGroupWidget(
            EnumSyncValue<GroupMode> groupModeValue,
            Object headerKey,
            GenericListSyncHandler<ByteBuf, MachineRow> rowHandler,
            Supplier<List<Integer>> indicesSupplier
    ) {
        widthRel(1f).coverChildrenHeight();

        Widget<Flow> header = groupModeValue.getValue().createGroupHeader(headerKey).size(200, 20);

        content = Flow.col()
                .widthRel(1f)
                .coverChildrenHeight();

        child(content);

        clearableList = new ClearableParentWidget()
                .widthRel(1f)
                .height(0);

        groupHeader = new ButtonWidget<>()
                .widthRel(1f)
                .height(20)
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
                });

        content.child(groupHeader);
        content.child(clearableList);
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
