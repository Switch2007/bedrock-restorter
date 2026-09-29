package com.restoration.world;

import com.restoration.db.BedrockDatabase;
import com.restoration.decoder.BlockEntityDecoder;
import com.restoration.decoder.SubChunkDecoder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class BedrockRestorer {

    public static void restore(ServerWorld world, Chunk chunk, ChunkPos pos) {
        int dimId = (world.getRegistryKey() == ServerWorld.NETHER) ? 1 : ((world.getRegistryKey() == ServerWorld.END) ? 2 : 0);
        int minY = (dimId == 0) ? -4 : 0;
        int maxY = (dimId == 0) ? 19 : 7;

        byte[] biomesData = BedrockDatabase.get(BedrockDatabase.getChunkTagKey(pos.x, pos.z, dimId, (byte) 0x2B));
        Set<Block> detectedBlocks = new HashSet<>();

        for (byte subY = (byte) minY; subY <= maxY; subY++) {
            byte[] rawSub = BedrockDatabase.get(BedrockDatabase.getSubChunkKey(pos.x, pos.z, dimId, subY));
            int sIdx = chunk.sectionCoordToIndex(subY);
            ChunkSection section = chunk.getSection(sIdx);

            if (biomesData != null) {
                BiomeParser.applyBiomes(world, section, biomesData, subY);
            }

            if (rawSub != null && rawSub.length > 0) {
                BlockState[][][] blocks = SubChunkDecoder.decode(rawSub);
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = blocks[x][y][z];
                            if (state != null && !state.isAir()) {
                                section.setBlockState(x, y, z, state, false);
                                detectedBlocks.add(state.getBlock());
                            }
                        }
                    }
                }
            }
        }

        byte[] beData = BedrockDatabase.get(BedrockDatabase.getChunkTagKey(pos.x, pos.z, dimId, (byte) 0x31));
        if (beData != null) {
            BlockEntityDecoder.applyBlockEntities(chunk, beData);
        }

        StructureFixer.identifyAndRegister(world, chunk, pos, detectedBlocks);

        Heightmap.populateHeightmaps(chunk, EnumSet.of(
                Heightmap.Type.MOTION_BLOCKING,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                Heightmap.Type.OCEAN_FLOOR,
                Heightmap.Type.WORLD_SURFACE
        ));

        world.getChunkManager().getLightingProvider().checkBlock(pos.getStartPos());
        chunk.setNeedsSaving(true);
    }
}
