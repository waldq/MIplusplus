package dev.waldq.mipp.item.debugjournal.utils.session;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.layout.Flow;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.item.debugjournal.MachineState;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Function;

public enum GroupMode implements ITextOwner {
    NONE(null, MIPP.text().debugJournal().groups().none()),
    DIMENSION(MachineRow::dimension, MIPP.text().debugJournal().groups().dimension()),
    STATE(MachineRow::getMainHistoryState, MIPP.text().debugJournal().groups().state()),
    BLOCK(MachineRow::machine, MIPP.text().debugJournal().groups().block());

    public static final GroupMode[] values = GroupMode.values();

    private final Function<MachineRow, ?> function;
    private final MutableComponent text;

    public Function<MachineRow, ?> getFunction() {
        return function;
    }

    public MutableComponent getText() {
        return text;
    }

    public Widget<Flow> createGroupHeader(Object key) {
        return switch (this) {
            case DIMENSION -> {
                if (key instanceof ResourceKey<?> dimension) {
                    // datamap maybe later TODO
                    if (dimension.equals(Level.OVERWORLD)) {
                        yield createBlockWidget(Blocks.GRASS_BLOCK, Component.literal(Level.OVERWORLD.location().toString()));
                    } else if (dimension.equals(Level.NETHER)) {
                        yield createBlockWidget(Blocks.NETHERRACK, Component.literal(Level.NETHER.location().toString()));
                    } else if (dimension.equals(Level.END)) {
                        yield createBlockWidget(Blocks.END_STONE, Component.literal(Level.END.location().toString()));
                    }
                }
                yield Flow.row().child(Text.comp(Component.literal(String.valueOf(key))).asWidget());
            }
            case STATE -> {
                if (key instanceof MachineState state) {
                    yield Flow.row()
                            .sizeRel(1f)
                            .childPadding(4)
                            .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                            .padding(4, 0, 0, 0)
                            .child(new Rectangle().color(state.color()).asWidget().size(8))
                            .child(Text.dynamic(state::text).asWidget());
                }
                yield Flow.row().child(Text.comp(Component.literal(String.valueOf(key))).asWidget());
            }
            case BLOCK -> {
                if (key instanceof Block block) {
                    yield createBlockWidget(block, block.getName());
                }
                yield Flow.row().child(Text.comp(Component.literal(String.valueOf(key))).asWidget());
            }
            default -> throw new IllegalArgumentException("GroupMode cannot have a header for " + this);
        };
    }

    private static Widget<Flow> createBlockWidget(Block block, MutableComponent comp) {
        return Flow.row()
                .sizeRel(1f)
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .padding(4, 0, 0, 0)
                .child(new ItemDrawable(block).asWidget().size(16))
                .child(Text.comp(comp).asWidget());
    }

    GroupMode(Function<MachineRow, ?> function, MutableComponent text) {
        this.function = function;
        this.text = text;
    }
}
