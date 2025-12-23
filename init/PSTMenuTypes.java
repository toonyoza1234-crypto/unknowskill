package daripher.skilltree.init;

import daripher.skilltree.client.screen.menu.WorkbenchScreen;
import daripher.skilltree.inventory.menu.WorkbenchMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(
   modid = "skilltree",
   bus = Bus.MOD
)
public class PSTMenuTypes {
   public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "skilltree");
   public static final RegistryObject<MenuType<WorkbenchMenu>> ARTISAN_WORKBENCH = REGISTRY.register(
      "artisan_workbench", () -> new MenuType(WorkbenchMenu::new, FeatureFlags.DEFAULT_FLAGS)
   );

   @SubscribeEvent
   public static void clientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> MenuScreens.register((MenuType)ARTISAN_WORKBENCH.get(), WorkbenchScreen::new));
   }
}
