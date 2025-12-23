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
import daripher.skilltree.skill.bonus.predicate.effect.EffectType;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class EffectDurationBonus implements SkillBonus<EffectDurationBonus> {
   private EffectType effectType;
   private float duration;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   private SkillBonus.Target target;
   @Nonnull
   private LivingMultiplier enemyMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate enemyCondition = NoneLivingEntityPredicate.INSTANCE;

   public EffectDurationBonus(EffectType effectType, float duration, SkillBonus.Target target) {
      this.effectType = effectType;
      this.duration = duration;
      this.target = target;
   }

   public float getDuration(@Nullable Player effectSource, LivingEntity entity) {
      if (this.target == SkillBonus.Target.PLAYER) {
         return !this.playerCondition.test(entity) ? 0.0F : this.duration * this.playerMultiplier.getValue(entity);
      } else if (!this.enemyCondition.test(entity)) {
         return 0.0F;
      } else {
         float duration = this.duration;
         return effectSource != null && !this.playerCondition.test(effectSource)
            ? 0.0F
            : duration * this.playerMultiplier.getValue(entity) * this.enemyMultiplier.getValue(entity);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.EFFECT_DURATION.get();
   }

   public EffectDurationBonus copy() {
      EffectDurationBonus bonus = new EffectDurationBonus(this.effectType, this.duration, this.target);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      bonus.enemyCondition = this.enemyCondition;
      bonus.enemyMultiplier = this.enemyMultiplier;
      return bonus;
   }

   public EffectDurationBonus multiply(double multiplier) {
      this.duration = (float)((double)this.duration * multiplier);
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof EffectDurationBonus otherBonus) {
         if (otherBonus.playerCondition != this.playerCondition) {
            return false;
         } else if (otherBonus.playerMultiplier != this.playerMultiplier) {
            return false;
         } else if (otherBonus.target != this.target) {
            return false;
         } else if (otherBonus.enemyCondition != this.enemyCondition) {
            return false;
         } else {
            return otherBonus.enemyMultiplier != this.enemyMultiplier ? false : otherBonus.effectType == this.effectType;
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<EffectDurationBonus> merge(SkillBonus<?> other) {
      if (other instanceof EffectDurationBonus otherBonus) {
         EffectDurationBonus mergedBonus = new EffectDurationBonus(this.effectType, this.duration + otherBonus.duration, this.target);
         mergedBonus.playerCondition = this.playerCondition;
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.enemyCondition = this.enemyCondition;
         mergedBonus.enemyMultiplier = this.enemyMultiplier;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component effectTypeDescription = Component.translatable(this.effectType.getDescriptionId() + ".plural");
      String key = this.getDescriptionId() + "." + this.target.getName();
      MutableComponent tooltip = Component.translatable(key, new Object[]{effectTypeDescription});
      tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.duration, Operation.MULTIPLY_BASE);
      tooltip = this.playerMultiplier.getTooltip(tooltip, this.target);
      tooltip = this.playerCondition.getTooltip(tooltip, this.target);
      tooltip = this.enemyMultiplier.getTooltip(tooltip, this.target);
      tooltip = this.enemyCondition.getTooltip(tooltip, this.target);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.duration > 0.0F ^ this.target == SkillBonus.Target.PLAYER ^ this.effectType != EffectType.HARMFUL;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int index, Consumer<EffectDurationBonus> consumer) {
      editor.addLabel(0, 0, "Effect Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.effectType)
         .setElementNameGetter(effectType -> Component.literal(effectType.name()))
         .setResponder(effectType -> this.selectEffectType(consumer, effectType));
      editor.increaseHeight(19);
      editor.addLabel(110, 0, "Duration", ChatFormatting.GOLD);
      editor.addLabel(0, 0, "Target", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(110, 0, 50, 14, (double)this.duration).setNumericResponder(value -> this.selectDuration(consumer, value));
      editor.addSelection(0, 0, 80, 1, this.target)
         .setNameGetter(target -> Component.literal(target.toString()))
         .setResponder(target -> this.selectTarget(editor, consumer, target));
      editor.increaseHeight(29);
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      if (this.target == SkillBonus.Target.ENEMY) {
         editor.addLabel(0, 0, "Enemy Condition", ChatFormatting.GOLD);
         editor.increaseHeight(19);
         editor.addSelectionMenu(0, 0, 200, this.enemyCondition)
            .setResponder(condition -> this.selectEnemyCondition(editor, consumer, condition))
            .setMenuInitFunc(() -> this.addEnemyConditionWidgets(editor, consumer));
         editor.increaseHeight(19);
         editor.addLabel(0, 0, "Enemy Multiplier", ChatFormatting.GOLD);
         editor.increaseHeight(19);
         editor.addSelectionMenu(0, 0, 200, this.enemyMultiplier)
            .setResponder(multiplier -> this.selectEnemyMultiplier(editor, consumer, multiplier))
            .setMenuInitFunc(() -> this.addEnemyMultiplierWidgets(editor, consumer));
         editor.increaseHeight(19);
      }
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectEnemyMultiplier(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer, LivingMultiplier multiplier) {
      this.setEnemyMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectEnemyCondition(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer, LivingEntityPredicate condition) {
      this.setEnemyCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectEffectType(Consumer<EffectDurationBonus> consumer, EffectType effectType) {
      this.setEffectType(effectType);
      consumer.accept(this.copy());
   }

   private void selectDuration(Consumer<EffectDurationBonus> consumer, Double duration) {
      this.setDuration(duration.floatValue());
      consumer.accept(this.copy());
   }

   private void selectTarget(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer, SkillBonus.Target target) {
      this.setTarget(target);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, m -> {
         this.setPlayerMultiplier(m);
         consumer.accept(this.copy());
      });
   }

   private void addEnemyConditionWidgets(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer) {
      this.enemyCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void addEnemyMultiplierWidgets(SkillTreeEditor editor, Consumer<EffectDurationBonus> consumer) {
      this.enemyMultiplier.addEditorWidgets(editor, m -> {
         this.setPlayerMultiplier(m);
         consumer.accept(this.copy());
      });
   }

   public void setDuration(float duration) {
      this.duration = duration;
   }

   public void setEffectType(EffectType effectType) {
      this.effectType = effectType;
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public void setTarget(SkillBonus.Target target) {
      this.target = target;
   }

   public void setEnemyCondition(@Nonnull LivingEntityPredicate enemyCondition) {
      this.enemyCondition = enemyCondition;
   }

   public void setEnemyMultiplier(@Nonnull LivingMultiplier enemyMultiplier) {
      this.enemyMultiplier = enemyMultiplier;
   }

   public SkillBonus.Target getTarget() {
      return this.target;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public EffectDurationBonus deserialize(JsonObject json) throws JsonParseException {
         EffectType effectType = EffectType.fromName(json.get("effect_type").getAsString());
         float duration = json.get("duration").getAsFloat();
         SkillBonus.Target target = SkillBonus.Target.fromName(json.get("target").getAsString());
         EffectDurationBonus bonus = new EffectDurationBonus(effectType, duration, target);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         bonus.enemyMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "enemy_multiplier");
         bonus.enemyCondition = SerializationHelper.deserializeLivingCondition(json, "enemy_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof EffectDurationBonus aBonus) {
            json.addProperty("effect_type", aBonus.effectType.getName());
            json.addProperty("duration", aBonus.duration);
            json.addProperty("target", aBonus.target.getName());
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aBonus.enemyMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.enemyCondition, "enemy_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public EffectDurationBonus deserialize(CompoundTag tag) {
         EffectType effectType = EffectType.fromName(tag.getString("effect_type"));
         float duration = tag.getFloat("duration");
         SkillBonus.Target target = SkillBonus.Target.fromName(tag.getString("target"));
         EffectDurationBonus bonus = new EffectDurationBonus(effectType, duration, target);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         bonus.enemyMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "enemy_multiplier");
         bonus.enemyCondition = SerializationHelper.deserializeLivingCondition(tag, "enemy_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof EffectDurationBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putString("effect_type", aBonus.effectType.getName());
            tag.putFloat("duration", aBonus.duration);
            tag.putString("target", aBonus.target.getName());
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.enemyMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.enemyCondition, "enemy_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public EffectDurationBonus deserialize(FriendlyByteBuf buf) {
         EffectType effectType = EffectType.values()[buf.readInt()];
         float duration = buf.readFloat();
         SkillBonus.Target target = SkillBonus.Target.values()[buf.readInt()];
         EffectDurationBonus bonus = new EffectDurationBonus(effectType, duration, target);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         bonus.enemyMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.enemyCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof EffectDurationBonus aBonus) {
            buf.writeInt(aBonus.effectType.ordinal());
            buf.writeFloat(aBonus.duration);
            buf.writeInt(aBonus.target.ordinal());
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.enemyMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.enemyCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new EffectDurationBonus(EffectType.BENEFICIAL, 0.1F, SkillBonus.Target.PLAYER);
      }
   }
}
