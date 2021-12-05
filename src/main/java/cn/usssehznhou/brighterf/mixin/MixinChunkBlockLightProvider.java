package cn.usssehznhou.brighterf.mixin;

import cn.usssehznhou.brighterf.util.FakeBlockLightStorage;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tony Yu
 */
@Mixin(ChunkBlockLightProvider.class)
public abstract class MixinChunkBlockLightProvider extends ChunkLightProvider<FakeBlockLightStorage.Data, FakeBlockLightStorage> {
    public MixinChunkBlockLightProvider(ChunkProvider chunkProvider, LightType type, FakeBlockLightStorage lightStorage) {
        super(chunkProvider, type, lightStorage);
    }

    @Shadow
    protected abstract int getLightSourceLuminance(long blockPos);

    @Inject(method = "getPropagatedLevel", at = @At("HEAD"), cancellable = true)
    protected void brighterFGetPropagatedLevel(long sourceId, long targetId, int level, CallbackInfoReturnable<Integer> cir) {
        //fabric logic causes NullPointerException at #69 direction.getOpposite(), use forge logic instead.
        if (targetId == Long.MAX_VALUE) {
            cir.setReturnValue(15);
        } else if (sourceId == Long.MAX_VALUE) {
            cir.setReturnValue(level + 15 - this.getLightSourceLuminance(targetId));
        } else if (level >= 15) {
            cir.setReturnValue(level);
        } else {
            int i = Integer.signum(BlockPos.unpackLongX(targetId) - BlockPos.unpackLongX(sourceId));
            int j = Integer.signum(BlockPos.unpackLongY(targetId) - BlockPos.unpackLongY(sourceId));
            int k = Integer.signum(BlockPos.unpackLongZ(targetId) - BlockPos.unpackLongZ(sourceId));
            Direction direction = Direction.fromVector(i, j, k);
            if (direction == null) {
                cir.setReturnValue(15);
            } else {
                MutableInt mutableint = new MutableInt();
                BlockState blockstate = this.getStateForLighting(targetId, mutableint);
                if (mutableint.getValue() >= 15) {
                    cir.setReturnValue(15);
                } else {
                    BlockState blockstate1 = this.getStateForLighting(sourceId, (MutableInt) null);
                    VoxelShape voxelshape = this.getOpaqueShape(blockstate1, sourceId, direction);
                    VoxelShape voxelshape1 = this.getOpaqueShape(blockstate, targetId, direction.getOpposite());
                    int l = 1;
                    long prevPos = sourceId + (sourceId - targetId);
                    try {
                        int prevLevel = this.getLevel(prevPos);
                        if (prevLevel == level - 1&& level != 14) {
                            l = 0;
                        }
                    } catch (Exception ignored) {
                    }
                    l += mutableint.getValue();

                    cir.setReturnValue(VoxelShapes.unionCoversFullCube(voxelshape,voxelshape1) ? 15
                                    : level + l
                            //Math.max(1, mutableint.getValue())
                    );
                }
            }
        }
    }
}
