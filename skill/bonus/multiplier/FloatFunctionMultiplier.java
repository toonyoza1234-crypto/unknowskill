package daripher.skilltree.skill.bonus.multiplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.function.AttributeValueFunction;
import daripher.skilltree.skill.bonus.function.FloatFunction;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class FloatFunctionMultiplier implements LivingMultiplier {
   private FloatFunction<?> floatFunction;
   private float divisor;

   public FloatFunctionMultiplier(FloatFunction<?> floatFunction, float divisor) {
      this.floatFunction = floatFunction;
      this.divisor = divisor;
   }

   @Override
   public float getValue(LivingEntity entity) {
      return (float)((int)(this.floatFunction.apply(entity) / this.divisor));
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      return this.floatFunction.getMultiplierTooltip(target, this.divisor, bonusTooltip);
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
      editor.addLabel(0, 0, "Value Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.floatFunction).setResponder(provider -> {
         this.selectValueProvider(consumer, provider);
         this.addValueProviderWidgets(editor, consumer);
         editor.rebuildWidgets();
      }).setMenuInitFunc(() -> this.addValueProviderWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Divisor", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.divisor)
         .setNumericFilter(value -> value > 0.0)
         .setNumericResponder(value -> this.selectDivisor(consumer, value));
      editor.increaseHeight(19);
   }

   private void addValueProviderWidgets(SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
      this.floatFunction.addEditorWidgets(editor, provider -> this.selectValueProvider(consumer, provider));
   }

   private void selectDivisor(Consumer<LivingMultiplier> consumer, Double value) {
      this.setDivisor(value.floatValue());
      consumer.accept(this);
   }

   private void selectValueProvider(Consumer<LivingMultiplier> consumer, FloatFunction<?> valueProvider) {
      this.setFloatFunction(valueProvider);
      consumer.accept(this);
   }

   public float getDivisor() {
      return this.divisor;
   }

   @Override
   public LivingMultiplier.Serializer getSerializer() {
      return (LivingMultiplier.Serializer)PSTLivingMultipliers.NUMERIC_VALUE.get();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FloatFunctionMultiplier that = (FloatFunctionMultiplier)o;
         return Float.compare(this.divisor, that.divisor) != 0 ? false : Objects.equals(this.floatFunction, that.floatFunction);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.floatFunction, this.divisor);
   }

   public void setFloatFunction(FloatFunction<?> floatFunction) {
      this.floatFunction = floatFunction;
   }

   public void setDivisor(float divisor) {
      this.divisor = divisor;
   }

   public FloatFunction<?> getFloatFunction() {
      return this.floatFunction;
   }

   public static class Serializer implements LivingMultiplier.Serializer {
      public LivingMultiplier deserialize(JsonObject json) throws JsonParseException {
         FloatFunction<?> valueProvider = SerializationHelper.deserializeValueProvider(json);
         float divisor = !json.has("divisor") ? 1.0F : json.get("divisor").getAsFloat();
         return new FloatFunctionMultiplier(valueProvider, divisor);
      }

      public void serialize(JsonObject json, LivingMultiplier multiplier) {
         if (multiplier instanceof FloatFunctionMultiplier aMultiplier) {
            SerializationHelper.serializeValueProvider(json, aMultiplier.floatFunction);
            json.addProperty("divisor", aMultiplier.divisor);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingMultiplier deserialize(CompoundTag tag) {
         FloatFunction<?> valueProvider = SerializationHelper.deserializeValueProvider(tag);
         float divisor = !tag.contains("divisor") ? 1.0F : tag.getFloat("divisor");
         return new FloatFunctionMultiplier(valueProvider, divisor);
      }

      public CompoundTag serialize(LivingMultiplier multiplier) {
         if (multiplier instanceof FloatFunctionMultiplier aMultiplier) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeValueProvider(tag, aMultiplier.floatFunction);
            tag.putFloat("divisor", aMultiplier.divisor);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingMultiplier deserialize(FriendlyByteBuf buf) {
         FloatFunction<?> valueProvider = NetworkHelper.readValueProvider(buf);
         float divisor = buf.readFloat();
         return new FloatFunctionMultiplier(valueProvider, divisor);
      }

      public void serialize(FriendlyByteBuf buf, LivingMultiplier multiplier) {
         if (multiplier instanceof FloatFunctionMultiplier aMultiplier) {
            NetworkHelper.writeValueProvider(buf, aMultiplier.floatFunction);
            buf.writeFloat(aMultiplier.divisor);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingMultiplier createDefaultInstance() {
         return new FloatFunctionMultiplier(new AttributeValueFunction(Attributes.MAX_HEALTH), 5.0F);
      }
   }
}
