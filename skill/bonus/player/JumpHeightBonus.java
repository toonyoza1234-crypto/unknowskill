package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
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

public final class JumpHeightBonus implements SkillBonus<JumpHeightBonus> {
   @Nonnull
   private LivingEntityPredicate playerCondition;
   private float multiplier;

   public JumpHeightBonus(@Nonnull LivingEntityPredicate playerCondition, float multiplier) {
      this.playerCondition = playerCondition;
      this.multiplier = multiplier;
   }

   public JumpHeightBonus(float multiplier) {
      this(NoneLivingEntityPredicate.INSTANCE, multiplier);
   }

   public float getJumpHeightMultiplier(Player player) {
      return !this.playerCondition.test(player) ? 0.0F : this.multiplier;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.JUMP_HEIGHT.get();
   }

   public JumpHeightBonus copy() {
      return new JumpHeightBonus(this.playerCondition, this.multiplier);
   }

   public JumpHeightBonus multiply(double multiplier) {
      this.multiplier = (float)((double)this.multiplier * multiplier);
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof JumpHeightBonus otherBonus ? Objects.equals(otherBonus.playerCondition, this.playerCondition) : false;
   }

   @Override
   public SkillBonus<JumpHeightBonus> merge(SkillBonus<?> other) {
      if (other instanceof JumpHeightBonus otherBonus) {
         return new JumpHeightBonus(this.playerCondition, otherBonus.multiplier + this.multiplier);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      MutableComponent tooltip = TooltipHelper.getSkillBonusTooltip(this.getDescriptionId(), (double)this.multiplier, Operation.MULTIPLY_BASE);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.multiplier > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<JumpHeightBonus> consumer) {
      editor.addLabel(0, 0, "Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.multiplier).setNumericResponder(value -> this.selectMultiplier(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<JumpHeightBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, condition -> {
         this.setPlayerCondition(condition);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<JumpHeightBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectMultiplier(Consumer<JumpHeightBonus> consumer, Double value) {
      this.setMultiplier(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setPlayerCondition(@Nonnull LivingEntityPredicate playerCondition) {
      this.playerCondition = playerCondition;
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   public float getMultiplier() {
      return this.multiplier;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         JumpHeightBonus that = (JumpHeightBonus)obj;
         return !Objects.equals(this.playerCondition, that.playerCondition) ? false : this.multiplier == that.multiplier;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerCondition, this.multiplier);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public JumpHeightBonus deserialize(JsonObject json) throws JsonParseException {
         LivingEntityPredicate condition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         float multiplier = SerializationHelper.getElement(json, "multiplier").getAsFloat();
         return new JumpHeightBonus(condition, multiplier);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof JumpHeightBonus aBonus) {
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            json.addProperty("multiplier", aBonus.multiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public JumpHeightBonus deserialize(CompoundTag tag) {
         LivingEntityPredicate condition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         float multiplier = tag.getFloat("multiplier");
         return new JumpHeightBonus(condition, multiplier);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof JumpHeightBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            tag.putFloat("multiplier", aBonus.multiplier);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public JumpHeightBonus deserialize(FriendlyByteBuf buf) {
         return new JumpHeightBonus(NetworkHelper.readLivingCondition(buf), buf.readFloat());
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof JumpHeightBonus aBonus) {
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            buf.writeFloat(aBonus.multiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new JumpHeightBonus(0.1F);
      }
   }
}
