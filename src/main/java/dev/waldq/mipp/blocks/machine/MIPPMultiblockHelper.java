package dev.waldq.mipp.blocks.machine;

import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public interface MIPPMultiblockHelper {
    HatchFlags HATCHES = new HatchFlags.Builder()
            .with(
                    HatchTypes.ITEM_INPUT, HatchTypes.ITEM_OUTPUT,
                    HatchTypes.FLUID_INPUT, HatchTypes.FLUID_OUTPUT,
                    HatchTypes.ENERGY_INPUT
            )
            .build();

    SimpleMember GLASS_MEMBER = new SimpleMember() {
        @Override
        public boolean matchesState(BlockState state, BlockEntity blockEntity) {
            return state.is(Tags.Blocks.GLASS_BLOCKS);
        }

        @Override
        public BlockEntity newBlockEntity(RegistryAccess registries, Level level, BlockPos pos, BlockState state) {
            return null;
        }

        @Override
        public BlockState getPreviewState() {
            return Blocks.GLASS.defaultBlockState();
        }
    };

    List<List<String>> pattern();

    Map<BiPredicate<Character, Integer>, SimpleMember> materialRules();

    default Map<BiPredicate<Character, Integer>, HatchFlags> getHatchPredicate() {
        return Map.of((ch, y) -> true, getHatches());
    }

    default HatchFlags getHatches() {
        return HATCHES;
    }

    default int getControllerXOffset() {
        return 0;
    }

    default void buildShapeTemplates(ShapeTemplate[] shapeTemplates) {}

}
