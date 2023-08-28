package cn.usssehznhou.brighterf.mixin;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import net.minecraft.world.level.lighting.LeveledPriorityQueue;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tony Yu
 */
@Mixin(DynamicGraphMinFixedPoint.class)
public abstract class MixinDynamicGraphMinFixedPoint {
    @Shadow
    protected abstract boolean isSource(long p_75551_);

    @Shadow
    protected abstract int getComputedLevel(long p_75566_, long p_75567_, int p_75568_);

    @Shadow
    @Final
    protected int levelCount;

    @Shadow
    protected abstract int calculatePriority(int p_278256_, int p_278328_);

    @Final
    @Shadow
    private LeveledPriorityQueue priorityQueue;

    @Final
    @Shadow
    private Long2ByteMap computedLevels;

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "checkEdge(JJIIIZ)V", at = @At("HEAD"), cancellable = true)
    private void brighterCheckEdge(long fromPos, long toPos, int newLevel, int previousLevel, int propagationLevel, boolean isDecreasing, CallbackInfo ci) {
        if (!this.isSource(toPos)) {
            newLevel = Mth.clamp(newLevel, 0, this.levelCount - 1);
            previousLevel = Mth.clamp(previousLevel, 0, this.levelCount - 1);
            boolean flag = propagationLevel == 255;
            if (flag) {
                propagationLevel = previousLevel;
            }

            int i;
            // if (isDecreasing) {
            //     i = Math.min(propagationLevel, newLevel);
            // } else {
                i = Mth.clamp(this.getComputedLevel(toPos, fromPos, newLevel), 0, this.levelCount - 1);
            // }

            int j = this.calculatePriority(previousLevel, propagationLevel);
            if (previousLevel != i) {
                int k = this.calculatePriority(previousLevel, i);
                if (j != k && !flag) {
                    this.priorityQueue.dequeue(toPos, j, k);
                }

                this.priorityQueue.enqueue(toPos, k);
                this.computedLevels.put(toPos, (byte) i);
            } else if (!flag) {
                this.priorityQueue.dequeue(toPos, j, this.levelCount);
                this.computedLevels.remove(toPos);
            }

        }
        ci.cancel();
    }
}
