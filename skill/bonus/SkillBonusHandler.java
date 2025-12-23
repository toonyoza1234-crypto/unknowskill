package daripher.skilltree.skill.bonus;

import com.mojang.datafixers.util.Either;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.effect.SkillBonusEffect;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.mixin.AbstractArrowAccessor;
import daripher.skilltree.mixin.MobEffectInstanceAccessor;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.bonus.event.AttackEventListener;
import daripher.skilltree.skill.bonus.event.BlockEventListener;
import daripher.skilltree.skill.bonus.event.CritEventListener;
import daripher.skilltree.skill.bonus.event.DamageTakenEventListener;
import daripher.skilltree.skill.bonus.event.EvasionEventListener;
import daripher.skilltree.skill.bonus.event.ItemUsedEventListener;
import daripher.skilltree.skill.bonus.event.KillEventListener;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.ItemBonusHandler;
import daripher.skilltree.skill.bonus.item.SkillBonusItemBonus;
import daripher.skilltree.skill.bonus.player.ArrowRetrievalBonus;
import daripher.skilltree.skill.bonus.player.BlockBreakSpeedBonus;
import daripher.skilltree.skill.bonus.player.CanPoisonAnyoneBonus;
import daripher.skilltree.skill.bonus.player.CantUseItemBonus;
import daripher.skilltree.skill.bonus.player.CritChanceBonus;
import daripher.skilltree.skill.bonus.player.CritDamageBonus;
import daripher.skilltree.skill.bonus.player.DamageAvoidanceBonus;
import daripher.skilltree.skill.bonus.player.DamageBonus;
import daripher.skilltree.skill.bonus.player.DamageConversionBonus;
import daripher.skilltree.skill.bonus.player.DamageTakenBonus;
import daripher.skilltree.skill.bonus.player.EffectDurationBonus;
import daripher.skilltree.skill.bonus.player.FreeEnchantmentBonus;
import daripher.skilltree.skill.bonus.player.GainedExperienceBonus;
import daripher.skilltree.skill.bonus.player.HealthReservationBonus;
import daripher.skilltree.skill.bonus.player.IncomingHealingBonus;
import daripher.skilltree.skill.bonus.player.ItemUsageSpeedBonus;
import daripher.skilltree.skill.bonus.player.ItemUseMovementSpeedBonus;
import daripher.skilltree.skill.bonus.player.JumpHeightBonus;
import daripher.skilltree.skill.bonus.player.ProjectileDuplicationBonus;
import daripher.skilltree.skill.bonus.player.ProjectileSpeedBonus;
import daripher.skilltree.skill.bonus.player.RepairEfficiencyBonus;
import daripher.skilltree.skill.bonus.predicate.damage.DamageCondition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Tick;
import net.minecraftforge.event.entity.living.MobEffectEvent.Added;
import net.minecraftforge.event.entity.living.MobEffectEvent.Applicable;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

@EventBusSubscriber(
   modid = "skilltree"
)
public class SkillBonusHandler {
   @SubscribeEvent
   public static void applyBreakSpeedMultiplier(BreakSpeed event) {
      Player player = event.getEntity();
      float multiplier = 1.0F;
      multiplier += getSkillBonuses(player, BlockBreakSpeedBonus.class).stream().map(b -> b.getMultiplier(player)).reduce(Float::sum).orElse(0.0F);
      event.setNewSpeed(event.getNewSpeed() * multiplier);
   }

   @SubscribeEvent
   public static void applyFallReductionMultiplier(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player) {
         float multiplier = getJumpHeightMultiplier(player);
         if (!(multiplier <= 1.0F)) {
            event.setDistance(event.getDistance() / multiplier);
         }
      }
   }

   @SubscribeEvent
   public static void applyRepairEfficiency(AnvilUpdateEvent event) {
      Player player = event.getPlayer();
      ItemStack stack = event.getLeft();
      float efficiency = getRepairEfficiency(player, stack);
      if (efficiency != 1.0F) {
         if (stack.isDamageableItem() && stack.isDamaged()) {
            ItemStack material = event.getRight();
            if (stack.getItem().isValidRepairItem(stack, material)) {
               ItemStack result = stack.copy();
               int durabilityPerMaterial = (int)((float)(result.getMaxDamage() * 12) * (1.0F + efficiency) / 100.0F);
               int durabilityRestored = durabilityPerMaterial;
               int materialsUsed = 0;

               int cost;
               for (cost = 0; durabilityRestored > 0 && materialsUsed < material.getCount(); materialsUsed++) {
                  result.setDamageValue(result.getDamageValue() - durabilityRestored);
                  cost++;
                  durabilityRestored = Math.min(result.getDamageValue(), durabilityPerMaterial);
               }

               if (event.getName() != null && !StringUtils.isBlank(event.getName())) {
                  if (!event.getName().equals(stack.getHoverName().getString())) {
                     cost++;
                     result.setHoverName(Component.literal(event.getName()));
                  }
               } else if (stack.hasCustomHoverName()) {
                  cost++;
                  result.resetHoverName();
               }

               event.setMaterialCost(materialsUsed);
               event.setCost(cost);
               event.setOutput(result);
            }
         }
      }
   }

   private static float getRepairEfficiency(Player player, ItemStack stack) {
      float efficiency = 1.0F;

      for (RepairEfficiencyBonus bonus : getSkillBonuses(player, RepairEfficiencyBonus.class)) {
         if (bonus.getItemCondition().test(stack)) {
            efficiency += bonus.getMultiplier();
         }
      }

      return efficiency;
   }

   @SubscribeEvent
   public static void tickSkillBonuses(PlayerTickEvent event) {
      if (!event.player.isDeadOrDying()) {
         if (event.player instanceof ServerPlayer player) {
            if (event.phase != Phase.END) {
               getSkillBonuses(player, TickingSkillBonus.class).forEach(bonus -> bonus.tick(player));
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void applyFlatDamageBonus(LivingHurtEvent event) {
      Player attacker = getPlayerAttacker(event);
      if (attacker != null) {
         LivingEntity target = event.getEntity();
         setLastTarget(attacker, target);
         float bonus = getDamageBonus(attacker, event.getSource(), target, Operation.ADDITION);
         event.setAmount(event.getAmount() + bonus);
      }
   }

   private static void setLastTarget(Player attacker, LivingEntity target) {
      CompoundTag dataTag = attacker.getPersistentData();
      dataTag.putInt("LastAttackTarget", target.getId());
   }

   @SubscribeEvent
   public static void applyBaseDamageMultipliers(LivingHurtEvent event) {
      Player attacker = getPlayerAttacker(event);
      if (attacker != null) {
         float bonus = getDamageBonus(attacker, event.getSource(), event.getEntity(), Operation.MULTIPLY_BASE);
         event.setAmount(event.getAmount() * (1.0F + bonus));
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void applyTotalDamageMultipliers(LivingHurtEvent event) {
      Player attacker = getPlayerAttacker(event);
      if (attacker != null) {
         float bonus = getDamageBonus(attacker, event.getSource(), event.getEntity(), Operation.MULTIPLY_TOTAL);
         event.setAmount(event.getAmount() * (1.0F + bonus));
      }
   }

   @Nullable
   private static Player getPlayerAttacker(LivingHurtEvent event) {
      Player attacker = null;
      if (event.getSource().getEntity() instanceof Player player) {
         attacker = player;
      } else if (event.getSource().getDirectEntity() instanceof Player player) {
         attacker = player;
      }

      return attacker;
   }

   private static float getDamageBonus(Player player, DamageSource damageSource, LivingEntity target, Operation operation) {
      float amount = 0.0F;

      for (DamageBonus bonus : getSkillBonuses(player, DamageBonus.class)) {
         amount += bonus.getDamageBonus(operation, damageSource, player, target);
      }

      return amount;
   }

   @SubscribeEvent
   public static void applyCritBonuses(CriticalHitEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (event.getTarget() instanceof LivingEntity target) {
            DamageSource var7 = player.level().damageSources().playerAttack(player);
            float critChance = getCritChance(player, var7, event.getEntity());
            if (!(player.getRandom().nextFloat() >= critChance)) {
               float critMultiplier = event.getDamageModifier();
               critMultiplier += getCritDamageMultiplier(player, var7, target);
               if (!event.isVanillaCritical()) {
                  critMultiplier += 0.5F;
                  event.setResult(Result.ALLOW);
               }

               event.setDamageModifier(critMultiplier);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void applyCritBonuses(LivingHurtEvent event) {
      if (!(event.getSource().getDirectEntity() instanceof Player)) {
         if (event.getSource().getEntity() instanceof ServerPlayer player) {
            float var4 = getCritChance(player, event.getSource(), event.getEntity());
            if (!(player.getRandom().nextFloat() >= var4)) {
               float critMultiplier = 1.5F;
               critMultiplier += getCritDamageMultiplier(player, event.getSource(), event.getEntity());
               event.setAmount(event.getAmount() * critMultiplier);
            }
         }
      }
   }

   private static float getCritDamageMultiplier(ServerPlayer player, DamageSource source, LivingEntity target) {
      float multiplier = 0.0F;

      for (CritDamageBonus bonus : getSkillBonuses(player, CritDamageBonus.class)) {
         multiplier += bonus.getDamageBonus(source, player, target);
      }

      return multiplier;
   }

   private static float getCritChance(ServerPlayer player, DamageSource source, LivingEntity target) {
      float critChance = 0.0F;

      for (CritChanceBonus bonus : getSkillBonuses(player, CritChanceBonus.class)) {
         critChance += bonus.getChanceBonus(source, player, target);
      }

      return critChance;
   }

   @SubscribeEvent
   public static void applyIncomingHealingBonus(LivingHealEvent event) {
      if (event.getEntity() instanceof Player player) {
         float var5 = 1.0F;

         for (IncomingHealingBonus bonus : getSkillBonuses(player, IncomingHealingBonus.class)) {
            var5 += bonus.getHealingMultiplier(player);
         }

         event.setAmount(event.getAmount() * var5);
      }
   }

   @SubscribeEvent
   public static void applyExperienceFromMobsBonus(LivingExperienceDropEvent event) {
      Player player = event.getAttackingPlayer();
      if (player != null) {
         float multiplier = 1.0F;
         multiplier += getExperienceMultiplier(player, GainedExperienceBonus.ExperienceSource.MOBS);
         event.setDroppedExperience((int)((float)event.getDroppedExperience() * multiplier));
      }
   }

   @SubscribeEvent
   public static void applyExperienceFromOreBonus(BreakEvent event) {
      if (event.getState().is(Blocks.ORES)) {
         float multiplier = 1.0F;
         multiplier += getExperienceMultiplier(event.getPlayer(), GainedExperienceBonus.ExperienceSource.ORE);
         event.setExpToDrop((int)((float)event.getExpToDrop() * multiplier));
      }
   }

   @SubscribeEvent
   public static void applyFishingExperienceBonus(ItemFishedEvent event) {
      Player player = event.getEntity();
      float multiplier = getExperienceMultiplier(player, GainedExperienceBonus.ExperienceSource.FISHING);
      if (multiplier != 0.0F) {
         int exp = (int)((float)(player.getRandom().nextInt(6) + 1) * multiplier);
         if (exp != 0) {
            ExperienceOrb expOrb = new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp);
            player.level().addFreshEntity(expOrb);
         }
      }
   }

   private static float getExperienceMultiplier(Player player, GainedExperienceBonus.ExperienceSource source) {
      float multiplier = 0.0F;

      for (GainedExperienceBonus bonus : getSkillBonuses(player, GainedExperienceBonus.class)) {
         if (bonus.getSource() == source) {
            multiplier += bonus.getMultiplier();
         }
      }

      return multiplier;
   }

   @SubscribeEvent
   public static void applyEventListenerEffect(LivingHurtEvent event) {
      Entity sourceEntity = event.getSource().getEntity();
      if (sourceEntity instanceof Player player) {
         for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonus.getEventListener() instanceof AttackEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
               listener.onEvent(player, event.getEntity(), event.getSource(), (EventListenerBonus<?>)copy);
            }
         }
      }

      if (event.getEntity() instanceof Player player) {
         for (EventListenerBonus<?> bonusx : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonusx.getEventListener() instanceof DamageTakenEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonusx.copy();
               LivingEntity attacker = sourceEntity instanceof LivingEntity ? (LivingEntity)sourceEntity : null;
               listener.onEvent(player, attacker, event.getSource(), (EventListenerBonus<?>)copy);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyEventListenerEffect(CriticalHitEvent event) {
      if (event.getTarget() instanceof LivingEntity target) {
         Player player = event.getEntity();

         for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonus.getEventListener() instanceof CritEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
               listener.onEvent(player, target, (EventListenerBonus<?>)copy);
            }
         }
      }
   }

   @SubscribeEvent
   public static void applyEventListenerEffect(ShieldBlockEvent event) {
      if (event.getEntity() instanceof Player player) {
         for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonus.getEventListener() instanceof BlockEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
               DamageSource source = event.getDamageSource();
               Entity sourceEntity = source.getEntity();
               LivingEntity attacker = sourceEntity instanceof LivingEntity ? (LivingEntity)sourceEntity : null;
               listener.onEvent(player, attacker, source, (EventListenerBonus<?>)copy);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyEventListenerEffect(Finish event) {
      if (event.getEntity() instanceof Player player) {
         for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonus.getEventListener() instanceof ItemUsedEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
               listener.onEvent(player, event.getItem(), (EventListenerBonus<?>)copy);
            }
         }
      }
   }

   @SubscribeEvent
   public static void applyEventListenerEffect(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof Player player) {
         for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (bonus.getEventListener() instanceof KillEventListener listener) {
               SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
               DamageSource source = event.getSource();
               listener.onEvent(player, player, source, (EventListenerBonus<?>)copy);
            }
         }
      }
   }

   @SubscribeEvent
   public static void applyArrowRetrievalBonus(LivingHurtEvent event) {
      if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
         if (event.getSource().getEntity() instanceof Player player) {
            AbstractArrowAccessor arrowAccessor = (AbstractArrowAccessor)arrow;
            ItemStack arrowStack = arrowAccessor.invokeGetPickupItem();
            if (arrowStack != null) {
               float retrievalChance = 0.0F;

               for (ArrowRetrievalBonus bonus : getSkillBonuses(player, ArrowRetrievalBonus.class)) {
                  retrievalChance += bonus.getChance();
               }

               if (!(player.getRandom().nextFloat() >= retrievalChance)) {
                  LivingEntity target = event.getEntity();
                  CompoundTag targetData = target.getPersistentData();
                  ListTag stuckArrowsTag = targetData.getList("StuckArrows", new CompoundTag().getId());
                  stuckArrowsTag.add(arrowStack.save(new CompoundTag()));
                  targetData.put("StuckArrows", stuckArrowsTag);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void retrieveArrows(LivingDeathEvent event) {
      LivingEntity entity = event.getEntity();
      ListTag arrowsTag = entity.getPersistentData().getList("StuckArrows", new CompoundTag().getId());
      if (!arrowsTag.isEmpty()) {
         for (Tag tag : arrowsTag) {
            ItemStack arrowStack = ItemStack.of((CompoundTag)tag);
            entity.spawnAtLocation(arrowStack);
         }
      }
   }

   @SubscribeEvent
   public static void applyHealthReservationEffect(PlayerTickEvent event) {
      if (event.phase != Phase.END && event.side != LogicalSide.CLIENT) {
         float reservation = getHealthReservation(event.player);
         if (reservation != 0.0F) {
            if (event.player.getHealth() / event.player.getMaxHealth() > 1.0F - reservation) {
               event.player.setHealth(event.player.getMaxHealth() * (1.0F - reservation));
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyHealthReservationEffect(LivingHealEvent event) {
      if (event.getEntity() instanceof Player player) {
         float reservation = getHealthReservation(player);
         if (reservation != 0.0F) {
            float healthAfterHealing = player.getHealth() + event.getAmount();
            if (healthAfterHealing / player.getMaxHealth() > 1.0F - reservation) {
               event.setCanceled(true);
            }
         }
      }
   }

   private static float getHealthReservation(Player player) {
      float reservation = 0.0F;

      for (HealthReservationBonus bonus : getSkillBonuses(player, HealthReservationBonus.class)) {
         reservation += bonus.getAmount(player);
      }

      return reservation;
   }

   @SubscribeEvent
   public static void applyCantUseItemBonus(AttackEntityEvent event) {
      for (CantUseItemBonus bonus : getSkillBonuses(event.getEntity(), CantUseItemBonus.class)) {
         if (bonus.getItemCondition().test(event.getEntity().getMainHandItem())) {
            event.setCanceled(true);
            return;
         }
      }
   }

   @SubscribeEvent
   public static void applyCantUseItemBonus(PlayerInteractEvent event) {
      for (CantUseItemBonus bonus : getSkillBonuses(event.getEntity(), CantUseItemBonus.class)) {
         if (bonus.getItemCondition().test(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            if (event.isCancelable()) {
               event.setCanceled(true);
            }

            return;
         }
      }
   }

   @SubscribeEvent
   public static void applyCantUseItemBonus(CurioEquipEvent event) {
      if (event.getEntity() instanceof Player player) {
         for (CantUseItemBonus bonus : getSkillBonuses(player, CantUseItemBonus.class)) {
            if (bonus.getItemCondition().test(event.getStack())) {
               event.setResult(Result.DENY);
               return;
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void addCantUseItemTooltip(GatherComponents event) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         for (CantUseItemBonus bonus : getSkillBonuses(player, CantUseItemBonus.class)) {
            if (bonus.getItemCondition().test(event.getItemStack())) {
               Component tooltip = Component.translatable("item.cant_use.info").withStyle(ChatFormatting.RED);
               event.getTooltipElements().add(Either.left(tooltip));
               return;
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST,
      receiveCanceled = true
   )
   public static void inflictPoisonForcefully(Applicable event) {
      if (event.getEffectInstance().getEffect() == MobEffects.POISON) {
         if (event.getEntity().getKillCredit() instanceof Player player) {
            if (!getSkillBonuses(player, CanPoisonAnyoneBonus.class).isEmpty()) {
               event.setResult(Result.ALLOW);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyDamageTakenBonuses(LivingHurtEvent event) {
      if (event.getEntity() instanceof Player player) {
         DamageSource damageSource = event.getSource();
         if (damageSource.getEntity() instanceof LivingEntity attacker) {
            float var9 = event.getAmount();
            float addition = getDamageTaken(player, attacker, damageSource, Operation.ADDITION);
            var9 += addition;
            float multiplier = getDamageTaken(player, attacker, damageSource, Operation.MULTIPLY_BASE);
            var9 *= 1.0F + multiplier;
            float multiplierTotal = getDamageTaken(player, attacker, damageSource, Operation.MULTIPLY_TOTAL);
            var9 *= 1.0F + multiplierTotal;
            event.setAmount(var9);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyDamageAvoidanceBonuses(LivingAttackEvent event) {
      if (event.getEntity() instanceof Player player) {
         DamageSource damageSource = event.getSource();
         LivingEntity attacker = getDamageSourceEntity(damageSource);
         float avoidance = getSkillBonuses(player, DamageAvoidanceBonus.class)
            .stream()
            .map(b -> b.getAvoidanceChance(damageSource, player, attacker))
            .reduce(Float::sum)
            .orElse(0.0F);
         if (player.getRandom().nextFloat() < avoidance) {
            event.setCanceled(true);
            applyEventListenerEffect(player, attacker);
         }
      }
   }

   @Nullable
   private static LivingEntity getDamageSourceEntity(DamageSource damageSource) {
      if (damageSource.getEntity() instanceof LivingEntity) {
         return (LivingEntity)damageSource.getEntity();
      } else {
         if (damageSource.getEntity() instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity) {
            return (LivingEntity)projectile.getOwner();
         }

         return null;
      }
   }

   private static void applyEventListenerEffect(Player player, LivingEntity attacker) {
      for (EventListenerBonus<?> bonus : getMergedSkillBonuses(player, EventListenerBonus.class)) {
         if (bonus.getEventListener() instanceof EvasionEventListener listener) {
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, attacker, (EventListenerBonus<?>)copy);
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void applyDamageConversionBonuses(LivingHurtEvent event) {
      DamageSource originalDamageSource = event.getSource();
      if (originalDamageSource.getEntity() instanceof Player player) {
         if (!getDamageConversionBonuses(player, originalDamageSource).findAny().isEmpty()) {
            LivingEntity target = event.getEntity();
            float originalDamageAmount = event.getAmount();
            getDamageConversionMap(event, player, originalDamageSource).forEach((damageCondition, amount) -> {
               DamageSource damageSource = damageCondition.createDamageSource(player);
               forcefullyInflictDamage(damageSource, amount * originalDamageAmount, target);
            });
            float convertedDamage = getConvertedDamagePercentage(player, originalDamageSource, target);
            event.setAmount(originalDamageAmount * (1.0F - convertedDamage));
         }
      }
   }

   @SubscribeEvent
   public static void applyEffectDurationBonuses(Added event) {
      Player source = null;
      if (event.getEffectSource() instanceof Player player) {
         source = player;
      }

      if (event.getEffectSource() instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
         source = player;
      }

      Player playerSource = source;
      float durationMultiplier = 1.0F;
      if (source != null) {
         durationMultiplier += getSkillBonuses(playerSource, EffectDurationBonus.class)
            .stream()
            .filter(b -> b.getTarget() == SkillBonus.Target.ENEMY)
            .map(b -> b.getDuration(playerSource, event.getEntity()))
            .reduce(Float::sum)
            .orElse(0.0F);
      }

      if (event.getEntity() instanceof Player player) {
         durationMultiplier += getSkillBonuses(player, EffectDurationBonus.class)
            .stream()
            .filter(b -> b.getTarget() == SkillBonus.Target.PLAYER)
            .map(b -> b.getDuration(playerSource, player))
            .reduce(Float::sum)
            .orElse(0.0F);
      }

      if (durationMultiplier != 1.0F) {
         MobEffectInstance effectInstance = event.getEffectInstance();
         int newDuration = (int)((float)effectInstance.getDuration() * durationMultiplier);
         ((MobEffectInstanceAccessor)effectInstance).setDuration(newDuration);
      }
   }

   @SubscribeEvent
   public static void applyProjectileDuplicationBonuses(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Projectile projectile) {
         if (event.getLevel() instanceof ServerLevel level) {
            if (projectile.getOwner() instanceof Player player) {
               if (!event.loadedFromDisk()) {
                  CompoundTag projectileTag = projectile.getPersistentData();
                  if (!projectileTag.getBoolean("duplicated")) {
                     float duplicationChance = getSkillBonuses(player, ProjectileDuplicationBonus.class)
                        .stream()
                        .map(b -> b.getChance(player))
                        .reduce(Float::sum)
                        .orElse(0.0F);
                     if (duplicationChance != 0.0F) {
                        projectileTag.putBoolean("duplicated", true);
                        int projectileAmount = (int)duplicationChance;
                        duplicationChance -= (float)projectileAmount;
                        RandomSource random = player.getRandom();
                        if (random.nextFloat() < duplicationChance) {
                           projectileAmount++;
                        }

                        fireDuplicateProjectiles(projectile, level, player, projectileAmount);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void forcefullyInflictDuplicatedProjectileDamage(LivingAttackEvent event) {
      DamageSource damageSource = event.getSource();
      if (damageSource.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof Player) {
         CompoundTag projectileTag = projectile.getPersistentData();
         if (!projectileTag.getBoolean("duplicated")) {
            return;
         }

         LivingEntity target = event.getEntity();
         target.invulnerableTime = 0;
         target.setInvulnerable(false);
         return;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void applyProjectileSpeedBonus(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Projectile projectile) {
         if (event.getLevel() instanceof ServerLevel) {
            if (projectile.getOwner() instanceof Player player) {
               CompoundTag projectileTag = projectile.getPersistentData();
               if (!projectileTag.getBoolean("speed_applied")) {
                  float speedBonus = 1.0F;
                  speedBonus += getSkillBonuses(player, ProjectileSpeedBonus.class).stream().map(b -> b.getMultiplier(player)).reduce(Float::sum).orElse(0.0F);
                  if (speedBonus != 1.0F) {
                     projectileTag.putBoolean("speed_applied", true);
                     Vec3 speedBonusVec = new Vec3((double)speedBonus, (double)speedBonus, (double)speedBonus);
                     Vec3 projectileMovement = projectile.getDeltaMovement();
                     projectile.setDeltaMovement(projectileMovement.multiply(speedBonusVec));
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void applyItemUsageSpeed(Tick event) {
      if (event.getEntity() instanceof Player player) {
         float additionalSpeed = getSkillBonuses(player, ItemUsageSpeedBonus.class)
            .stream()
            .map(bonus -> bonus.getMultiplier(player, event.getItem()))
            .reduce(Float::sum)
            .orElse(0.0F);
         if (additionalSpeed != 0.0F) {
            int useTimeOffset = -1;
            if (additionalSpeed < 0.0F) {
               useTimeOffset = 1;
               additionalSpeed *= -1.0F;
            }

            while (additionalSpeed > 1.0F) {
               event.setDuration(event.getDuration() + useTimeOffset);
               additionalSpeed--;
            }

            if (additionalSpeed > 0.5F) {
               if (event.getEntity().tickCount % 2 == 0) {
                  event.setDuration(event.getDuration() + useTimeOffset);
               }

               additionalSpeed -= 0.5F;
            }

            int mod = (int)Math.floor((double)(1.0F / Math.min(1.0F, additionalSpeed)));
            if (event.getEntity().tickCount % mod == 0) {
               event.setDuration(event.getDuration() + useTimeOffset);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public static void applyItemUseMovementSpeedBonus(MovementInputUpdateEvent event) {
      Player player = event.getEntity();
      Input input = event.getInput();
      if (player.isUsingItem() && !player.isPassenger()) {
         float defaultPenalty = 0.8F;
         float penaltyReduction = getSkillBonuses(player, ItemUseMovementSpeedBonus.class)
            .stream()
            .map(bonus -> bonus.getMultiplier(player, player.getUseItem()))
            .reduce(Float::sum)
            .orElse(0.0F);
         defaultPenalty += defaultPenalty * penaltyReduction;
         float reductionFactor = 1.0F - defaultPenalty;
         input.leftImpulse *= reductionFactor;
         input.forwardImpulse *= reductionFactor;
         input.leftImpulse *= 5.0F;
         input.forwardImpulse *= 5.0F;
      }
   }

   private static void fireDuplicateProjectiles(Projectile projectile, ServerLevel level, Player player, int projectileAmount) {
      float spreadAngle = 5.0F;

      for (int i = 0; i < projectileAmount; i++) {
         int side = i % 2 == 0 ? 1 : -1;
         int projectileNumber = i / 2 + 1;
         float angleOffset = (float)(projectileNumber * side) * spreadAngle;
         duplicateProjectileWithOffset(projectile, player, level, angleOffset);
      }
   }

   private static void duplicateProjectileWithOffset(Projectile original, Player player, ServerLevel level, float angleOffset) {
      EntityType<?> projectileType = original.getType();
      Projectile duplicate = (Projectile)projectileType.create(level);
      if (duplicate != null) {
         Vec3 movementVector = original.getDeltaMovement();
         Vec3 rotatedDirection = rotateVector(movementVector, (double)angleOffset);
         Vec3 originalPos = original.position();
         Vec3 duplicatePos = originalPos.add(rotatedDirection.normalize());
         duplicate.setPos(duplicatePos.x, duplicatePos.y, duplicatePos.z);
         duplicate.setDeltaMovement(rotatedDirection);
         duplicate.setOwner(player);
         CompoundTag projectileTag = duplicate.getPersistentData();
         projectileTag.putBoolean("duplicated", true);
         if (duplicate instanceof AbstractArrow duplicateArrow) {
            AbstractArrow originalArrow = (AbstractArrow)original;
            duplicateArrow.pickup = Pickup.DISALLOWED;
            float velocity = (float)movementVector.length();
            duplicateArrow.setEnchantmentEffectsFromEntity(player, velocity);
            duplicateArrow.setBaseDamage(originalArrow.getBaseDamage());
         } else if (duplicate instanceof ThrownPotion potion) {
            ThrownPotion originalPotion = (ThrownPotion)original;
            potion.setItem(originalPotion.getItem());
         }

         level.addFreshEntity(duplicate);
      }
   }

   private static Vec3 rotateVector(Vec3 vector, double angleDegrees) {
      double angleRadians = Math.toRadians(angleDegrees);
      double cos = Math.cos(angleRadians);
      double sin = Math.sin(angleRadians);
      double x = vector.x * cos - vector.z * sin;
      double z = vector.x * sin + vector.z * cos;
      return new Vec3(x, vector.y, z);
   }

   private static float getConvertedDamagePercentage(Player player, DamageSource originalDamageSource, LivingEntity target) {
      return getDamageConversionBonuses(player, originalDamageSource)
         .map(b -> b.getConversionRate(originalDamageSource, player, target))
         .reduce(Float::sum)
         .orElse(0.0F);
   }

   @NotNull
   private static Map<DamageCondition, Float> getDamageConversionMap(LivingHurtEvent event, Player player, DamageSource originalDamageSource) {
      Map<DamageCondition, Float> conversions = new HashMap<>();
      getDamageConversionBonuses(player, originalDamageSource)
         .forEach(
            bonus -> {
               DamageCondition resultDamageSource = bonus.getResultDamageCondition();
               conversions.put(
                  resultDamageSource,
                  conversions.getOrDefault(resultDamageSource, 0.0F) + bonus.getConversionRate(originalDamageSource, player, event.getEntity())
               );
            }
         );
      return conversions;
   }

   @NotNull
   private static Stream<DamageConversionBonus> getDamageConversionBonuses(Player player, DamageSource damageSource) {
      return getSkillBonuses(player, DamageConversionBonus.class)
         .stream()
         .filter(b -> b.getOriginalDamageCondition().met(damageSource))
         .filter(b -> !b.getResultDamageCondition().met(damageSource));
   }

   public static void forcefullyInflictDamage(DamageSource source, float amount, Entity entity) {
      MinecraftServer server = entity.getServer();
      if (server != null) {
         server.tell(new TickTask(server.getTickCount() + 1, () -> {
            entity.invulnerableTime = 0;
            entity.hurt(source, amount);
         }));
      }
   }

   private static float getDamageTaken(Player player, LivingEntity attacker, DamageSource damageSource, Operation operation) {
      List<DamageTakenBonus> damageTakenBonuses = getSkillBonuses(player, DamageTakenBonus.class);
      return damageTakenBonuses.stream().map(b -> b.getDamageBonus(operation, damageSource, player, attacker)).reduce(Float::sum).orElse(0.0F);
   }

   public static float getJumpHeightMultiplier(Player player) {
      float multiplier = 1.0F;

      for (JumpHeightBonus bonus : getSkillBonuses(player, JumpHeightBonus.class)) {
         multiplier += bonus.getJumpHeightMultiplier(player);
      }

      return multiplier;
   }

   public static float getFreeEnchantmentChance(@Nonnull Player player, ItemStack itemStack) {
      return getSkillBonuses(player, FreeEnchantmentBonus.class).stream().map(bonus -> bonus.getChance(player, itemStack)).reduce(Float::sum).orElse(0.0F);
   }

   public static <T> List<T> getSkillBonuses(@Nonnull Player player, Class<T> type) {
      if (!PlayerSkillsProvider.hasSkills(player)) {
         return List.of();
      } else {
         List<T> bonuses = new ArrayList<>();
         bonuses.addAll(getPlayerBonuses(player, type));
         bonuses.addAll(getEffectBonuses(player, type));
         bonuses.addAll(getEquipmentBonuses(player, type));
         return bonuses;
      }
   }

   public static <T> List<T> getMergedSkillBonuses(@Nonnull Player player, Class<T> type) {
      return mergeSkillBonuses(getSkillBonuses(player, type));
   }

   @NotNull
   private static <T> List<T> mergeSkillBonuses(List<T> bonuses) {
      List<T> mergedBonuses = new ArrayList<>();

      for (T bonus : bonuses) {
         SkillBonus skillBonus = (SkillBonus)bonus;
         Optional<SkillBonus> mergeTarget = mergedBonuses.stream().map(SkillBonus.class::cast).filter(skillBonus::canMerge).findAny();
         if (mergeTarget.isPresent()) {
            mergedBonuses.remove(mergeTarget.get());
            mergedBonuses.add((T)mergeTarget.get().copy().merge(skillBonus));
         } else {
            mergedBonuses.add((T)skillBonus);
         }
      }

      return mergedBonuses;
   }

   private static <T> List<T> getPlayerBonuses(Player player, Class<T> type) {
      List<T> list = new ArrayList<>();

      for (PassiveSkill skill : PlayerSkillsProvider.get(player).getPlayerSkills()) {
         for (SkillBonus<?> skillBonus : skill.getBonuses()) {
            if (type.isInstance(skillBonus)) {
               list.add(type.cast(skillBonus));
            }
         }
      }

      return list;
   }

   private static <T> List<T> getEffectBonuses(Player player, Class<T> type) {
      List<T> bonuses = new ArrayList<>();

      for (MobEffectInstance e : player.getActiveEffects()) {
         MobEffect bonus = e.getEffect();
         if (bonus instanceof SkillBonusEffect) {
            SkillBonusEffect skillEffect = (SkillBonusEffect)bonus;
            SkillBonus<?> bonusx = skillEffect.getBonus().copy();
            if (type.isInstance(bonusx)) {
               SkillBonus<?> var8 = bonusx.copy().multiply((double)e.getAmplifier());
               bonuses.add(type.cast(var8));
            }
         }
      }

      return bonuses;
   }

   private static <T> List<T> getEquipmentBonuses(Player player, Class<T> type) {
      return PlayerHelper.getAllEquipment(player).map(s -> getItemBonuses(s, type)).flatMap(Collection::stream).toList();
   }

   private static <T> List<T> getItemBonuses(ItemStack stack, Class<T> type) {
      List<ItemBonus<?>> itemBonuses = new ArrayList<>(ItemBonusHandler.getItemBonuses(stack));
      List<T> bonuses = new ArrayList<>();

      for (ItemBonus<?> itemBonus : itemBonuses) {
         if (itemBonus instanceof SkillBonusItemBonus) {
            SkillBonusItemBonus bonus = (SkillBonusItemBonus)itemBonus;
            SkillBonus<?> skillBonus = bonus.skillBonus();
            if (type.isInstance(skillBonus)) {
               bonuses.add(type.cast(skillBonus));
            }
         }
      }

      return bonuses;
   }
}
