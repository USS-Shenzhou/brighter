package cn.ussshenzhou.brighter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tony Yu
 */
@Mixin(BlockLightEngine.class)
public abstract class MixinBlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {

    protected MixinBlockLightEngine(LightChunkGetter p_285189_, BlockLightSectionStorage p_284964_) {
        super(p_285189_, p_284964_);
    }

    @Final
    @Shadow
    private BlockPos.MutableBlockPos mutablePos;

    @Inject(method = "propagateIncrease", at = @At("HEAD"), cancellable = true)
    private void brighterPropagateIncrease(long startPos, long noIdeaWhatThisFuckIs, int startLevel, CallbackInfo ci) {
        BlockState blockstate = null;

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (QueueEntry.shouldPropagateInDirection(noIdeaWhatThisFuckIs, direction)) {
                long nextPos = BlockPos.offset(startPos, direction);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(nextPos))) {
                    int oldNextPosLevel = this.storage.getStoredLevel(nextPos);
                    int nextLevel = startLevel - 1;
                    if (nextLevel > oldNextPosLevel) {
                        this.mutablePos.set(nextPos);
                        BlockState nextPosBlockState = this.getState(this.mutablePos);
                        int nextPosBlockOpacity = this.getOpacity(nextPosBlockState, this.mutablePos);
                        int calculatedNextPosLevel = startLevel - nextPosBlockOpacity;

                        try {
                            long prevPos = startPos + (startPos - nextPos);
                            int prevPosLevel = this.storage.getStoredLevel(prevPos);
                            if (prevPosLevel == startLevel + 1 && nextPosBlockOpacity < 15) {
                                calculatedNextPosLevel = startLevel;
                            }
                        } catch (Exception ignored) {
                        }

                        if (calculatedNextPosLevel > oldNextPosLevel) {
                            if (blockstate == null) {
                                blockstate = QueueEntry.isFromEmptyShape(noIdeaWhatThisFuckIs) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(startPos));
                            }

                            if (!this.shapeOccludes(startPos, blockstate, nextPos, nextPosBlockState, direction)) {
                                this.storage.setStoredLevel(nextPos, calculatedNextPosLevel);
                                if (calculatedNextPosLevel > 1) {
                                    this.enqueueIncrease(nextPos, QueueEntry.increaseSkipOneDirection(calculatedNextPosLevel, isEmptyShape(nextPosBlockState), direction.getOpposite()));
                                }
                            }
                        }
                    }
                }
            }
        }

        ci.cancel();
    }

    @Shadow
    protected abstract int getEmission(long p_285243_, BlockState p_284973_);

    @Inject(method = "propagateDecrease", at = @At("HEAD"), cancellable = true)
    private void brighterPropagateDecrease(long startPos, long noIdeaWhatThisFuckIs, CallbackInfo ci) {
        int i = QueueEntry.getFromLevel(noIdeaWhatThisFuckIs);

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (QueueEntry.shouldPropagateInDirection(noIdeaWhatThisFuckIs, direction)) {
                long nextPos = BlockPos.offset(startPos, direction);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(nextPos))) {
                    int oldNextPosLevel = this.storage.getStoredLevel(nextPos);
                    if (oldNextPosLevel != 0) {
                        if (oldNextPosLevel <= i - 1) {
                            BlockState blockstate = this.getState(this.mutablePos.set(nextPos));
                            int nextPosLevelIfLightSource = this.getEmission(nextPos, blockstate);
                            this.storage.setStoredLevel(nextPos, 0);

                            if (nextPosLevelIfLightSource < oldNextPosLevel) {
                                //this.enqueueDecrease(nextPos, QueueEntry.decreaseSkipOneDirection(oldNextPosLevel, direction.getOpposite()));
                                this.enqueueDecrease(nextPos, QueueEntry.decreaseSkipOneDirection(oldNextPosLevel + 1, direction.getOpposite()));
                            }

                            if (nextPosLevelIfLightSource > 0) {
                                this.enqueueIncrease(nextPos, QueueEntry.increaseLightFromEmission(nextPosLevelIfLightSource, isEmptyShape(blockstate)));
                            }

                        } else {
                            this.enqueueIncrease(nextPos, QueueEntry.increaseOnlyOneDirection(oldNextPosLevel, false, direction.getOpposite()));
                        }
                    }
                }
            }
        }


        ci.cancel();
    }


}
