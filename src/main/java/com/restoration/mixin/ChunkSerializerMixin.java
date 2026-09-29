package com.restoration.mixin;

import com.restoration.world.BedrockRestorer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true)
    private static void onDeserialize(ServerWorld world, ChunkPos pos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk chunk = cir.getReturnValue();
        if (chunk == null) return;

        boolean isEmpty = true;
        for (var section : chunk.getSectionArray()) {
            if (!section.isEmpty()) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            BedrockRestorer.restore(world, chunk, pos);
        }
    }
}
