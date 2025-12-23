package daripher.skilltree.skill.bonus.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public class HealthLevelFunction implements FloatFunction<HealthLevelFunction> {
   private boolean percentage;
   private boolean missing;

   public HealthLevelFunction(boolean percentage, boolean missing) {
      this.percentage = percentage;
      this.missing = missing;
   }

   @Override
   public float apply(LivingEntity entity) {
      float value = entity.getHealth();
      if (this.missing) {
         value = entity.getMaxHealth() - value;
      }

      if (this.percentage) {
         value /= entity.getMaxHealth();
      }

      return value;
   }

   @Override
   public MutableComponent getMultiplierTooltip(SkillBonus.Target target, float divisor, Component bonusTooltip) {
      String key = "%s.multiplier.%s".formatted(this.getDescriptionId(), target.getName());
      String pointsKey = this.getDescriptionId();
      pointsKey = pointsKey + (this.percentage ? ".percentage" : ".point");
      if (divisor != 1.0F) {
         key = key + ".plural";
         pointsKey = pointsKey + ".plural";
      }

      Component pointsDescription = Component.translatable(pointsKey);
      if (this.missing) {
         key = key + ".missing";
      }

      return divisor != 1.0F
         ? Component.translatable(key, new Object[]{bonusTooltip, this.formatNumber(divisor), pointsDescription})
         : Component.translatable(key, new Object[]{bonusTooltip, pointsDescription});
   }

   @Override
   public MutableComponent getConditionTooltip(SkillBonus.Target target, FloatFunctionEntityPredicate.Logic logic, Component bonusTooltip, float requiredValue) {
      String key = "%s.condition.%s".formatted(this.getDescriptionId(), target.getName());
      String pointsKey = this.getDescriptionId();
      pointsKey = pointsKey + (this.percentage ? ".percentage" : ".point");
      if (requiredValue != 1.0F) {
         pointsKey = pointsKey + ".plural";
      }

      Component pointsDescription = Component.translatable(pointsKey);
      if (logic == FloatFunctionEntityPredicate.Logic.EQUAL && this.percentage && requiredValue == 1.0F) {
         return Component.translatable(key + ".full", new Object[]{bonusTooltip});
      } else if (logic == FloatFunctionEntityPredicate.Logic.LESS && this.percentage && requiredValue == 1.0F) {
         return Component.translatable(key + ".not_full", new Object[]{bonusTooltip});
      } else {
         if (this.missing) {
            key = key + ".missing";
         }

         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("health_level", valueDescription);
         return Component.translatable(key, new Object[]{bonusTooltip, logicDescription, pointsDescription});
      }
   }

   @Override
   public MutableComponent getRequirementTooltip(FloatFunctionEntityPredicate.Logic logic, float requiredValue) {
      String key = "%s.requirement".formatted(this.getDescriptionId());
      String pointsKey = this.getDescriptionId() + ".point";
      if (requiredValue != 1.0F) {
         pointsKey = pointsKey + ".plural";
      }

      Component pointsDescription = Component.translatable(pointsKey);
      if (logic == FloatFunctionEntityPredicate.Logic.EQUAL && this.percentage && requiredValue == 1.0F) {
         return Component.translatable(key + ".full");
      } else if (logic == FloatFunctionEntityPredicate.Logic.LESS && this.percentage && requiredValue == 1.0F) {
         return Component.translatable(key + ".not_full");
      } else {
         if (this.missing) {
            key = key + ".missing";
         }

         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("health_level", valueDescription);
         return Component.translatable(key, new Object[]{logicDescription, pointsDescription});
      }
   }

   @Override
   public String formatNumber(float number) {
      return this.percentage ? FloatFunction.super.formatNumber(number * 100.0F) + "%" : FloatFunction.super.formatNumber(number);
   }

   @Override
   public FloatFunction.Serializer getSerializer() {
      return (FloatFunction.Serializer)PSTFloatFunctions.HEALTH_LEVEL.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer) {
      editor.addLabel(0, 0, "Missing", ChatFormatting.GREEN);
      editor.addLabel(55, 0, "Percentage", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addCheckBox(0, 0, this.missing).setResponder(v -> this.selectMissingMode(consumer, v));
      editor.addCheckBox(55, 0, this.percentage).setResponder(v -> this.selectPercentageMode(consumer, v));
      editor.increaseHeight(19);
   }

   private void selectMissingMode(Consumer<FloatFunction<?>> consumer, boolean missing) {
      this.setMissing(missing);
      consumer.accept(this);
   }

   private void selectPercentageMode(Consumer<FloatFunction<?>> consumer, boolean percentage) {
      this.setPercentage(percentage);
      consumer.accept(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HealthLevelFunction that = (HealthLevelFunction)o;
         return this.percentage == that.percentage && this.missing == that.missing;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.percentage, this.missing);
   }

   public void setMissing(boolean missing) {
      this.missing = missing;
   }

   public void setPercentage(boolean percentage) {
      this.percentage = percentage;
   }

   public static class Serializer implements FloatFunction.Serializer {
      public FloatFunction<?> deserialize(JsonObject json) throws JsonParseException {
         boolean percentage = json.get("percentage").getAsBoolean();
         boolean missing = json.get("missing").getAsBoolean();
         return new HealthLevelFunction(percentage, missing);
      }

      public void serialize(JsonObject json, FloatFunction<?> provider) {
         if (provider instanceof HealthLevelFunction aProvider) {
            json.addProperty("percentage", aProvider.percentage);
            json.addProperty("missing", aProvider.missing);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(CompoundTag tag) {
         boolean percentage = tag.getBoolean("percentage");
         boolean missing = tag.getBoolean("missing");
         return new HealthLevelFunction(percentage, missing);
      }

      public CompoundTag serialize(FloatFunction<?> provider) {
         if (provider instanceof HealthLevelFunction aProvider) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("percentage", aProvider.percentage);
            tag.putBoolean("missing", aProvider.missing);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(FriendlyByteBuf buf) {
         boolean percentage = buf.readBoolean();
         boolean missing = buf.readBoolean();
         return new HealthLevelFunction(percentage, missing);
      }

      public void serialize(FriendlyByteBuf buf, FloatFunction<?> provider) {
         if (provider instanceof HealthLevelFunction aProvider) {
            buf.writeBoolean(aProvider.percentage);
            buf.writeBoolean(aProvider.missing);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public FloatFunction<?> createDefaultInstance() {
         return new HealthLevelFunction(false, false);
      }
   }
}
