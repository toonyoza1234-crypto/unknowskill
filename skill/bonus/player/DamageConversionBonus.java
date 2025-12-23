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
import daripher.skilltree.skill.bonus.predicate.damage.MagicDamageCondition;
import daripher.skilltree.skill.bonus.predicate.damage.MeleeDamageCondition;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class DamageConversionBonus implements SkillBonus<DamageConversionBonus> {
   private float amount;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingMultiplier targetMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private LivingEntityPredicate targetCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private DamageCondition originalDamageCondition;
   @Nonnull
   private DamageCondition resultDamageCondition;

   public DamageConversionBonus(float amount, @NotNull DamageCondition originalDamageCondition, @NotNull DamageCondition resultDamageCondition) {
      this.amount = amount;
      this.originalDamageCondition = originalDamageCondition;
      this.resultDamageCondition = resultDamageCondition;
   }

   public float getConversionRate(DamageSource source, Player player, LivingEntity target) {
      if (!this.originalDamageCondition.met(source)) {
         return 0.0F;
      } else if (!this.playerCondition.test(player)) {
         return 0.0F;
      } else {
         return !this.targetCondition.test(target) ? 0.0F : this.amount * this.playerMultiplier.getValue(player) * this.targetMultiplier.getValue(target);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.DAMAGE_CONVERSION.get();
   }

   public DamageConversionBonus copy() {
      DamageConversionBonus bonus = new DamageConversionBonus(this.amount, this.originalDamageCondition, this.resultDamageCondition);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.targetMultiplier = this.targetMultiplier;
      bonus.playerCondition = this.playerCondition;
      bonus.targetCondition = this.targetCondition;
      return bonus;
   }

   public DamageConversionBonus multiply(double multiplier) {
      this.amount *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof DamageConversionBonus otherBonus) {
         if (!Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier)) {
            return false;
         } else if (!Objects.equals(otherBonus.targetMultiplier, this.targetMultiplier)) {
            return false;
         } else if (!Objects.equals(otherBonus.playerCondition, this.playerCondition)) {
            return false;
         } else if (!Objects.equals(otherBonus.originalDamageCondition, this.originalDamageCondition)) {
            return false;
         } else {
            return !Objects.equals(otherBonus.resultDamageCondition, this.resultDamageCondition)
               ? false
               : Objects.equals(otherBonus.targetCondition, this.targetCondition);
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<DamageConversionBonus> merge(SkillBonus<?> other) {
      if (other instanceof DamageConversionBonus otherBonus) {
         float mergedAmount = otherBonus.amount + this.amount;
         DamageConversionBonus mergedBonus = new DamageConversionBonus(mergedAmount, this.originalDamageCondition, this.resultDamageCondition);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.targetMultiplier = this.targetMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         mergedBonus.targetCondition = this.targetCondition;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String formattedAmount = TooltipHelper.formatNumber((double)(this.amount * 100.0F));
      MutableComponent tooltip = Component.translatable(
         this.getDescriptionId(), new Object[]{formattedAmount, this.originalDamageCondition.getTooltip(), this.resultDamageCondition.getTooltip()}
      );
      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.targetMultiplier.getTooltip(tooltip, SkillBonus.Target.ENEMY);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.targetCondition.getTooltip(tooltip, SkillBonus.Target.ENEMY);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.amount > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<DamageConversionBonus> consumer) {
      editor.addLabel(0, 0, "Conversion", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.amount).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Original Damage", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.originalDamageCondition).setResponder(condition -> this.selectDamageCondition(consumer, condition));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Result Damage", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.resultDamageCondition).setResponder(condition -> this.selectResultDamageCondition(consumer, condition));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Target Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.targetCondition)
         .setResponder(condition -> this.selectTargetCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addTargetConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Target Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.targetMultiplier)
         .setResponder(multiplier -> this.selectTargetMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addTargetMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void selectAmount(Consumer<DamageConversionBonus> consumer, Double value) {
      this.setAmount(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addTargetMultiplierWidgets(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer) {
      this.targetMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setEnemyMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectTargetMultiplier(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer, LivingMultiplier multiplier) {
      this.setEnemyMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addTargetConditionWidgets(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer) {
      this.targetCondition.addEditorWidgets(editor, c -> {
         this.setTargetCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectTargetCondition(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer, LivingEntityPredicate condition) {
      this.setTargetCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<DamageConversionBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectDamageCondition(Consumer<DamageConversionBonus> consumer, DamageCondition condition) {
      this.setDamageCondition(condition);
      consumer.accept(this.copy());
   }

   private void selectResultDamageCondition(Consumer<DamageConversionBonus> consumer, DamageCondition condition) {
      this.setResultDamageCondition(condition);
      consumer.accept(this.copy());
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setDamageCondition(DamageCondition condition) {
      this.originalDamageCondition = condition;
      return this;
   }

   public SkillBonus<?> setResultDamageCondition(DamageCondition condition) {
      this.resultDamageCondition = condition;
      return this;
   }

   public SkillBonus<?> setTargetCondition(LivingEntityPredicate condition) {
      this.targetCondition = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public SkillBonus<?> setEnemyMultiplier(LivingMultiplier multiplier) {
      this.targetMultiplier = multiplier;
      return this;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   @Nonnull
   public DamageCondition getOriginalDamageCondition() {
      return this.originalDamageCondition;
   }

   @Nonnull
   public DamageCondition getResultDamageCondition() {
      return this.resultDamageCondition;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public DamageConversionBonus deserialize(JsonObject json) throws JsonParseException {
         float amount = SerializationHelper.getElement(json, "amount").getAsFloat();
         DamageCondition originalDamageCondition = SerializationHelper.deserializeDamageCondition(json, "original_damage");
         DamageCondition resultDamageCondition = SerializationHelper.deserializeDamageCondition(json, "result_damage");
         DamageConversionBonus bonus = new DamageConversionBonus(amount, originalDamageCondition, resultDamageCondition);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.targetMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "enemy_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         bonus.targetCondition = SerializationHelper.deserializeLivingCondition(json, "target_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof DamageConversionBonus aBonus) {
            json.addProperty("amount", aBonus.amount);
            SerializationHelper.serializeDamageCondition(json, aBonus.originalDamageCondition, "original_damage");
            SerializationHelper.serializeDamageCondition(json, aBonus.resultDamageCondition, "result_damage");
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(json, aBonus.targetMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeLivingCondition(json, aBonus.targetCondition, "target_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public DamageConversionBonus deserialize(CompoundTag tag) {
         float amount = tag.getFloat("amount");
         DamageCondition originalDamageCondition = SerializationHelper.deserializeDamageCondition(tag, "original_damage");
         DamageCondition resultDamageCondition = SerializationHelper.deserializeDamageCondition(tag, "result_damage");
         DamageConversionBonus bonus = new DamageConversionBonus(amount, originalDamageCondition, resultDamageCondition);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.targetMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "enemy_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         bonus.targetCondition = SerializationHelper.deserializeLivingCondition(tag, "target_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof DamageConversionBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("amount", aBonus.amount);
            SerializationHelper.serializeDamageCondition(tag, aBonus.originalDamageCondition, "original_damage");
            SerializationHelper.serializeDamageCondition(tag, aBonus.resultDamageCondition, "result_damage");
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.targetMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeLivingCondition(tag, aBonus.targetCondition, "target_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public DamageConversionBonus deserialize(FriendlyByteBuf buf) {
         float amount = buf.readFloat();
         DamageCondition originalDamageCondition = NetworkHelper.readDamageCondition(buf);
         DamageCondition resultDamageCondition = NetworkHelper.readDamageCondition(buf);
         DamageConversionBonus bonus = new DamageConversionBonus(amount, originalDamageCondition, resultDamageCondition);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.targetMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         bonus.targetCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof DamageConversionBonus aBonus) {
            buf.writeFloat(aBonus.amount);
            NetworkHelper.writeDamageCondition(buf, aBonus.originalDamageCondition);
            NetworkHelper.writeDamageCondition(buf, aBonus.resultDamageCondition);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.targetMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            NetworkHelper.writeLivingCondition(buf, aBonus.targetCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new DamageConversionBonus(0.05F, new MeleeDamageCondition(), new MagicDamageCondition());
      }
   }
}
