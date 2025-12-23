package daripher.skilltree.skill.bonus.predicate.living;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.function.FloatFunction;
import daripher.skilltree.skill.bonus.function.HealthLevelFunction;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public class FloatFunctionEntityPredicate implements LivingEntityPredicate {
   private FloatFunction<?> valueProvider;
   private float requiredValue;
   private FloatFunctionEntityPredicate.Logic logic;

   public FloatFunctionEntityPredicate(FloatFunction<?> valueProvider, float requiredValue, FloatFunctionEntityPredicate.Logic logic) {
      this.valueProvider = valueProvider;
      this.requiredValue = requiredValue;
      this.logic = logic;
   }

   public boolean test(LivingEntity living) {
      float value = this.valueProvider.apply(living);

      return switch (this.logic) {
         case EQUAL -> value == this.requiredValue;
         case MORE -> value > this.requiredValue;
         case LESS -> value < this.requiredValue;
         case AT_LEAST -> value >= this.requiredValue;
         case AT_MOST -> value <= this.requiredValue;
      };
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      return this.valueProvider.getConditionTooltip(target, this.logic, bonusTooltip, this.requiredValue);
   }

   @Override
   public LivingEntityPredicate.Serializer getSerializer() {
      return (LivingEntityPredicate.Serializer)PSTLivingConditions.NUMERIC_VALUE.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer) {
      editor.addLabel(0, 0, "Value Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.valueProvider)
         .setResponder(provider -> this.selectValueProvider(editor, consumer, provider))
         .setMenuInitFunc(() -> this.addValueProviderWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Logic", ChatFormatting.GREEN);
      editor.addLabel(100, 0, "Required Value", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 90, this.logic)
         .setElementNameGetter(logic -> Component.literal(logic.name()))
         .setResponder(logic -> this.selectLogic(consumer, logic));
      editor.addNumericTextField(100, 0, 50, 14, (double)this.requiredValue).setNumericResponder(value -> this.selectRequiredValue(consumer, value));
      editor.increaseHeight(19);
   }

   private void addValueProviderWidgets(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer) {
      this.valueProvider.addEditorWidgets(editor, provider -> this.selectValueProvider(editor, consumer, provider));
   }

   private void selectRequiredValue(Consumer<LivingEntityPredicate> consumer, Double value) {
      this.setRequiredValue(value.floatValue());
      consumer.accept(this);
   }

   private void selectValueProvider(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer, FloatFunction<?> provider) {
      this.setValueProvider(provider);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   private void selectLogic(Consumer<LivingEntityPredicate> consumer, FloatFunctionEntityPredicate.Logic logic) {
      this.setLogic(logic);
      consumer.accept(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FloatFunctionEntityPredicate that = (FloatFunctionEntityPredicate)o;
         if (Float.compare(this.requiredValue, that.requiredValue) != 0) {
            return false;
         } else {
            return !Objects.equals(this.valueProvider, that.valueProvider) ? false : this.logic == that.logic;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.valueProvider, this.requiredValue, this.logic);
   }

   public void setValueProvider(FloatFunction<?> provider) {
      this.valueProvider = provider;
   }

   public void setRequiredValue(float requiredValue) {
      this.requiredValue = requiredValue;
   }

   public void setLogic(FloatFunctionEntityPredicate.Logic logic) {
      this.logic = logic;
   }

   public FloatFunction<?> getValueProvider() {
      return this.valueProvider;
   }

   public FloatFunctionEntityPredicate.Logic getLogic() {
      return this.logic;
   }

   public float getRequiredValue() {
      return this.requiredValue;
   }

   public static enum Logic {
      MORE,
      LESS,
      EQUAL,
      AT_LEAST,
      AT_MOST;

      public String getName() {
         return this.name().toLowerCase();
      }

      public Component getTooltip(String subtype, Object... args) {
         String conditionDescriptionId = ((LivingEntityPredicate.Serializer)PSTLivingConditions.NUMERIC_VALUE.get()).createDefaultInstance().getDescriptionId();
         String key = conditionDescriptionId + "." + this.getName();
         return TooltipHelper.getOptionalTooltip(key, subtype, args);
      }
   }

   public static class Serializer implements LivingEntityPredicate.Serializer {
      public LivingEntityPredicate deserialize(JsonObject json) throws JsonParseException {
         FloatFunction<?> valueProvider = SerializationHelper.deserializeValueProvider(json);
         float requiredValue = json.get("required_value").getAsFloat();
         FloatFunctionEntityPredicate.Logic logic = FloatFunctionEntityPredicate.Logic.valueOf(json.get("logic").getAsString());
         return new FloatFunctionEntityPredicate(valueProvider, requiredValue, logic);
      }

      public void serialize(JsonObject json, LivingEntityPredicate condition) {
         if (condition instanceof FloatFunctionEntityPredicate aCondition) {
            SerializationHelper.serializeValueProvider(json, aCondition.valueProvider);
            json.addProperty("required_value", aCondition.requiredValue);
            json.addProperty("logic", aCondition.logic.name());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(CompoundTag tag) {
         FloatFunction<?> valueProvider = SerializationHelper.deserializeValueProvider(tag);
         float requiredValue = tag.getFloat("required_value");
         FloatFunctionEntityPredicate.Logic logic = FloatFunctionEntityPredicate.Logic.valueOf(tag.getString("logic"));
         return new FloatFunctionEntityPredicate(valueProvider, requiredValue, logic);
      }

      public CompoundTag serialize(LivingEntityPredicate condition) {
         if (condition instanceof FloatFunctionEntityPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeValueProvider(tag, aCondition.valueProvider);
            tag.putFloat("required_value", aCondition.requiredValue);
            tag.putString("logic", aCondition.logic.name());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(FriendlyByteBuf buf) {
         FloatFunction<?> valueProvider = NetworkHelper.readValueProvider(buf);
         float requiredValue = buf.readFloat();
         FloatFunctionEntityPredicate.Logic logic = FloatFunctionEntityPredicate.Logic.values()[buf.readInt()];
         return new FloatFunctionEntityPredicate(valueProvider, requiredValue, logic);
      }

      public void serialize(FriendlyByteBuf buf, LivingEntityPredicate condition) {
         if (condition instanceof FloatFunctionEntityPredicate aCondition) {
            NetworkHelper.writeValueProvider(buf, aCondition.valueProvider);
            buf.writeFloat(aCondition.requiredValue);
            buf.writeInt(aCondition.logic.ordinal());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingEntityPredicate createDefaultInstance() {
         return new FloatFunctionEntityPredicate(new HealthLevelFunction(true, false), 1.0F, FloatFunctionEntityPredicate.Logic.EQUAL);
      }
   }
}
