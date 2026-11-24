package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.MIPPClient;
import dev.waldq.mipp.item.debugjournal.MachineState;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class JournalListWidget extends ParentWidget<JournalListWidget> implements Interactable {
    private static final int PAGE_LENGTH = 30;
    private static final int GROUPED_LIST_LENGTH = 8;

    private static final int MIDDLE_ROW_HEIGHT = 18;

    private final GenericListSyncHandler<ByteBuf, MachineRow> rowHandler;
    private final Supplier<List<Integer>> indicesSupplier; // or indexes, me no english sorry
    private final boolean expand;
    private int lastIndicesSize;
    private int lastPageLength = -1;

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
        }

        col.child(list.widthRel(1f));
        if (expand) list.heightRelOffset(1f, -MIDDLE_ROW_HEIGHT);

        Flow middleRow = Flow.row()
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
        if (visibleCount == lastPageLength) return;

        lastPageLength = visibleCount;
        list.removeAll();
        if (!expand) {
            list.height(visibleCount * 20);
        }

        for (int i = 0; i < visibleCount; i++) {
            list.child(createMachineRowEntry(i));
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

    private ButtonWidget<?> createMachineRowEntry(int i) {
        ItemDrawable item = new ItemDrawable();
        IDrawable icon = (ctx, x, y, w, h, theme) -> {
            MachineRow r = row(i);
            if (r != null) item.item(r.machine()).draw(ctx, x, y, w, h, theme);
        };
        IDrawable stateSquare = (ctx, x, y, w, h, theme) -> {
            MachineRow r = row(i);
            if (r != null) ctx.getGraphics().fill(x, y, x + w, y + h, MachineState.values[r.state()].color());
        };

        return new ButtonWidget<>()
                .widthRel(1f).height(20)
                .setEnabledIf(w -> row(i) != null)
                .child(
                        Flow.row()
                                .sizeRel(1f)
                                .childPadding(4)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .padding(4, 0, 0, 0)
                                .child(icon.asWidget().size(16))
                                .child(Text.dynamic(() -> {
                                    MachineRow r = row(i);
                                    return r == null ? Component.empty() : r.machine().getName();
                                }).asWidget().width(140))
                                .child(stateSquare.asWidget().size(8))
                                .child(Text.dynamic(() -> {
                                    MachineRow r = row(i);
                                    return r == null ? Component.empty() : MachineState.values[r.state()].text();
                                }).asWidget().width(90))
                                .child(new HistoryBarDrawable(() -> {
                                    MachineRow r = row(i);
                                    return r == null ? new byte[0] : r.history();
                                }).asWidget().expanded().height(8))

                );
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

    public static class HistoryBarDrawable implements IDrawable {
        private final Supplier<byte[]> historySupplier;

        public HistoryBarDrawable(Supplier<byte[]> historySupplier) {
            this.historySupplier = historySupplier;
        }

        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            byte[] history = historySupplier.get();
            int n = Math.min(history.length, MIPPClient.config().debugJournal().maxBucketsShown());
            if (n == 0) return;

            GuiGraphics graphics = context.getGraphics();
            int gap = 1;
            int segmentWidth = (width - (n - 1) * gap) / n;

            if (segmentWidth <= 0) return;

            int x0 = x;

            for (int i = 0; i < n; i++) {
                graphics.fill(
                        x0, y,
                        x0 + segmentWidth, y + height,
                        MachineState.values[history[i]].color()
                );

                x0 += segmentWidth + gap;
            }
        }
    }
}
