package cn.ussshenzhou.brighter.mixin;

import cn.ussshenzhou.brighter.util.FakeBlockLightStorage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.lighting.BlockLightEngine;
import net.minecraft.world.lighting.LightEngine;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * @author Tony Yu
 */
@Mixin(BlockLightEngine.class)
public abstract class MixinBlockLightEngine extends LightEngine<FakeBlockLightStorage.FakeBlockDataLayerStorageMap, FakeBlockLightStorage> {
    public MixinBlockLightEngine(IChunkLightProvider p_i51296_1_, LightType p_i51296_2_, FakeBlockLightStorage p_i51296_3_) {
        super(p_i51296_1_, p_i51296_2_, p_i51296_3_);
    }

    @Shadow
    protected abstract int getLightEmission(long p_75509_);

    @Inject(method = "computeLevelFromNeighbor", at = @At("HEAD"), cancellable = true)
    private void brighterGetEdgeLevelHead(long startPos, long endPos, int startLevel, CallbackInfoReturnable<Integer> cir) {
        if (endPos == Long.MAX_VALUE) {
            cir.setReturnValue(15);
        } else if (startPos == Long.MAX_VALUE) {
            cir.setReturnValue(startLevel + 15 - this.getLightEmission(endPos));
        } else if (startLevel >= 15) {
            cir.setReturnValue(startLevel);
        } else {
            int i = Integer.signum(BlockPos.getX(endPos) - BlockPos.getX(startPos));
            int j = Integer.signum(BlockPos.getY(endPos) - BlockPos.getY(startPos));
            int k = Integer.signum(BlockPos.getZ(endPos) - BlockPos.getZ(startPos));
            Direction direction = Direction.fromNormal(i, j, k);
            if (direction == null) {
                cir.setReturnValue(15);
            } else {
                MutableInt mutableint = new MutableInt();
                BlockState blockstate = this.getStateAndOpacity(endPos, mutableint);
                if (mutableint.getValue() >= 15) {
                    cir.setReturnValue(15);
                } else {
                    BlockState blockstate1 = this.getStateAndOpacity(startPos, (MutableInt) null);
                    VoxelShape voxelshape = this.getShape(blockstate1, startPos, direction);
                    VoxelShape voxelshape1 = this.getShape(blockstate, endPos, direction.getOpposite());
                    int l = 1;
                    long prevPos = startPos + (startPos - endPos);
                    try {
                        int prevLevel = this.getLevel(prevPos);
                        if (prevLevel == startLevel - 1&& startLevel != 14) {
                            l = 0;
                        }
                    } catch (Exception ignored) {
                    }
                    l += mutableint.getValue();
                    cir.setReturnValue(VoxelShapes.faceShapeOccludes(voxelshape, voxelshape1) ? 15
                                    : startLevel + l
                            //Math.max(1, mutableint.getValue())
                    );
                }
            }
        }
    }
}
