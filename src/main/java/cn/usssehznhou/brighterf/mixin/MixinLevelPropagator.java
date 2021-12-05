package cn.usssehznhou.brighterf.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.light.LevelPropagator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Tony Yu
 */
@Mixin(LevelPropagator.class)
public abstract class MixinLevelPropagator {

    @Shadow
    protected abstract boolean isMarker(long var1);

    @Shadow
    protected abstract int recalculateLevel(long var1, long var3, int var5);

    @Shadow
    @Final
    private int levelCount;

    @Shadow
    protected abstract int minLevel(int a, int b);

    @Shadow
    protected abstract void removePendingUpdate(long id, int level, int levelCount, boolean removeFully);

    @Shadow
    protected abstract void addPendingUpdate(long id, int level, int targetLevel);


    @Inject(method = "updateLevel(JJIIIZ)V", at = @At("HEAD"), cancellable = true)
    private void brighterFUpdateLevel(long sourceId, long id, int level, int currentLevel, int pendingLevel, boolean decrease, CallbackInfo ci) {
        boolean bl;
        if (this.isMarker(id)) {
            return;
        }
        level = MathHelper.clamp(level, 0, this.levelCount - 1);
        currentLevel = MathHelper.clamp(currentLevel, 0, this.levelCount - 1);
        if (pendingLevel == 255) {
            bl = true;
            pendingLevel = currentLevel;
        } else {
            bl = false;
        }
        //----
        int i = MathHelper.clamp(this.recalculateLevel(id, sourceId, level), 0, this.levelCount - 1);
        //----
        int j = this.minLevel(currentLevel, pendingLevel);
        if (currentLevel != i) {
            int k = this.minLevel(currentLevel, i);
            if (j != k && !bl) {
                this.removePendingUpdate(id, j, k, false);
            }
            this.addPendingUpdate(id, i, k);
        } else if (!bl) {
            this.removePendingUpdate(id, j, this.levelCount, true);
        }
        ci.cancel();
    }
}
