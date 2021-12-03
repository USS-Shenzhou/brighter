package cn.ussshenzhou.brighter.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftGame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.*;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Tony Yu
 */
@Mixin(BlockLightEngine.class)
public abstract class MixinBlockLightEngine extends LightEngine<BlockLightStorage.StorageMap, BlockLightStorage> {

    public MixinBlockLightEngine(IChunkLightProvider chunkLightProvider, LightType lightTypeIn, BlockLightStorage storageIn) {
        super(chunkLightProvider, lightTypeIn, storageIn);
    }

    @Inject(method = "computeLevel", at = @At("RETURN"), remap = false, cancellable = true)
    private void brighterComputeLevel(long pos, long excludedSourcePos, int level, CallbackInfoReturnable<Integer> cir) {
    }

    @Inject(method = "getEdgeLevel", at = @At("RETURN"), remap = false, cancellable = true)
    private void brighterGetEdgeLevel(long startPos, long endPos, int startLevel, CallbackInfoReturnable<Integer> cir) {
        //cir.setReturnValue(-1);
    }

    @Shadow(remap = false)
    @Final
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    @Shadow(remap = false)
    private int getLightValue(long worldPos) {
        int i = BlockPos.unpackX(worldPos);
        int j = BlockPos.unpackY(worldPos);
        int k = BlockPos.unpackZ(worldPos);
        IBlockReader iblockreader = this.chunkProvider.getChunkForLight(i >> 4, k >> 4);
        return iblockreader != null ? iblockreader.getLightValue(this.mutablePos.setPos(i, j, k)) : 0;
    }

    @Inject(method = "getEdgeLevel", at = @At("HEAD"), remap = false, cancellable = true)
    private void brighterGetEdgeLevelHead(long startPos, long endPos, int startLevel, CallbackInfoReturnable<Integer> cir) {
        if (endPos == Long.MAX_VALUE) {
            cir.setReturnValue(15);
        } else if (startPos == Long.MAX_VALUE) {
            cir.setReturnValue(startLevel + 15 - this.getLightValue(endPos));
        } else if (startLevel >= 15) {
            cir.setReturnValue(startLevel);
        } else {
            int i = Integer.signum(BlockPos.unpackX(endPos) - BlockPos.unpackX(startPos));
            int j = Integer.signum(BlockPos.unpackY(endPos) - BlockPos.unpackY(startPos));
            int k = Integer.signum(BlockPos.unpackZ(endPos) - BlockPos.unpackZ(startPos));
            Direction direction = Direction.byLong(i, j, k);
            if (direction == null) {
                cir.setReturnValue(15);
            } else {
                MutableInt mutableint = new MutableInt();
                BlockState blockstate = this.getBlockAndOpacity(endPos, mutableint);
                if (mutableint.getValue() >= 15) {
                    cir.setReturnValue(15);
                } else {
                    BlockState blockstate1 = this.getBlockAndOpacity(startPos, (MutableInt) null);
                    VoxelShape voxelshape = this.getVoxelShape(blockstate1, startPos, direction);
                    VoxelShape voxelshape1 = this.getVoxelShape(blockstate, endPos, direction.getOpposite());


                    int l = 1;
                    long prevPos = startPos + (startPos - endPos);
                    long prevLevel = 15;
                    try {
                        prevLevel = 15 - ((SectionLightStorageAccessor) storage).callGetLight(prevPos);
                    } catch (NullPointerException ignored) {
                    }
                    if (prevLevel == startLevel - 1 ) {
                        l = 0;
                    }

                    l += mutableint.getValue();

                    int a = (int) (Math.random() * 100000);
                    if (endPos == BlockPos.pack(-39, 4, -123)) {
                        LogManager.getLogger().info(a + "---" + "prevPos---" + BlockPos.unpackX(prevPos) + "---" + BlockPos.unpackY(prevPos) + "---" + BlockPos.unpackZ(prevPos) + "---" + prevLevel);
                        LogManager.getLogger().info(a + "---" + "startPos---" + BlockPos.unpackX(startPos) + "---" + BlockPos.unpackY(startPos) + "---" + BlockPos.unpackZ(startPos) + "---" + startLevel);
                        LogManager.getLogger().info(a + "---" + "endPos---" + BlockPos.unpackX(endPos) + "---" + BlockPos.unpackY(endPos) + "---" + BlockPos.unpackZ(endPos) + "---" + (startLevel + l));
                    }

                    cir.setReturnValue(
                            VoxelShapes.faceShapeCovers(voxelshape, voxelshape1) ? 15 :
                                    startLevel + l
                            //Math.max(1, mutableint.getValue())
                    );
                }
            }
        }
    }
}
