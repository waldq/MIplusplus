package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ScrollingTextWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.client.ClientMachineOverlays;
import dev.waldq.mipp.item.debugjournal.MachineState;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.utils.GeneralUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class MachineRowWidget extends ParentWidget<MachineRowWidget> {
    private static final int SIZE = 20;

    private final Flow content;
    private final Widget<Flow> rowCompact;
    private final ClearableParentWidget rowExpanded;
    private Widget<Flow> rowInfo;

    public MachineRowWidget(Supplier<MachineRow> rowSupplier) {
        widthRel(1f).coverChildrenHeight();

        content = Flow.col()
                .widthRel(1f)
                .coverChildrenHeight();

        child(content);

        rowExpanded = new ClearableParentWidget()
                .widthRel(1f)
                .height(0);

        rowCompact = createMachineRow(rowSupplier)
                .widthRel(1f)
                .height(SIZE);

        content.child(rowCompact);
        content.child(rowExpanded);
    }

    private Widget<Flow> createMachineRow(Supplier<MachineRow> rowSupplier) {
        ItemDrawable item = new ItemDrawable();
        IDrawable icon = (ctx, x, y, w, h, theme) -> {
            MachineRow r = rowSupplier.get();
            if (r != null) item.item(r.machine()).draw(ctx, x, y, w, h, theme);
        };
        IDrawable stateSquare = (ctx, x, y, w, h, theme) -> {
            MachineRow r = rowSupplier.get();
            if (r != null) ctx.getGraphics().fill(x, y, x + w, y + h, MachineState.values[r.state()].color());
        };
        HistoryBarDrawable history = new HistoryBarDrawable(
                () -> {
                    MachineRow r = rowSupplier.get();
                    return r == null ? new byte[0] : r.history();
                },
                () -> {
                    MachineRow r = rowSupplier.get();
                    return r == null ? new short[0] : r.workTicks();
                }
        );

        return Flow.row()
                .setEnabledIf(w -> rowSupplier.get() != null)
                .child(new ButtonWidget<>()
                        .widthRelOffset(1f, -SIZE).height(SIZE)
                        .child(
                                Flow.row()
                                        .sizeRel(1f)
                                        .childPadding(4)
                                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                        .padding(4, 0, 0, 0)
                                        .child(icon.asWidget().size(16))
                                        .child(Text.dynamic(() -> {
                                            MachineRow r = rowSupplier.get();
                                            return r == null ? Component.empty() : r.machine().getName();
                                        }).asWidget().width(140))
                                        .child(stateSquare.asWidget().size(8))
                                        .child(new ScrollingTextWidget(Text.dynamic(() -> {
                                            MachineRow r = rowSupplier.get();
                                            return r == null ? Component.empty() : MachineState.values[r.state()].text();
                                        })).size(90, 16))
                                        .child(history
                                                .asWidget()
                                                .expanded()
                                                .height(8)
                                                .tooltipAutoUpdate(true)
                                                .tooltipDynamic(history::tooltip)
                                        )

                        )
                        .onMousePressed((ctx, btn) -> {
                            if (rowInfo == null) {
                                rowInfo = createExpanded(rowSupplier);
                                rowExpanded.child(rowInfo.widthRel(0.99f).right(0));
                                rowExpanded.coverChildrenHeight();
                            } else {
                                rowExpanded.clear();
                                rowExpanded.coverChildrenHeight(-1).height(0);
                                rowInfo = null;
                            }
                            scheduleResize();
                            return true;
                        })
                )
                .child(new ButtonWidget<>()
                        .size(SIZE)
                        .overlay(GuiTextures.VISIBLE)
                        .onMousePressed((context, button) -> {
                            var row = rowSupplier.get();
                            if (row == null) return false;
                            return ClientMachineOverlays.addOrRemoveMachine(row);
                        })
                        .tooltip(r -> r.addLine(MIPP.text().debugJournal().highlights().highlight())));
    }

    private Widget<Flow> createExpanded(Supplier<MachineRow> rowSupplier) {
        var j = MIPP.text().debugJournal().expanded();
        return Flow.row()
                .widthRel(1f)
                .height(32)
                .wrap()
                .crossAxisChildPadding(4)
                .childPadding(40)
                .padding(24, 6, 6, 6)
                .child(new TextWidget<>(() -> {
                    var row = rowSupplier.get();
                    return row == null ? Component.empty()
                            : j.blockPos(Component.literal(GeneralUtils.formatBlockPos(BlockPos.of(row.pos()))));
                }).height(10))
                .child(new TextWidget<>(() -> {
                    var row = rowSupplier.get();
                    return row == null ? Component.empty()
                            : j.dimension(Component.literal(row.dimension().location().toString()));
                }).height(10))
                .child(new TextWidget<>(() -> {
                    var row = rowSupplier.get();
                    return row == null || row.cableTier() == null ? Component.empty()
                            : j.energyTier(row.cableTier().shortEnglishName());
                }).height(10))
                .child(new TextWidget<>(() -> {
                    var row = rowSupplier.get();
                    return row == null ? Component.empty()
                            : j.overdriving(Component.literal(String.valueOf(row.isOverdriving())));
                }).height(10));
    }
}
