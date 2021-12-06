package cn.ussshenzhou.brighter.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.LightDataMap;
import net.minecraft.world.lighting.SectionLightStorage;

/**
 * @author Tony Yu
 */
//@Mixin(BlockLightSectionStorage.class)
public class FakeBlockLightStorage extends SectionLightStorage<FakeBlockLightStorage.FakeBlockDataLayerStorageMap> {

    protected FakeBlockLightStorage(IChunkLightProvider p_75511_) {
        super(LightType.BLOCK, p_75511_, new FakeBlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
    }

    @Override
    protected int getLightValue(long p_75786_) {
        long i = SectionPos.blockToSection(p_75786_);
        NibbleArray nibblearray = this.getDataLayer(i, false);
        return nibblearray == null ? 0 : nibblearray.get(SectionPos.sectionRelative(BlockPos.getX(p_75786_)), SectionPos.sectionRelative(BlockPos.getY(p_75786_)), SectionPos.sectionRelative(BlockPos.getZ(p_75786_)));
    }

    //@Mixin(targets = "BlockLightSectionStorage.BlockLightSectionStorageMap")
    public static final class FakeBlockDataLayerStorageMap extends LightDataMap<FakeBlockDataLayerStorageMap> {
        public FakeBlockDataLayerStorageMap(Long2ObjectOpenHashMap<NibbleArray> p_75515_) {
            super(p_75515_);
        }

        @Override
        public FakeBlockDataLayerStorageMap copy() {
            return new FakeBlockDataLayerStorageMap(this.map.clone());
        }
    }
}
