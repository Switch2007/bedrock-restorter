package com.restoration.world;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BiomeParser {

    @SuppressWarnings("unchecked")
    public static void applyBiomes(ServerWorld world, ChunkSection section, byte[] data3D, int subChunkIndex) {
        if (data3D == null || data3D.length <= 512) return;

        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        ByteBuffer buffer = ByteBuffer.wrap(data3D).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(512);

        int arrayIndex = subChunkIndex + 4;
        if (arrayIndex < 0 || arrayIndex >= 24) return;

        for (int i = 0; i < arrayIndex; i++) {
            if (!skipSubChunk(buffer)) return;
        }

        if (!buffer.hasRemaining()) return;
        byte header = buffer.get();
        if ((header & 0xFF) == 0xFF) return;

        int bits = (header & 0xFF) >> 1;
        PalettedContainer<RegistryEntry<Biome>> container = (PalettedContainer<RegistryEntry<Biome>>) section.getBiomeContainer();

        if (bits == 0) {
            int pCount = buffer.hasRemaining() ? buffer.getInt() : 1;
            int bId = buffer.getInt();
            RegistryEntry<Biome> entry = getEntry(biomeRegistry, bId);
            for (int qx = 0; qx < 4; qx++) {
                for (int qy = 0; qy < 4; qy++) {
                    for (int qz = 0; qz < 4; qz++) {
                        container.set(qx, qy, qz, entry);
                    }
                }
            }
            return;
        }

        int blocksPerWord = 32 / bits;
        int wordsCount = (4096 + blocksPerWord - 1) / blocksPerWord;

        int[] words = new int[wordsCount];
        for (int i = 0; i < wordsCount; i++) {
            words[i] = buffer.getInt();
        }

        int paletteSize = buffer.getInt();
        List<RegistryEntry<Biome>> palette = new ArrayList<>(paletteSize);
        for (int i = 0; i < paletteSize; i++) {
            palette.add(getEntry(biomeRegistry, buffer.getInt()));
        }

        int mask = (1 << bits) - 1;
        for (int qx = 0; qx < 4; qx++) {
            for (int qy = 0; qy < 4; qy++) {
                for (int qz = 0; qz < 4; qz++) {
                    int bx = qx * 4 + 2;
                    int by = qy * 4 + 2;
                    int bz = qz * 4 + 2;

                    int idx = (bx << 8) | (bz << 4) | by;
                    int wordIdx = idx / blocksPerWord;
                    int bitOffset = (idx % blocksPerWord) * bits;
                    int pIdx = (words[wordIdx] >>> bitOffset) & mask;

                    RegistryEntry<Biome> entry = (pIdx < palette.size()) ? palette.get(pIdx) : biomeRegistry.entryOf(BiomeKeys.PLAINS);
                    container.set(qx, qy, qz, entry);
                }
            }
        }
    }

    private static boolean skipSubChunk(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) return false;
        byte header = buffer.get();
        if ((header & 0xFF) == 0xFF) return true;

        int bits = (header & 0xFF) >> 1;
        if (bits == 0) {
            buffer.getInt();
            buffer.getInt();
            return true;
        }

        int bpw = 32 / bits;
        int wc = (4096 + bpw - 1) / bpw;
        buffer.position(buffer.position() + wc * 4);
        int ps = buffer.getInt();
        buffer.position(buffer.position() + ps * 4);
        return true;
    }

    private static RegistryEntry<Biome> getEntry(Registry<Biome> registry, int id) {
        Identifier javaId = BiomeMapping.getJavaIdentifier(id);
        return registry.getEntry(javaId).orElseGet(() -> registry.entryOf(BiomeKeys.PLAINS));
    }
}
