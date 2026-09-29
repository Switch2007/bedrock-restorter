package com.restoration.decoder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.querz.nbt.tag.CompoundTag;

public class BlockStateMapper {

    public static BlockState map(CompoundTag blockTag) {
        String name = blockTag.getString("name");
        if (name == null || name.isEmpty()) return Blocks.AIR.getDefaultState();

        String javaId = switch (name) {
            case "minecraft:grass" -> "minecraft:short_grass";
            case "minecraft:stone" -> handleStoneVariants(blockTag);
            case "minecraft:flowing_water" -> "minecraft:water";
            case "minecraft:flowing_lava" -> "minecraft:lava";
            default -> name;
        };

        Identifier id = Identifier.tryParse(javaId);
        if (id == null || !Registries.BLOCK.containsId(id)) return Blocks.AIR.getDefaultState();

        Block block = Registries.BLOCK.get(id);
        BlockState state = block.getDefaultState();

        CompoundTag states = blockTag.getCompoundTag("states");
        if (states == null) return state;

        if (states.containsKey("pillar_axis") && state.contains(Properties.AXIS)) {
            String axis = states.getString("pillar_axis");
            state = switch (axis) {
                case "x" -> state.with(Properties.AXIS, Direction.Axis.X);
                case "z" -> state.with(Properties.AXIS, Direction.Axis.Z);
                default -> state.with(Properties.AXIS, Direction.Axis.Y);
            };
        }

        if (states.containsKey("weirdo_direction") && state.contains(Properties.HORIZONTAL_FACING)) {
            int dir = states.getInt("weirdo_direction");
            Direction direction = switch (dir) {
                case 0 -> Direction.EAST;
                case 1 -> Direction.WEST;
                case 2 -> Direction.SOUTH;
                default -> Direction.NORTH;
            };
            state = state.with(Properties.HORIZONTAL_FACING, direction);
        }

        if (states.containsKey("upside_down_bit")) {
            boolean isTop = states.getByte("upside_down_bit") == 1;
            if (state.contains(Properties.BLOCK_HALF)) {
                state = state.with(Properties.BLOCK_HALF, isTop ? BlockHalf.TOP : BlockHalf.BOTTOM);
            } else if (state.contains(Properties.SLAB_TYPE)) {
                state = state.with(Properties.SLAB_TYPE, isTop ? SlabType.TOP : SlabType.BOTTOM);
            }
        }

        return state;
    }

    private static String handleStoneVariants(CompoundTag blockTag) {
        CompoundTag states = blockTag.getCompoundTag("states");
        if (states != null && states.containsKey("stone_type")) {
            String type = states.getString("stone_type");
            return switch (type) {
                case "granite" -> "minecraft:granite";
                case "diorite" -> "minecraft:diorite";
                case "andesite" -> "minecraft:andesite";
                case "granite_smooth" -> "minecraft:polished_granite";
                case "diorite_smooth" -> "minecraft:polished_diorite";
                case "andesite_smooth" -> "minecraft:polished_andesite";
                default -> "minecraft:stone";
            };
        }
        return "minecraft:stone";
    }
}
