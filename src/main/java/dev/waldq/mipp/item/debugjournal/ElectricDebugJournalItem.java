package dev.waldq.mipp.item.debugjournal;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.util.TextHelper;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.PlayerInventoryUIFactory;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ScrollingTextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.DropdownWidget;

import dev.waldq.mipp.MIPP;
import dev.waldq.mipp.MIPPComponents;
import dev.waldq.mipp.client.ClientMachineOverlays;
import dev.waldq.mipp.item.ElectricItem;
import dev.waldq.mipp.item.IItemUIHolder;
import dev.waldq.mipp.item.debugjournal.gui.JournalWidget;
import dev.waldq.mipp.item.debugjournal.gui.ModeDropdownWidget;
import dev.waldq.mipp.item.debugjournal.utils.session.*;
import dev.waldq.mipp.item.debugjournal.utils.MachineRow;
import dev.waldq.mipp.saveddata.DebugJournalSavedData;
import dev.waldq.mipp.utils.CableTierUtils;
import dev.waldq.mipp.utils.GeneralUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.UUID;

import static dev.waldq.mipp.item.debugjournal.utils.session.JournalSession.noRefs;

public class ElectricDebugJournalItem extends ElectricItem implements IItemUIHolder {
    private final CableTier tier;
    private final boolean allowAllMine;
    private final int maxBindings;


    public ElectricDebugJournalItem(Properties properties, CableTier tier, long capacity, int maxBindings) {
        super(properties, capacity);
        this.tier = tier;
        this.allowAllMine = tier == CableTier.HV;
        this.maxBindings = maxBindings;
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
        if (!(level instanceof ServerLevel serverLevel)) {
            if (player.isShiftKeyDown()) {
                ClientMachineOverlays.removeAll();
            }
            return InteractionResultHolder.success(stack);
        }

        var journalUUID = getUUIDComponent(stack);

        var noRefs = noRefs(serverLevel, player, journalUUID, allowAllMine);
        if (noRefs) {
            player.displayClientMessage(MIPP.text().debugJournal().noMachines(), true);
            return InteractionResultHolder.pass(stack);
        }

        if (!player.isCreative()) {
            tryUseEnergy(stack, cost);
        }
        PlayerInventoryUIFactory.INSTANCE.openFromHand(player, usedHand);

        return InteractionResultHolder.sidedSuccess(stack, true);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        var player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        var level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        var be = level.getBlockEntity(pos);
        if (be instanceof MachineBlockEntity) {
            if (player.isShiftKeyDown()) {
                if (level instanceof ServerLevel serverLevel) {
                    var data = DebugJournalSavedData.get(serverLevel);
                    var journalUUID = getUUIDComponent(stack);
                    var result = data.bindOrUnbind(
                            journalUUID,
                            new GlobalPos(serverLevel.dimension(), pos),
                            maxBindings
                    );
                    var formattedPos = Component.literal(GeneralUtils.formatBlockPos(pos));
                    var dim = Component.literal(serverLevel.dimension().location().toString());
                    var t = MIPP.text().debugJournal().bindings();

                    switch (result) {
                        case BOUND -> player.displayClientMessage(t.bound(formattedPos, dim), true);
                        case UNBOUND -> player.displayClientMessage(t.unbound(formattedPos, dim), true);
                        case BOUND_AND_REMOVED -> player.displayClientMessage(t.boundAndRemoved(formattedPos, dim), true);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            // TODO opening individual machines journal
        }
        return InteractionResult.PASS;
    }

    private static UUID getUUIDComponent(ItemStack stack) {
        if (stack.has(MIPPComponents.UUID)) {
            return stack.get(MIPPComponents.UUID);
        }
        var uuid = UUID.randomUUID();
        stack.set(MIPPComponents.UUID, uuid);
        return uuid;
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

        BooleanSyncValue allMineValue = new BooleanSyncValue(session::isSelectAllMine, session::setSelectAllMine).allowC2S();
        syncManager.syncValue("allMine", allMineValue);

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
                        .childPadding(4)
                        .child(Flow.row()
                                .widthRel(1f)
                                .height(18)
                                .child(createModeButtons(
                                        filterValue,
                                        sortValue,
                                        groupModeValue
                                ).left(0))
                                .child(new ToggleButton()
                                        .setEnabledIf(b -> allowAllMine)
                                        .right(0)
                                        .decoration()
                                        .value(allMineValue)))
                        .child(journalWidget
                                .expanded()
                        )
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

    private Widget<Flow> createModeButtons(
            EnumSyncValue<FilterMode> filterValue,
            EnumSyncValue<SortMode> sortValue,
            EnumSyncValue<GroupMode> groupModeValue
    ) {
        return Flow.row()
                .childPadding(4)
                .child(createDropDownWidget(FilterMode.class, filterValue, "filterMenu", GuiTextures.FILTER))
                .child(createDropDownWidget(SortMode.class, sortValue, "sortMenu", GuiTextures.REVERSE))
                .child(createDropDownWidget(GroupMode.class, groupModeValue, "groupMenu", GuiTextures.FOLDER));
    }

    private <T extends Enum<T> & ITextOwner> DropdownWidget createDropDownWidget(
            Class<T> e,
            EnumSyncValue<T> syncValue,
            String name,
            IDrawable icon
    ) {
        return new ModeDropdownWidget<>(name, e)
                .size(18)
                .decoration()
                .options(e.getEnumConstants())
                .optionToWidget((m, b) -> b
                        ? new IDrawable.DrawableWidget(icon) {
                            @Override
                            public boolean canHover() {
                                return false;
                            }
                        }.size(18).center()
                        : new ScrollingTextWidget(m.getText())
                        .size(104, 18)
                        .leftRelAnchor(0.5f, 0.5f)
                        .textAlign(Alignment.Center)
                        .verticalCenter()
                )
                .value(syncValue)
                .tooltipDynamic(r -> r.addLine(syncValue.getValue().getText()))
                .excludeAreaInRecipeViewer();
    }
}
