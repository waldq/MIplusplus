package dev.waldq.mipp.item;

import aztech.modern_industrialization.MIComponents;

import dev.technici4n.grandpower.api.ISimpleEnergyItem;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ElectricItem extends Item implements ISimpleEnergyItem {
    private final long capacity;

    public ElectricItem(Properties properties, long capacity) {
        super(properties
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .component(MIComponents.ENERGY, 0L)
        );
        this.capacity = capacity;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(this.getStoredEnergy(stack) / (double) this.getEnergyCapacity(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) { return 0xFF0000; }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }

    @Override
    public DataComponentType<Long> getEnergyComponent() { return MIComponents.ENERGY.get(); }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return this.capacity;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return this.capacity;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return this.capacity;
    }
}
