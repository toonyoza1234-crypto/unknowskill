package daripher.skilltree.init;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.skill.bonus.predicate.living.AllArmorEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.BurningEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.CrouchingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.DualWieldingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.FishingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.HasEffectEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.HasItemEquippedEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.HasItemInHandEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.UnarmedEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.UnderwaterEntityPredicate;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PSTLivingConditions {
   public static final ResourceLocation REGISTRY_ID = new ResourceLocation("skilltree", "living_conditions");
   public static final DeferredRegister<LivingEntityPredicate.Serializer> REGISTRY = DeferredRegister.create(REGISTRY_ID, "skilltree");
   public static final RegistryObject<LivingEntityPredicate.Serializer> NONE = REGISTRY.register("none", NoneLivingEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> HAS_ITEM_EQUIPPED = REGISTRY.register(
      "has_item_equipped", HasItemEquippedEntityPredicate.Serializer::new
   );
   public static final RegistryObject<LivingEntityPredicate.Serializer> HAS_EFFECT = REGISTRY.register("has_effect", HasEffectEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> BURNING = REGISTRY.register("burning", BurningEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> FISHING = REGISTRY.register("fishing", FishingEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> UNDERWATER = REGISTRY.register("underwater", UnderwaterEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> DUAL_WIELDING = REGISTRY.register(
      "dual_wielding", DualWieldingEntityPredicate.Serializer::new
   );
   public static final RegistryObject<LivingEntityPredicate.Serializer> HAS_ITEM_IN_HAND = REGISTRY.register(
      "has_item_in_hand", HasItemInHandEntityPredicate.Serializer::new
   );
   public static final RegistryObject<LivingEntityPredicate.Serializer> CROUCHING = REGISTRY.register("crouching", CrouchingEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> UNARMED = REGISTRY.register("unarmed", UnarmedEntityPredicate.Serializer::new);
   public static final RegistryObject<LivingEntityPredicate.Serializer> NUMERIC_VALUE = REGISTRY.register(
      "numeric_value", FloatFunctionEntityPredicate.Serializer::new
   );
   public static final RegistryObject<LivingEntityPredicate.Serializer> ALL_ARMOR = REGISTRY.register("all_armor", AllArmorEntityPredicate.Serializer::new);

   public static List<LivingEntityPredicate> conditionsList() {
      return PSTRegistries.LIVING_CONDITIONS.get().getValues().stream().map(LivingEntityPredicate.Serializer::createDefaultInstance).toList();
   }

   public static String getName(LivingEntityPredicate condition) {
      ResourceLocation id = PSTRegistries.LIVING_CONDITIONS.get().getKey(condition.getSerializer());
      return TooltipHelper.idToName(Objects.requireNonNull(id).getPath());
   }
}
