package com.restoration.decoder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SubChunkDecoder {

    public static BlockState[][][] decode(byte[] rawData) {
        BlockState[][][] blocks = new BlockState[16][16][16];
        if (rawData == null || rawData.length < 2) return blocks;

        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
        byte version = buffer.get();
        byte layersCount = (version >= 8) ? buffer.get() : 1;
        if (version == 9) buffer.get();

        if (layersCount <= 0) return blocks;

        decodeLayer(buffer, blocks, false);

        if (layersCount > 1 && buffer.hasRemaining()) {
            decodeLayer(buffer, blocks, true);
        }

        return blocks;
    }

    private static void decodeLayer(ByteBuffer buffer, BlockState[][][] blocks, boolean isWaterLayer) {
        byte storageHeader = buffer.get();
        int bitsPerBlock = (storageHeader & 0xFF) >> 1;

        if (bitsPerBlock == 0) {
            int paletteSize = buffer.getInt();
            List<BlockState> palette = readPalette(buffer, paletteSize);
            BlockState state = palette.isEmpty() ? Blocks.AIR.getDefaultState() : palette.get(0);
            applyUniformBlock(blocks, state, isWaterLayer);
            return;
        }

        int blocksPerWord = 32 / bitsPerBlock;
        int wordsCount = (4096 + blocksPerWord - 1) / blocksPerWord;

        int[] words = new int[wordsCount];
        for (int i = 0; i < wordsCount; i++) {
            words[i] = buffer.getInt();
        }

        int paletteSize = buffer.getInt();
        List<BlockState> palette = readPalette(buffer, paletteSize);

        int mask = (1 << bitsPerBlock) - 1;
        int blockIndex = 0;
        int[] indices = new int[4096];

        for (int word : words) {
            for (int b = 0; b < blocksPerWord && blockIndex < 4096; b++) {
                indices[blockIndex++] = (word >>> (b * bitsPerBlock)) & mask;
            }
        }

        for (int i = 0; i < 4096; i++) {
            int x = (i >> 8) & 0xF;
            int z = (i >> 4) & 0xF;
            int y = i & 0xF;

            int pIdx = indices[i];
            BlockState layerState = (pIdx < palette.size()) ? palette.get(pIdx) : Blocks.AIR.getDefaultState();

            if (!isWaterLayer) {
                blocks[x][y][z] = layerState;
            } else if (layerState.isOf(Blocks.WATER)) {
                BlockState base = blocks[x][y][z];
                if (base != null && base.contains(Properties.WATERLOGGED)) {
                    blocks[x][y][z] = base.with(Properties.WATERLOGGED, true);
                }
            }
        }
    }

    private static void applyUniformBlock(BlockState[][][] blocks, BlockState state, boolean isWaterLayer) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (!isWaterLayer) {
                        blocks[x][y][z] = state;
                    } else if (state.isOf(Blocks.WATER) && blocks[x][y][z] != null && blocks[x][y][z].contains(Properties.WATERLOGGED)) {
                        blocks[x][y][z] = blocks[x][y][z].with(Properties.WATERLOGGED, true);
                    }
                }
            }
        }
    }

    private static List<BlockState> readPalette(ByteBuffer buffer, int size) {
        List<BlockState> list = new ArrayList<>(size);
        byte[] nbtBytes = new byte[buffer.remaining()];
        buffer.get(nbtBytes);

        ByteArrayInputStream bais = new ByteArrayInputStream(nbtBytes);
        NBTDeserializer deserializer = new NBTDeserializer(true);

        for (int i = 0; i < size; i++) {
            try {
                NamedTag tag = deserializer.fromStream(bais);
                if (tag.getTag() instanceof CompoundTag compound) {
                    list.add(BlockStateMapper.map(compound));
                } else {
                    list.add(Blocks.AIR.getDefaultState());
                }
            } catch (Exception e) {
                list.add(Blocks.AIR.getDefaultState());
            }
        }
        return list;
    }
}
