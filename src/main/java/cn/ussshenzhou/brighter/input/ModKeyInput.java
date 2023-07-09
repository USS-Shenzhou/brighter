package cn.ussshenzhou.brighter.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModKeyInput {
    @SuppressWarnings("NoTranslation")
    public static final KeyMapping RELIGHT = new KeyMapping(
            "Re-calculate light", KeyConflictContext.UNIVERSAL, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "Brighter"
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (RELIGHT.consumeClick()) {
            var level = Minecraft.getInstance().level;
            var chunks = level.getChunkSource().storage.chunks;

            var blockLightEngine = (BlockLightEngine) Minecraft.getInstance().level.getLightEngine().blockEngine;
            var data = blockLightEngine.storage.visibleSectionData;
            data.clearCache();
            data.map.clear();


            for (int i = 0; i < chunks.length(); i++) {
                var chunk = chunks.get(i);
                if (chunk == null) {
                    continue;
                }
                blockLightEngine.propagateLightSources(chunk.getPos());

                for (int j = level.getMinSection(); j < level.getMaxSection(); j++) {
                    var secPos = SectionPos.of(chunk.getPos(), j);
                    blockLightEngine.updateSectionStatus(secPos, false);
                    blockLightEngine.storage.sectionsAffectedByLightUpdates.add(SectionPos.asLong(secPos.center()));
                }
            }

            MinecraftServer minecraftServer = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
            ServerLevel serverLevel = minecraftServer.getLevel(Level.OVERWORLD);
            var blockLightEngineServer = (BlockLightEngine) serverLevel.getLightEngine().blockEngine;
            var dataServer = blockLightEngineServer.storage.visibleSectionData;
            dataServer.clearCache();
            dataServer.map.clear();

            serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
                if (chunkHolder == null) {
                    return;
                }
                serverLevel.getLightEngine().propagateLightSources(chunkHolder.getPos());
                for (int j = serverLevel.getLightEngine().getMinLightSection(); j < serverLevel.getLightEngine().getMaxLightSection(); j++) {
                    var secPos = SectionPos.of(chunkHolder.getPos(), j);
                    serverLevel.getLightEngine().updateSectionStatus(secPos, false);
                    blockLightEngineServer.storage.sectionsAffectedByLightUpdates.add(SectionPos.asLong(secPos.center()));
                }
            });
        }
    }
}
