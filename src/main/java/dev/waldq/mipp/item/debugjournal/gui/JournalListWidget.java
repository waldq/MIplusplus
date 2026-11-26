package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.item.debugjournal.utils.MachineRow;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Supplier;

public class JournalListWidget extends ParentWidget<JournalListWidget> {
    private static final int PAGE_LENGTH = 30;
    private static final int GROUPED_LIST_LENGTH = 8;

    private static final int MIDDLE_ROW_HEIGHT = 18;

    private final GenericListSyncHandler<ByteBuf, MachineRow> rowHandler;
    private final Supplier<List<Integer>> indicesSupplier; // or indexes, me no english sorry
    private final boolean expand;
    private int lastIndicesSize;

    private final MachineListWidget list;
    private int pageIndex = 0;
    private final int visibleListLength;

    public JournalListWidget(
            GenericListSyncHandler<ByteBuf, MachineRow> rowHandler,
            Supplier<List<Integer>> indicesSupplier,
            boolean expand
    ) {
        this.rowHandler = rowHandler;
        this.indicesSupplier = indicesSupplier;
        this.expand = expand;
        this.lastIndicesSize = indicesSupplier.get().size();

        this.visibleListLength = expand ? PAGE_LENGTH : GROUPED_LIST_LENGTH;

        if (expand) {
            sizeRel(1f);
        } else {
            coverChildrenHeight();
        }

        list = new MachineListWidget();
        Flow col = Flow.col().widthRel(1f);

        if (expand) {
            col.sizeRel(1f);
        } else {
            col.coverChildrenHeight();
            list.coverChildrenHeight();
        }

        col.child(list.widthRel(1f));
        if (expand) list.heightRelOffset(1f, -MIDDLE_ROW_HEIGHT);

        Flow middleRow = Flow.row()
                .childPadding(4)
                .widthRel(1f)
                .height(MIDDLE_ROW_HEIGHT);
        middleRow.child(createPageButtons()
                .leftRelAnchor(0.5f, 0.5f));

        col.child(middleRow);

        child(col);

        rebuildList();

        onUpdateListener(w -> {
            int currentSize = indicesSupplier.get().size();
            if (currentSize != lastIndicesSize) {
                lastIndicesSize = currentSize;
                rebuildList();
            }
        });
    }

    private void rebuildList() {
        List<Integer> indices = indicesSupplier.get();
        int maxPage = Math.max(0, (indices.size() - 1) / visibleListLength);
        pageIndex = Math.clamp(pageIndex, 0, maxPage);

        int visibleCount = Math.min(visibleListLength, indices.size() - pageIndex * visibleListLength);

        list.removeAll();

        for (int i = 0; i < visibleCount; i++) {
            int ind = i;
            list.child(new MachineRowWidget(() -> row(ind)));
        }
    }

    private MachineRow row(int i) {
        List<Integer> indices = indicesSupplier.get();

        int index = pageIndex * visibleListLength + i;
        if (index < 0 || index >= indices.size()) return null;

        int rowIndex = indices.get(index);
        List<MachineRow> rows = rowHandler.getValue();

        return rowIndex >= 0 && rowIndex < rows.size() ? rows.get(rowIndex) : null;
    }

    private Widget<Flow> createPageButtons() {

        return Flow.row()
                .size(92, 18)
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .childPadding(4)
                .child(new ButtonWidget<>()
                        .size(18)
                        .overlay(Text.comp(Component.literal("<")).alignment(Alignment.Center))
                        .decoration()
                        .onMousePressed((ctx, btn) -> {
                            int maxPage = Math.max(0, (indicesSupplier.get().size() - 1) / visibleListLength);
                            pageIndex = Math.clamp(pageIndex - 1, 0, maxPage);
                            rebuildList();
                            return true;
                        })
                        .excludeAreaInRecipeViewer())
                .child(new TextWidget<>(() -> {
                    int size = indicesSupplier.get().size();
                    int pages = Math.max(1, (size + visibleListLength - 1) / visibleListLength);
                    return Component.literal("%d/%d".formatted(pageIndex + 1, pages));
                })
                        .textAlign(Alignment.Center)
                        .size(48, 18)
                        .excludeAreaInRecipeViewer())
                .child(new ButtonWidget<>()
                        .size(18)
                        .overlay(Text.comp(Component.literal(">")).alignment(Alignment.Center))
                        .decoration()
                        .onMousePressed((ctx, btn) -> {
                            int maxPage = Math.max(0, (indicesSupplier.get().size() - 1) / visibleListLength);
                            pageIndex = Math.clamp(pageIndex + 1, 0, maxPage);
                            rebuildList();
                            return true;
                        })
                        .excludeAreaInRecipeViewer());
    }

    private static class MachineListWidget extends ListWidget<Widget<?>, MachineListWidget> {
        @Override
        public boolean removeAll() {
            return super.removeAll();
        }
    }
}
