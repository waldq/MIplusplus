package dev.waldq.mipp.item;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.PlayerInventoryUIFactory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;


/*
 * This file is adapted code originally part of GregTech:CEu, hosted at https://github.com/GregTechCEu/GregTech-Modern
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */
public interface IItemUIHolder extends IUIHolder<PlayerInventoryGuiData<?>> {

    default boolean shouldOpenUI() {
        return true;
    }

    // These open GUI immediately upon being called with super.use() or super.useOn()
    default InteractionResult useOn(UseOnContext context) {
        if (!shouldOpenUI()) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(context.getHand());
        }

        if (!context.getLevel().isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHand(context.getPlayer(), context.getHand());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!shouldOpenUI()) {
            ItemStack itemStack = player.getItemInHand(hand);
            FoodProperties foodproperties = itemStack.getFoodProperties(player);
            if (foodproperties != null) {
                if (player.canEat(foodproperties.canAlwaysEat())) {
                    player.startUsingItem(hand);
                    return InteractionResultHolder.consume(itemStack);
                }
                return InteractionResultHolder.fail(itemStack);
            }
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(hand);
        }

        if (!level.isClientSide()) {
            PlayerInventoryUIFactory.INSTANCE.openFromHand(player, hand);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}