package cn.ussshenzhou.brighter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
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
    @Shadow(remap = false)
    protected abstract boolean isSource(long p_75551_);

    @Shadow(remap = false)
    protected abstract int getComputedLevel(long p_75566_, long p_75567_, int p_75568_);

    @Shadow(remap = false)
    @Final
    private int levelCount;

    @Shadow(remap = false)
    protected abstract int getKey(int p_75549_, int p_75550_);

    @Shadow(remap = false)
    protected abstract void dequeue(long p_75559_, int p_75560_, int p_75561_, boolean p_75562_);

    @Shadow(remap = false)
    protected abstract void enqueue(long p_75555_, int p_75556_, int p_75557_);

    @Inject(method = "checkEdge(JJIIIZ)V", at = @At("HEAD"), remap = false,cancellable = true)
    private void BrighterCheckEdge(long fromPos, long toPos, int newLevel, int previousLevel, int propagationLevel, boolean isDecreasing, CallbackInfo ci) {
        /*if (Minecraft.getInstance().isSameThread()) {
            LogManager.getLogger().info(
                    BlockPos.getX(fromPos)
                            + " "
                            + BlockPos.getY(fromPos)
                            + " "
                            + BlockPos.getZ(fromPos)
                            + "  |  "
                            + BlockPos.getX(toPos)
                            + " "
                            + BlockPos.getY(toPos)
                            + " "
                            + BlockPos.getZ(toPos)
                            + "   "
                            + (15 - newLevel)
                            + "   "
                            + (15 - previousLevel)
                            + "   "
                            + (15 - propagationLevel)
                            + "   "
                            + isDecreasing);
        }*/
        if (!this.isSource(toPos)) {
            newLevel = Mth.clamp(newLevel, 0, this.levelCount - 1);
            previousLevel = Mth.clamp(previousLevel, 0, this.levelCount - 1);
            boolean flag;
            if (propagationLevel == 255) {
                flag = true;
                propagationLevel = previousLevel;
            } else {
                flag = false;
            }

            int i;
            //if (isDecreasing) {
            //    i = Math.min(propagationLevel, newLevel);
            //} else {
              i = Mth.clamp(this.getComputedLevel(toPos, fromPos, newLevel), 0, this.levelCount - 1);
            //}

            int j = this.getKey(previousLevel, propagationLevel);
            if (previousLevel != i) {
                int k = this.getKey(previousLevel, i);
                if (j != k && !flag) {
                    this.dequeue(toPos, j, k, false);
                }

                this.enqueue(toPos, i, k);
            } else if (!flag) {
                this.dequeue(toPos, j, this.levelCount, true);
            }

        }
        ci.cancel();
    }
}
