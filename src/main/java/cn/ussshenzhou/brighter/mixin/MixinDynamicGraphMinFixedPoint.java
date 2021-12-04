package cn.ussshenzhou.brighter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.apache.logging.log4j.LogManager;
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

    @Inject(method = "checkEdge(JJIIIZ)V", cancellable = true, at = @At("HEAD"),remap = false)
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
    }
}
