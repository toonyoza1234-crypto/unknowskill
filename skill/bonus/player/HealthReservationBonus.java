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
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class HealthReservationBonus implements SkillBonus<HealthReservationBonus> {
   private float amount;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;

   public HealthReservationBonus(float amount) {
      this.amount = amount;
   }

   public float getAmount(Player player) {
      return !this.playerCondition.test(player) ? 0.0F : this.amount * this.playerMultiplier.getValue(player);
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.HEALTH_RESERVATION.get();
   }

   public HealthReservationBonus copy() {
      HealthReservationBonus bonus = new HealthReservationBonus(this.amount);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      return bonus;
   }

   public HealthReservationBonus multiply(double multiplier) {
      this.amount *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof HealthReservationBonus otherBonus) {
         return !Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier) ? false : Objects.equals(otherBonus.playerCondition, this.playerCondition);
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<HealthReservationBonus> merge(SkillBonus<?> other) {
      if (other instanceof HealthReservationBonus otherBonus) {
         HealthReservationBonus mergedBonus = new HealthReservationBonus(otherBonus.amount + this.amount);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      MutableComponent tooltip = TooltipHelper.getSkillBonusTooltip(this.getDescriptionId(), (double)this.amount, Operation.MULTIPLY_BASE);
      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.amount < 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<HealthReservationBonus> consumer) {
      editor.addLabel(0, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.amount).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
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
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<HealthReservationBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<HealthReservationBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<HealthReservationBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, condition -> {
         this.setPlayerCondition(condition);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<HealthReservationBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectAmount(Consumer<HealthReservationBonus> consumer, Double value) {
      this.setAmount(value.floatValue());
      consumer.accept(this.copy());
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public HealthReservationBonus deserialize(JsonObject json) throws JsonParseException {
         float amount = SerializationHelper.getElement(json, "amount").getAsFloat();
         HealthReservationBonus bonus = new HealthReservationBonus(amount);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof HealthReservationBonus aBonus) {
            json.addProperty("amount", aBonus.amount);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public HealthReservationBonus deserialize(CompoundTag tag) {
         float amount = tag.getFloat("amount");
         HealthReservationBonus bonus = new HealthReservationBonus(amount);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof HealthReservationBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("amount", aBonus.amount);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public HealthReservationBonus deserialize(FriendlyByteBuf buf) {
         float amount = buf.readFloat();
         HealthReservationBonus bonus = new HealthReservationBonus(amount);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof HealthReservationBonus aBonus) {
            buf.writeFloat(aBonus.amount);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new HealthReservationBonus(0.05F);
      }
   }
}
