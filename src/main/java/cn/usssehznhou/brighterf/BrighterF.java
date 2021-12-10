package cn.usssehznhou.brighterf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

/**
 * @author Tony Yu
 */
public class BrighterF implements ModInitializer {
    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("starlight")){
            LogManager.getLogger().error("BrighterF is noy compatible with Starlight! BrighterF will not work.");
        }
    }
}
