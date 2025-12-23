package daripher.skilltree;

import daripher.skilltree.compat.attributeslib.AttributesLibCompatibility;
import daripher.skilltree.config.ClientConfig;
import daripher.skilltree.config.ServerConfig;
import daripher.skilltree.init.PSTBlocks;
import daripher.skilltree.init.PSTCreativeTabs;
import daripher.skilltree.init.PSTDamageConditions;
import daripher.skilltree.init.PSTEnchantmentConditions;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.init.PSTItemConditions;
import daripher.skilltree.init.PSTItems;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.init.PSTLootModifiers;
import daripher.skilltree.init.PSTMenuTypes;
import daripher.skilltree.init.PSTMobEffects;
import daripher.skilltree.init.PSTPotions;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.init.PSTRecipeTypes;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.init.PSTSkillRequirements;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("skilltree")
public class SkillTreeMod {
   public static final String MOD_ID = "skilltree";
   public static final Logger LOGGER = LogManager.getLogger("skilltree");

   public SkillTreeMod() {
      IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
      PSTItems.REGISTRY.register(eventBus);
      PSTMobEffects.REGISTRY.register(eventBus);
      PSTCreativeTabs.REGISTRY.register(eventBus);
      PSTSkillBonuses.REGISTRY.register(eventBus);
      PSTLivingConditions.REGISTRY.register(eventBus);
      PSTLivingMultipliers.REGISTRY.register(eventBus);
      PSTDamageConditions.REGISTRY.register(eventBus);
      PSTItemConditions.REGISTRY.register(eventBus);
      PSTEnchantmentConditions.REGISTRY.register(eventBus);
      PSTEventListeners.REGISTRY.register(eventBus);
      PSTLootModifiers.REGISTRY.register(eventBus);
      PSTFloatFunctions.REGISTRY.register(eventBus);
      PSTPotions.REGISTRY.register(eventBus);
      PSTSkillRequirements.REGISTRY.register(eventBus);
      PSTBlocks.REGISTRY.register(eventBus);
      PSTMenuTypes.REGISTRY.register(eventBus);
      PSTRecipeSerializers.REGISTRY.register(eventBus);
      PSTItemBonuses.REGISTRY.register(eventBus);
      PSTRecipeTypes.REGISTRY.register(eventBus);
      ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.SPEC);
      ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.SPEC);
      this.addCompatibilities();
   }

   protected void addCompatibilities() {
      if (ModList.get().isLoaded("attributeslib")) {
         AttributesLibCompatibility.INSTANCE.register();
      }
   }
}
