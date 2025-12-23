package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.damage.DamageCondition;
import daripher.skilltree.skill.bonus.predicate.damage.MeleeDamageCondition;
import daripher.skilltree.skill.bonus.predicate.damage.NoneDamageCondition;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class DamageAvoidanceBonus implements SkillBonus<DamageAvoidanceBonus> {
   private float chance;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingMultiplier attackerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private LivingEntityPredicate attackerCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private DamageCondition damageCondition = NoneDamageCondition.INSTANCE;

   public DamageAvoidanceBonus(float chance) {
      this.chance = chance;
   }

   public float getAvoidanceChance(DamageSource source, Player player, @Nullable LivingEntity attacker) {
      if (!this.damageCondition.met(source)) {
         return 0.0F;
      } else if (!this.playerCondition.test(player)) {
         return 0.0F;
      } else if (this.attackerCondition == NoneLivingEntityPredicate.INSTANCE || attacker != null && this.attackerCondition.test(attacker)) {
         float result = this.chance * this.playerMultiplier.getValue(player);
         if (attacker != null) {
            result *= this.attackerMultiplier.getValue(attacker);
         }

         return result;
      } else {
         return 0.0F;
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.DAMAGE_AVOIDANCE.get();
   }

   public DamageAvoidanceBonus copy() {
      DamageAvoidanceBonus bonus = new DamageAvoidanceBonus(this.chance);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.attackerMultiplier = this.attackerMultiplier;
      bonus.playerCondition = this.playerCondition;
      bonus.damageCondition = this.damageCondition;
      bonus.attackerCondition = this.attackerCondition;
      return bonus;
   }

   public DamageAvoidanceBonus multiply(double multiplier) {
      this.chance *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof DamageAvoidanceBonus otherBonus) {
         if (!Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier)) {
            return false;
         } else if (!Objects.equals(otherBonus.attackerMultiplier, this.attackerMultiplier)) {
            return false;
         } else if (!Objects.equals(otherBonus.playerCondition, this.playerCondition)) {
            return false;
         } else {
            return !Objects.equals(otherBonus.damageCondition, this.damageCondition)
               ? false
               : Objects.equals(otherBonus.attackerCondition, this.attackerCondition);
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<DamageAvoidanceBonus> merge(SkillBonus<?> other) {
      if (other instanceof DamageAvoidanceBonus otherBonus) {
         float mergedChance = otherBonus.chance + this.chance;
         DamageAvoidanceBonus mergedBonus = new DamageAvoidanceBonus(mergedChance);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.attackerMultiplier = this.attackerMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         mergedBonus.damageCondition = this.damageCondition;
         mergedBonus.attackerCondition = this.attackerCondition;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      MutableComponent tooltip = Component.translatable(this.getDescriptionId(), new Object[]{this.damageCondition.getTooltip()});
      tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.attackerMultiplier.getTooltip(tooltip, SkillBonus.Target.ENEMY);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.attackerCondition.getTooltip(tooltip, SkillBonus.Target.ENEMY);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<DamageAvoidanceBonus> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Damage Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.damageCondition).setResponder(condition -> this.selectDamageCondition(consumer, condition));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Attacker Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.attackerCondition)
         .setResponder(condition -> this.selectTargetCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addTargetConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Attacker Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.attackerMultiplier)
         .setResponder(multiplier -> this.selectTargetMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addTargetMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void selectChance(Consumer<DamageAvoidanceBonus> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addTargetMultiplierWidgets(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer) {
      this.attackerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setEnemyMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectTargetMultiplier(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer, LivingMultiplier multiplier) {
      this.setEnemyMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addTargetConditionWidgets(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer) {
      this.attackerCondition.addEditorWidgets(editor, c -> {
         this.setTargetCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectTargetCondition(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer, LivingEntityPredicate condition) {
      this.setTargetCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<DamageAvoidanceBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectDamageCondition(Consumer<DamageAvoidanceBonus> consumer, DamageCondition condition) {
      this.setDamageCondition(condition);
      consumer.accept(this.copy());
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setDamageCondition(DamageCondition condition) {
      this.damageCondition = condition;
      return this;
   }

   public SkillBonus<?> setTargetCondition(LivingEntityPredicate condition) {
      this.attackerCondition = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public SkillBonus<?> setEnemyMultiplier(LivingMultiplier multiplier) {
      this.attackerMultiplier = multiplier;
      return this;
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public DamageAvoidanceBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         DamageAvoidanceBonus bonus = new DamageAvoidanceBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.attackerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "attacker_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         bonus.damageCondition = SerializationHelper.deserializeDamageCondition(json);
         bonus.attackerCondition = SerializationHelper.deserializeLivingCondition(json, "attacker_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof DamageAvoidanceBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(json, aBonus.attackerMultiplier, "attacker_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeDamageCondition(json, aBonus.damageCondition);
            SerializationHelper.serializeLivingCondition(json, aBonus.attackerCondition, "attacker_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public DamageAvoidanceBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         DamageAvoidanceBonus bonus = new DamageAvoidanceBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.attackerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "attacker_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         bonus.damageCondition = SerializationHelper.deserializeDamageCondition(tag);
         bonus.attackerCondition = SerializationHelper.deserializeLivingCondition(tag, "attacker_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof DamageAvoidanceBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.attackerMultiplier, "attacker_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeDamageCondition(tag, aBonus.damageCondition);
            SerializationHelper.serializeLivingCondition(tag, aBonus.attackerCondition, "attacker_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public DamageAvoidanceBonus deserialize(FriendlyByteBuf buf) {
         float chance = buf.readFloat();
         DamageAvoidanceBonus bonus = new DamageAvoidanceBonus(chance);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.attackerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         bonus.damageCondition = NetworkHelper.readDamageCondition(buf);
         bonus.attackerCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof DamageAvoidanceBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.attackerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            NetworkHelper.writeDamageCondition(buf, aBonus.damageCondition);
            NetworkHelper.writeLivingCondition(buf, aBonus.attackerCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new DamageAvoidanceBonus(0.1F).setDamageCondition(new MeleeDamageCondition());
      }
   }
}
