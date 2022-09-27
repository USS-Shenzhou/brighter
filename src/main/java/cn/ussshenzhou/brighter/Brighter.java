package cn.ussshenzhou.brighter;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("brighter")
public class Brighter {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public Brighter() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        // LOGGER.info("HELLO FROM PREINIT");
        // LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        if (ModList.get().isLoaded("starlight")) {
            LogManager.getLogger().error("Brighter is noy compatible with Starlight! Brighter will not work.");
        }
    }

    private void setupClient(final FMLClientSetupEvent event){
        if (ModList.get().isLoaded("starlight")) {
            IModInfo iModInfo = ModList.get().getModContainerById("brighter")
                    .map(ModContainer::getModInfo)
                    .orElseThrow(() -> new IllegalStateException("ModInfo absent while mod itself is present"));

            ModLoader.get().addWarning(new ModLoadingWarning(
                    iModInfo,
                    ModLoadingStage.SIDED_SETUP,
                    "Brighter is noy compatible with Starlight! Brighter will not work."
            ));
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("brighter", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLDedicatedServerSetupEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
