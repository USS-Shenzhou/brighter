package cn.ussshenzhou.brighter.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.apache.logging.log4j.LogManager;

/**
 * @author Tony Yu
 */
//@Mixin(BlockLightSectionStorage.class)
public class FakeBlockLightSectionStorage extends LayerLightSectionStorage<FakeBlockLightSectionStorage.FakeBlockDataLayerStorageMap> {

    protected FakeBlockLightSectionStorage(LightChunkGetter p_75511_) {
        super(LightLayer.BLOCK, p_75511_, new FakeBlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
    }

    @Override
    protected int getLightValue(long p_75786_) {
        long i = SectionPos.blockToSection(p_75786_);
        DataLayer datalayer = this.getDataLayer(i, false);
        return datalayer == null ? 0 : datalayer.get(SectionPos.sectionRelative(BlockPos.getX(p_75786_)), SectionPos.sectionRelative(BlockPos.getY(p_75786_)), SectionPos.sectionRelative(BlockPos.getZ(p_75786_)));

    }

    //@Mixin(targets = "BlockLightSectionStorage.BlockLightSectionStorageMap")
    public static final class FakeBlockDataLayerStorageMap extends DataLayerStorageMap<FakeBlockDataLayerStorageMap> {
        public FakeBlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> p_75515_) {
            super(p_75515_);
        }

        @Override
        public FakeBlockDataLayerStorageMap copy() {
            return new FakeBlockDataLayerStorageMap(this.map.clone());
        }
    }
}
