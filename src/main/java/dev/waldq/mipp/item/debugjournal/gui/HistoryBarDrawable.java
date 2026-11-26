package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPClient;
import dev.waldq.mipp.item.debugjournal.MachineState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class HistoryBarDrawable implements IDrawable {
    private final Supplier<byte[]> historySupplier;
    private final Supplier<short[]> workTicksSupplier;

    private Map<MachineState, Integer> statesWithCounts = new HashMap<>();

    public HistoryBarDrawable(Supplier<byte[]> historySupplier, Supplier<short[]> workTicksSupplier) {
        this.historySupplier = historySupplier;
        this.workTicksSupplier = workTicksSupplier;
    }

    void tooltip(RichTooltip tooltip) {
        updateStateWithCounts();
        if (statesWithCounts.isEmpty()) return;

        tooltip.addLine(MIPP.text().debugJournal().workTime(sum(workTicksSupplier.get()))); //TODO

        statesWithCounts.entrySet().stream()
                .sorted(Comparator.comparing(
                        entry -> entry.getKey().text().getString(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .forEach(entry -> {
                    MachineState state = entry.getKey();

                    tooltip.addDrawable(new Rectangle()
                                    .color(entry.getKey().color())
                                    .asIcon()
                                    .size(8)
                            )
                            .add(Component.literal(" "))
                            .add(state.text())
                            .newLine();

                });
    }

    private void updateStateWithCounts() {
        var history = historySupplier.get();
        var workTicks = workTicksSupplier.get();
        if (history.length != workTicks.length) return;

        var length = history.length;
        statesWithCounts.clear();

        for (int i = 0; i < length; i++) {
            var state = MachineState.values[history[i]];
            statesWithCounts.merge(state, (int) workTicks[i], Integer::sum);
        }
    }

    private Component sum(short[] workTicks) {
        int res = 0;
        for (short bucket : workTicks) {
            res += bucket;
        }
        return Component.literal(String.valueOf(res / 20));
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
