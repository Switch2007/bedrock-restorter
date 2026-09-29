package com.restoration.decoder;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.ByteArrayInputStream;

public class BlockEntityDecoder {

    public static void applyBlockEntities(Chunk chunk, byte[] blockEntitiesRaw) {
        if (blockEntitiesRaw == null || blockEntitiesRaw.length == 0) return;

        ByteArrayInputStream bais = new ByteArrayInputStream(blockEntitiesRaw);
        NBTDeserializer deserializer = new NBTDeserializer(true);

        while (bais.available() > 0) {
            try {
                NamedTag namedTag = deserializer.fromStream(bais);
                if (!(namedTag.getTag() instanceof CompoundTag tag)) break;

                int x = tag.getInt("x");
                int y = tag.getInt("y");
                int z = tag.getInt("z");
                String id = tag.getString("id");

                BlockPos pos = new BlockPos(x, y, z);
                BlockEntity be = chunk.getBlockEntity(pos);

                if (be != null) {
                    if ("MobSpawner".equalsIgnoreCase(id) && be instanceof MobSpawnerBlockEntity) {
                        // Спавнер инициализируется в чанке
                    }
                }
            } catch (Exception e) {
                break;
            }
        }
    }
}
