package cn.ussshenzhou.brighter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;

/**
 * @author USS_Shenzhou
 * The value here should match an entry in the META-INF/mods.toml file
 */
@Mod("brighter")
public class Brighter {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "brighter");
    public static final RegistryObject<Block> TRANSPARENT_WHITE_CONCRETE_B = BLOCKS.register("transparent_white_concrete", () ->
            new AbstractGlassBlock(
                    BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).instrument(NoteBlockInstrument.HAT).strength(1.8F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((p_61031_, p_61032_, p_61033_, p_61034_) -> false)) {
            }
    );
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "brighter");
    public static final RegistryObject<Item> TRANSPARENT_WHITE_CONCRETE_I = ITEMS.register("transparent_white_concrete", () -> new BlockItem(
            TRANSPARENT_WHITE_CONCRETE_B.get(), new Item.Properties()
    ));

    public Brighter() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::setupClient);
        MinecraftForge.EVENT_BUS.register(this);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(this::putItemsIntoTab);
    }

    private void setup(final FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("starlight")) {
            LogManager.getLogger().error("Brighter is noy compatible with Starlight! Brighter will not work.");
        }
    }

    private void setupClient(final FMLClientSetupEvent event) {
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

    @SubscribeEvent
    public void putItemsIntoTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS) {
            event.accept(TRANSPARENT_WHITE_CONCRETE_I.get());
        }
    }
}
