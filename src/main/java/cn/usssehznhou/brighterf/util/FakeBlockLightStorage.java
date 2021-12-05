package cn.usssehznhou.brighterf.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.LightStorage;

/**
 * @author Tony Yu
 */
public class FakeBlockLightStorage extends LightStorage<FakeBlockLightStorage.Data> {
    protected FakeBlockLightStorage(ChunkProvider chunkProvider) {
        super(LightType.BLOCK, chunkProvider, new FakeBlockLightStorage.Data(new Long2ObjectOpenHashMap<ChunkNibbleArray>()));
    }

    @Override
    protected int getLight(long blockPos) {
        long l = ChunkSectionPos.fromBlockPos(blockPos);
        ChunkNibbleArray chunkNibbleArray = this.getLightSection(l, false);
        if (chunkNibbleArray == null) {
            return 0;
        }
        return chunkNibbleArray.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
    }

    public static final class Data extends ChunkToNibbleArrayMap<FakeBlockLightStorage.Data> {
        public Data(Long2ObjectOpenHashMap<ChunkNibbleArray> long2ObjectOpenHashMap) {
            super(long2ObjectOpenHashMap);
        }

        @Override
        public FakeBlockLightStorage.Data copy() {
            return new FakeBlockLightStorage.Data((Long2ObjectOpenHashMap<ChunkNibbleArray>) this.arrays.clone());
        }
    }
}
