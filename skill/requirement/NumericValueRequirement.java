package daripher.skilltree.skill.requirement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.init.PSTSkillRequirements;
import daripher.skilltree.skill.bonus.function.EffectAmountFunction;
import daripher.skilltree.skill.bonus.predicate.effect.EffectType;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class NumericValueRequirement implements SkillRequirement<NumericValueRequirement> {
   private FloatFunctionEntityPredicate condition;

   public NumericValueRequirement(FloatFunctionEntityPredicate condition) {
      this.condition = condition;
   }

   public boolean test(Player player) {
      return this.condition.test((LivingEntity)player);
   }

   @Override
   public MutableComponent getTooltip() {
      return this.condition.getValueProvider().getRequirementTooltip(this.condition.getLogic(), this.condition.getRequiredValue());
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<NumericValueRequirement> consumer) {
      this.condition.addEditorWidgets(editor, condition -> this.setCondition((FloatFunctionEntityPredicate)condition, consumer));
   }

   public void setCondition(FloatFunctionEntityPredicate condition, Consumer<NumericValueRequirement> consumer) {
      this.condition = condition;
      consumer.accept(this);
   }

   public NumericValueRequirement copy() {
      return new NumericValueRequirement(this.condition);
   }

   @Override
   public SkillRequirement.Serializer getSerializer() {
      return (SkillRequirement.Serializer)PSTSkillRequirements.NUMERIC_VALUE.get();
   }

   public static class Serializer implements SkillRequirement.Serializer {
      public SkillRequirement<?> deserialize(JsonObject json) throws JsonParseException {
         FloatFunctionEntityPredicate condition = (FloatFunctionEntityPredicate)((LivingEntityPredicate.Serializer)PSTLivingConditions.NUMERIC_VALUE.get())
            .deserialize(json);
         return new NumericValueRequirement(condition);
      }

      public void serialize(JsonObject json, SkillRequirement<?> requirement) {
         if (requirement instanceof NumericValueRequirement aRequirement) {
            aRequirement.condition.getSerializer().serialize(json, aRequirement.condition);
         }
      }

      public SkillRequirement<?> deserialize(CompoundTag tag) {
         FloatFunctionEntityPredicate condition = (FloatFunctionEntityPredicate)((LivingEntityPredicate.Serializer)PSTLivingConditions.NUMERIC_VALUE.get())
            .deserialize(tag);
         return new NumericValueRequirement(condition);
      }

      public CompoundTag serialize(SkillRequirement<?> requirement) {
         CompoundTag tag = new CompoundTag();
         return requirement instanceof NumericValueRequirement aRequirement ? aRequirement.condition.getSerializer().serialize(aRequirement.condition) : tag;
      }

      public SkillRequirement<?> deserialize(FriendlyByteBuf buf) {
         FloatFunctionEntityPredicate condition = (FloatFunctionEntityPredicate)((LivingEntityPredicate.Serializer)PSTLivingConditions.NUMERIC_VALUE.get())
            .deserialize(buf);
         return new NumericValueRequirement(condition);
      }

      public void serialize(FriendlyByteBuf buf, SkillRequirement<?> requirement) {
         if (requirement instanceof NumericValueRequirement aRequirement) {
            aRequirement.condition.getSerializer().serialize(buf, aRequirement.condition);
         }
      }

      @Override
      public SkillRequirement<?> createDefaultInstance() {
         return new NumericValueRequirement(
            new FloatFunctionEntityPredicate(new EffectAmountFunction(EffectType.BENEFICIAL), 5.0F, FloatFunctionEntityPredicate.Logic.MORE)
         );
      }
   }
}
