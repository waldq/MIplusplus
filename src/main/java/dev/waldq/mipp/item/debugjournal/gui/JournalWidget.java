package dev.waldq.mipp.item.debugjournal.gui;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;

import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.item.debugjournal.utils.session.GroupMode;

import io.netty.buffer.ByteBuf;

import java.util.*;
import java.util.stream.IntStream;

public class JournalWidget extends ParentWidget<JournalWidget> implements Interactable {
    private final GenericListSyncHandler<ByteBuf, MachineRow> rowHandler;
    private final EnumSyncValue<GroupMode> groupModeValue;
    private GroupMode renderedGroupMode;

    private List<Integer> ungroupedIndices;

    private ListWidget groupedList;

    private final Map<Object, Group> groups = new LinkedHashMap<>();
    private List<Object> lastGroupKeys = List.of();

    private boolean shouldRebuild;

    public JournalWidget(
            GenericListSyncHandler<ByteBuf, MachineRow> rowHandler,
            EnumSyncValue<GroupMode> groupModeValue
    ) {
        this.rowHandler = rowHandler;
        this.groupModeValue = groupModeValue;
        ungroupedIndices = IntStream.range(0, rowHandler.getValue().size()).boxed().toList();
        rowHandler.changeListener(() -> shouldRebuild = true);

        sizeRel(1f);

        rebuild();
    }

    public void updateGroupMode() {
        GroupMode cur = groupModeValue.getValue();
        if (cur != renderedGroupMode) rebuild();
    }

    private void rebuild() {
        GroupMode newGroupMode = groupModeValue.getValue();

        if (newGroupMode != renderedGroupMode) {
            removeAll();

            groups.clear();
            lastGroupKeys = List.of();
            groupedList = null;

            renderedGroupMode = newGroupMode;

            if (renderedGroupMode == GroupMode.NONE) {
                var list = new JournalListWidget(
                        rowHandler,
                        () -> ungroupedIndices,
                        true
                );
                child(list.sizeRel(1f));
            } else {
                groupedList = new ListWidget<>().sizeRel(1f).padding(0);
                child(groupedList);
            }
        }

        if (renderedGroupMode == GroupMode.NONE) {
            int size = rowHandler.getValue().size();
            if (ungroupedIndices.size() != size)
                ungroupedIndices = IntStream.range(0, size).boxed().toList();
        } else {
            updateGroups();
        }
    }

    private void updateGroups() {
        List<MachineRow> rows = rowHandler.getValue();

        List<Object> nextKeys = new ArrayList<>(rows.size());
        boolean changed = rows.size() != lastGroupKeys.size();
        for (int i = 0; i < rows.size(); i++) {
            var key = renderedGroupMode.getFunction().apply(rows.get(i));
            nextKeys.add(key);

            if (!changed && !Objects.equals(lastGroupKeys.get(i), key)) {
                changed = true;
            }
        }
        if (!changed) return;

        Map<Object, List<Integer>> nextIndices = new LinkedHashMap<>();
        for (int i = 0; i < nextKeys.size(); i++) {
            nextIndices.computeIfAbsent(nextKeys.get(i), k -> new ArrayList<>()).add(i);
        }

        var iterator = groups.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();

            if (!nextIndices.containsKey(entry.getKey())) {
                groupedList.remove(entry.getValue().widget);
                iterator.remove();
            }
        }

        List<MachineGroupWidget> orderedGroups = new ArrayList<>(nextIndices.size());
        for (var entry : nextIndices.entrySet()) {
            var key = entry.getKey();
            var indices = entry.getValue();

            Group group = groups.get(key);

            if (group == null) {
                group = new Group(key, indices);
                groups.put(key, group);
                groupedList.child(group.widget);
            } else {
                group.indices = indices;
            }

            orderedGroups.add(group.widget);
        }
        lastGroupKeys = nextKeys;

        List<MachineGroupWidget> children = groupedList.getTypeChildren();

        if (!children.equals(orderedGroups)) {
            for (int i = 0; i < orderedGroups.size(); i++) {
                MachineGroupWidget expected = orderedGroups.get(i);
                int currentIndex = children.indexOf(expected);

                if (currentIndex >= 0 && currentIndex != i) {
                    Collections.swap(children, i, currentIndex);
                }
            }
            groupedList.scheduleResize();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (shouldRebuild) {
            shouldRebuild = false;
            rebuild();
        }
    }

    private class Group {
        private List<Integer> indices;
        private final MachineGroupWidget widget;

        private Group(Object key, List<Integer> indices) {
            this.indices = indices;
            this.widget = new MachineGroupWidget(
                    groupModeValue,
                    key,
                    rowHandler,
                    () -> this.indices
            );
        }
    }
}
