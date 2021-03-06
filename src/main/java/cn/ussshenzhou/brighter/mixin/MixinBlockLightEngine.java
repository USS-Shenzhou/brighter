package cn.ussshenzhou.brighter.mixin;

import cn.ussshenzhou.brighter.util.FakeBlockLightSectionStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
public abstract class MixinBlockLightEngine extends LayerLightEngine<FakeBlockLightSectionStorage.FakeBlockDataLayerStorageMap, FakeBlockLightSectionStorage> {
    public MixinBlockLightEngine(LightChunkGetter p_75640_, LightLayer p_75641_, FakeBlockLightSectionStorage p_75642_) {
        super(p_75640_, p_75641_, p_75642_);
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

                    cir.setReturnValue(Shapes.faceShapeOccludes(voxelshape, voxelshape1) ? 15
                                    : startLevel + l
                            //Math.max(1, mutableint.getValue())
                    );
                }
            }
        }
    }
}
