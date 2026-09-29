package com.restoration.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.HashSet;
import java.util.Set;

public class StructureFixer {

    public static class RestoredPiece extends StructurePiece {
        public RestoredPiece(BlockBox box) {
            super(StructurePieceType.JIGSAW, 0, box);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {}

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                             ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox,
                             ChunkPos chunkPos, BlockPos pivot) {}
    }

    public static void identifyAndRegister(ServerWorld world, Chunk chunk, ChunkPos pos, Set<Block> blocks) {
        Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Set<RegistryKey<Structure>> detected = new HashSet<>();

        if (blocks.contains(Blocks.DARK_OAK_PLANKS) && blocks.contains(Blocks.COBBLESTONE)) {
            detected.add(StructureKeys.PILLAGER_OUTPOST);
        }
        if (blocks.contains(Blocks.NETHER_BRICKS) || blocks.contains(Blocks.NETHER_BRICK_FENCE)) {
            detected.add(StructureKeys.FORTRESS);
        }
        if (blocks.contains(Blocks.POLISHED_BLACKSTONE_BRICKS) || blocks.contains(Blocks.GILDED_BLACKSTONE)) {
            detected.add(StructureKeys.BASTION_REMNANT);
        }
        if (blocks.contains(Blocks.CAULDRON) && blocks.contains(Blocks.CRAFTING_TABLE)) {
            detected.add(StructureKeys.SWAMP_HUT);
        }

        for (RegistryKey<Structure> key : detected) {
            Structure struct = registry.get(key);
            if (struct == null) continue;

            BlockBox box = new BlockBox(pos.getStartX(), world.getBottomY(), pos.getStartZ(), pos.getEndX(), world.getTopY(), pos.getEndZ());
            StructureStart start = new StructureStart(struct, pos, 0, box);
            start.getChildren().add(new RestoredPiece(box));

            chunk.setStructureStart(struct, start);
            chunk.addStructureReference(struct, pos.toLong());
        }
    }
}
