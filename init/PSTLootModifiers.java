package daripher.skilltree.init;

import com.mojang.serialization.Codec;
import daripher.skilltree.loot.modifier.AddItemModifier;
import daripher.skilltree.loot.modifier.SkillBonusesModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class PSTLootModifiers {
   public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(
      Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "skilltree"
   );

   public static void register(IEventBus eventBus) {
      REGISTRY.register(eventBus);
   }

   static {
      REGISTRY.register("add_item", AddItemModifier.CODEC);
      REGISTRY.register("skill_bonuses", SkillBonusesModifier.CODEC);
   }
}
