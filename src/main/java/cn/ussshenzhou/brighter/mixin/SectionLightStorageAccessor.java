package cn.ussshenzhou.brighter.mixin;

import net.minecraft.world.lighting.SectionLightStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Tony Yu
 */
@Mixin(SectionLightStorage.class)
public interface SectionLightStorageAccessor {
    @Invoker(remap = false)
    int callGetLight(long worldPos);
}
