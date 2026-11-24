package dev.waldq.mipp.item.debugjournal;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.util.TextHelper;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.PlayerInventoryUIFactory;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.DropdownWidget;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPComponents;
import dev.waldq.mipp.item.ElectricItem;
import dev.waldq.mipp.item.IItemUIHolder;
import dev.waldq.mipp.item.debugjournal.gui.JournalWidget;
import dev.waldq.mipp.item.debugjournal.utils.session.JournalSession;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.item.debugjournal.utils.session.FilterMode;
import dev.waldq.mipp.item.debugjournal.utils.session.GroupMode;
import dev.waldq.mipp.item.debugjournal.utils.session.SortMode;
import dev.waldq.mipp.utils.CableTierUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.UUID;

import static dev.waldq.mipp.item.debugjournal.utils.session.JournalSession.getRefs;

public class ElectricDebugJournalItem extends ElectricItem implements IItemUIHolder {
    private final CableTier tier;
    private final boolean allowAllMine;

    public ElectricDebugJournalItem(Properties properties, CableTier tier, long capacity) {
        super(properties, capacity);
        this.tier = tier;
        this.allowAllMine = tier == CableTier.HV;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        long cost = CableTierUtils.CableNumbers.get(tier).cost();

        if (!player.isCreative() && getStoredEnergy(stack) < cost) {
            if (!level.isClientSide()) {
                var amount = TextHelper.getAmount(cost);
                player.displayClientMessage(MIPP.text().debugJournal().energyNotice(MIText.Eu.text(amount.digit(), amount.unit())), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            if (level instanceof ServerLevel serverLevel) {
                var journalUUID = stack.get(MIPPComponents.UUID);
                if (journalUUID == null) {
                    journalUUID = UUID.randomUUID();
                    stack.set(MIPPComponents.UUID, journalUUID);
                }

                var refs = getRefs(serverLevel, player, journalUUID, allowAllMine);
                if (refs.isEmpty()) {
                    player.displayClientMessage(MIPP.text().debugJournal().noMachines(), true);
                    return InteractionResultHolder.pass(stack);
                }

                if (!player.isCreative()) {
                    tryUseEnergy(stack, cost);
                }
                PlayerInventoryUIFactory.INSTANCE.openFromHand(player, usedHand);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, true);
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new ModularScreen(MIPP.ID, mainPanel);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        Player player = data.getPlayer();

        JournalSession session = new JournalSession(player, data.getUsedItemStack().get(MIPPComponents.UUID), allowAllMine);

        EnumSyncValue<SortMode> sortValue = new EnumSyncValue<>(SortMode.class, session::getSort, session::setSort).allowC2S();
        syncManager.syncValue("sort", sortValue);

        EnumSyncValue<FilterMode> filterValue = new EnumSyncValue<>(FilterMode.class, session::getFilter, session::setFilter).allowC2S();
        syncManager.syncValue("filter", filterValue);

        EnumSyncValue<GroupMode> groupModeValue = new EnumSyncValue<>(GroupMode.class, session::getGroupMode, m -> {
            filterValue.setValue(FilterMode.NONE, true, true);
            session.setGroupMode(m);
        }).allowC2S();
        syncManager.syncValue("groupMode", groupModeValue);

        BooleanSyncValue isUpdatingValue = new BooleanSyncValue(session::isUpdating, session::setUpdating).allowC2S();
        syncManager.syncValue("isUpdating", isUpdatingValue);

        IntSyncValue refreshCounterValue = new IntSyncValue(session::getRefreshCounter, session::setRefreshCounter).allowC2S();
        syncManager.syncValue("refreshCounter", refreshCounterValue);

        GenericListSyncHandler<ByteBuf, MachineRow> rowHandler = GenericListSyncHandler.<ByteBuf, MachineRow>builder()
                .getter(session::getRows)
                .serializer(MachineRow.STREAM_CODEC)
                .deserializer(MachineRow.STREAM_CODEC)
                .build();
        syncManager.syncValue("rows", rowHandler);

        JournalWidget journalWidget = new JournalWidget(rowHandler, groupModeValue).onUpdateListener(JournalWidget::updateGroupMode);

        return ModularPanel.defaultPanel("electric_debug_journal", 500, 300)
                .padding(4)
                .child(Flow.col()
                        .child(journalWidget
                                .expanded()
                        )
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .wrap()
                                .children(
                                        Arrays.asList(MachineState.values), this::createStateEntry
                                )
                        )
                )
                .child(new DropdownWidget<>("groupMenu", GroupMode.class)
                        .size(110, 18)
                        .top(-14).leftRelAnchor(0f, 1f)
                        .decoration()
                        .options(GroupMode.values)
                        .optionToWidget((m, b) -> Text.comp(m.getText()).asWidget().center())
                        .value(groupModeValue)
                        .excludeAreaInRecipeViewer()
                )
                .child(new DropdownWidget<>("sortMenu", SortMode.class)
                        .size(110, 18)
                        .top(4).leftRelAnchor(0f, 1f)
                        .decoration()
                        .options(SortMode.values)
                        .optionToWidget((m, b) -> Text.comp(m.getText()).asWidget().center())
                        .value(sortValue)
                        .excludeAreaInRecipeViewer()
                )
                .child(new DropdownWidget<>("filterMenu", FilterMode.class)
                        .size(110, 18)
                        .top(22).leftRelAnchor(0f, 1f)
                        .decoration()
                        .options(FilterMode.values)
                        .optionToWidget((m, b) -> Text.comp(m.getText()).asWidget().center())
                        .value(filterValue)
                        .excludeAreaInRecipeViewer()
                )
                .child(new ToggleButton()
                        .top(76).leftRelAnchor(0f, 1f)
                        .decoration()
                        .value(isUpdatingValue)
                        .excludeAreaInRecipeViewer())
                .child(new ButtonWidget<>()
                        .top(94).leftRelAnchor(0f, 1f)
                        .decoration()
                        .onMousePressed((context, button) -> {
                            refreshCounterValue.setIntValue(refreshCounterValue.getIntValue() + 1, true, true);
                            return true;
                        })
                        .excludeAreaInRecipeViewer()
                );

    }

    private Widget<Flow> createStateEntry(MachineState state) {
        IDrawable stateSquare = (ctx, x, y, w, h, theme) -> {
            ctx.getGraphics().fill(x, y, x + w, y + h, state.color());
        };
        return Flow.row()
                .size(100, 10)
                .childPadding(4)
                .child(stateSquare.asWidget().size(8))
                .child(Text.comp(state.text()).asWidget());
    }
}
