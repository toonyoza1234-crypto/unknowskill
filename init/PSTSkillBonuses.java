package daripher.skilltree.init;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.player.AllAttributesBonus;
import daripher.skilltree.skill.bonus.player.ArrowRetrievalBonus;
import daripher.skilltree.skill.bonus.player.AttributeBonus;
import daripher.skilltree.skill.bonus.player.BlockBreakSpeedBonus;
import daripher.skilltree.skill.bonus.player.CanPoisonAnyoneBonus;
import daripher.skilltree.skill.bonus.player.CantUseItemBonus;
import daripher.skilltree.skill.bonus.player.CommandBonus;
import daripher.skilltree.skill.bonus.player.CritChanceBonus;
import daripher.skilltree.skill.bonus.player.CritDamageBonus;
import daripher.skilltree.skill.bonus.player.CurioSlotsBonus;
import daripher.skilltree.skill.bonus.player.DamageAvoidanceBonus;
import daripher.skilltree.skill.bonus.player.DamageBonus;
import daripher.skilltree.skill.bonus.player.DamageConversionBonus;
import daripher.skilltree.skill.bonus.player.DamageTakenBonus;
import daripher.skilltree.skill.bonus.player.EffectDurationBonus;
import daripher.skilltree.skill.bonus.player.FreeEnchantmentBonus;
import daripher.skilltree.skill.bonus.player.GainExperienceBonus;
import daripher.skilltree.skill.bonus.player.GainedExperienceBonus;
import daripher.skilltree.skill.bonus.player.GrantItemBonus;
import daripher.skilltree.skill.bonus.player.HealingBonus;
import daripher.skilltree.skill.bonus.player.HealthReservationBonus;
import daripher.skilltree.skill.bonus.player.IncomingHealingBonus;
import daripher.skilltree.skill.bonus.player.InflictDamageBonus;
import daripher.skilltree.skill.bonus.player.InflictEffectBonus;
import daripher.skilltree.skill.bonus.player.InflictIgniteBonus;
import daripher.skilltree.skill.bonus.player.ItemDurabilityLossAvoidanceBonus;
import daripher.skilltree.skill.bonus.player.ItemUsageSpeedBonus;
import daripher.skilltree.skill.bonus.player.ItemUseMovementSpeedBonus;
import daripher.skilltree.skill.bonus.player.JumpHeightBonus;
import daripher.skilltree.skill.bonus.player.LethalPoisonBonus;
import daripher.skilltree.skill.bonus.player.LootDuplicationBonus;
import daripher.skilltree.skill.bonus.player.MoreItemBonusesBonus;
import daripher.skilltree.skill.bonus.player.ProjectileDuplicationBonus;
import daripher.skilltree.skill.bonus.player.ProjectileSpeedBonus;
import daripher.skilltree.skill.bonus.player.RecipeUnlockBonus;
import daripher.skilltree.skill.bonus.player.RepairEfficiencyBonus;
import daripher.skilltree.skill.bonus.player.SelfSplashImmuneBonus;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PSTSkillBonuses {
   public static final ResourceLocation REGISTRY_ID = new ResourceLocation("skilltree", "skill_bonuses");
   public static final DeferredRegister<SkillBonus.Serializer> REGISTRY = DeferredRegister.create(REGISTRY_ID, "skilltree");
   public static final RegistryObject<SkillBonus.Serializer> ATTRIBUTE = REGISTRY.register("attribute", AttributeBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> COMMAND = REGISTRY.register("command", CommandBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> DAMAGE = REGISTRY.register("damage", DamageBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> CRIT_DAMAGE = REGISTRY.register("crit_damage", CritDamageBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> CRIT_CHANCE = REGISTRY.register("crit_chance", CritChanceBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> REPAIR_EFFICIENCY = REGISTRY.register("repair_efficiency", RepairEfficiencyBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> BLOCK_BREAK_SPEED = REGISTRY.register("block_break_speed", BlockBreakSpeedBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> FREE_ENCHANTMENT = REGISTRY.register("free_enchantment", FreeEnchantmentBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> JUMP_HEIGHT = REGISTRY.register("jump_height", JumpHeightBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> INCOMING_HEALING = REGISTRY.register("incoming_healing", IncomingHealingBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> LOOT_DUPLICATION = REGISTRY.register("loot_duplication", LootDuplicationBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> GAINED_EXPERIENCE = REGISTRY.register("gained_experience", GainedExperienceBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> INFLICT_IGNITE = REGISTRY.register("inflict_ignite", InflictIgniteBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> ARROW_RETRIEVAL = REGISTRY.register("arrow_retrieval", ArrowRetrievalBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> HEALTH_RESERVATION = REGISTRY.register(
      "health_reservation", HealthReservationBonus.Serializer::new
   );
   public static final RegistryObject<SkillBonus.Serializer> ALL_ATTRIBUTES = REGISTRY.register("all_attributes", AllAttributesBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> INFLICT_EFFECT = REGISTRY.register("inflict_effect", InflictEffectBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> CANT_USE_ITEM = REGISTRY.register("cant_use_item", CantUseItemBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> HEALING = REGISTRY.register("healing", HealingBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> INFLICT_DAMAGE = REGISTRY.register("inflict_damage", InflictDamageBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> CAN_POISON_ANYONE = REGISTRY.register("can_poison_anyone", CanPoisonAnyoneBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> LETHAL_POISON = REGISTRY.register("lethal_poison", LethalPoisonBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> CURIO_SLOTS = REGISTRY.register("curio_slots", CurioSlotsBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> DAMAGE_TAKEN = REGISTRY.register("damage_taken", DamageTakenBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> DAMAGE_AVOIDANCE = REGISTRY.register("damage_avoidance", DamageAvoidanceBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> DAMAGE_CONVERSION = REGISTRY.register("damage_conversion", DamageConversionBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> GRANT_ITEM = REGISTRY.register("grant_item", GrantItemBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> EFFECT_DURATION = REGISTRY.register("effect_duration", EffectDurationBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> PROJECTILE_DUPLICATION = REGISTRY.register(
      "projectile_duplication", ProjectileDuplicationBonus.Serializer::new
   );
   public static final RegistryObject<SkillBonus.Serializer> SELF_SPLASH_IMMUNE = REGISTRY.register("self_splash_immune", SelfSplashImmuneBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> PROJECTILE_SPEED = REGISTRY.register("projectile_speed", ProjectileSpeedBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> ITEM_DURABILITY_LOSS_AVOIDANCE = REGISTRY.register(
      "item_durability_loss_avoidance", ItemDurabilityLossAvoidanceBonus.Serializer::new
   );
   public static final RegistryObject<SkillBonus.Serializer> ITEM_USAGE_SPEED = REGISTRY.register("item_usage_speed", ItemUsageSpeedBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> ITEM_USE_MOVEMENT_SPEED = REGISTRY.register(
      "item_use_movement_speed", ItemUseMovementSpeedBonus.Serializer::new
   );
   public static final RegistryObject<SkillBonus.Serializer> RECIPE_UNLOCK = REGISTRY.register("recipe_unlock", RecipeUnlockBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> MORE_ITEM_BONUSES = REGISTRY.register("more_item_bonuses", MoreItemBonusesBonus.Serializer::new);
   public static final RegistryObject<SkillBonus.Serializer> GAIN_EXPERIENCE = REGISTRY.register("gain_experience", GainExperienceBonus.Serializer::new);

   public static List<SkillBonus> bonusList() {
      return PSTRegistries.SKILL_BONUSES.get().getValues().stream().map(SkillBonus.Serializer::createDefaultInstance).map(SkillBonus.class::cast).toList();
   }

   public static String getName(SkillBonus<?> bonus) {
      ResourceLocation id = PSTRegistries.SKILL_BONUSES.get().getKey(bonus.getSerializer());
      return TooltipHelper.idToName(Objects.requireNonNull(id).getPath());
   }
}
