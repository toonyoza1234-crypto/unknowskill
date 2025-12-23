package daripher.skilltree.skill.bonus.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.effect.EffectType;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class EffectAmountFunction implements FloatFunction<EffectAmountFunction> {
   private EffectType effectType;

   public EffectAmountFunction(EffectType effectType) {
      this.effectType = effectType;
   }

   @Override
   public float apply(LivingEntity entity) {
      List<MobEffect> effects = entity.getActiveEffects().stream().<MobEffect>map(MobEffectInstance::getEffect).toList();

      return switch (this.effectType) {
         case ANY -> (float)effects.size();
         case NEUTRAL -> (float)effects.stream().filter(e -> e.getCategory() == MobEffectCategory.NEUTRAL).count();
         case HARMFUL -> (float)effects.stream().filter(e -> e.getCategory() == MobEffectCategory.HARMFUL).count();
         case BENEFICIAL -> (float)effects.stream().filter(e -> e.getCategory() == MobEffectCategory.BENEFICIAL).count();
      };
   }

   @Override
   public MutableComponent getMultiplierTooltip(SkillBonus.Target target, float divisor, Component bonusTooltip) {
      String key = "%s.multiplier.%s".formatted(this.getDescriptionId(), target.getName());
      String effectTypeKey = this.effectType.getDescriptionId();
      if (divisor != 1.0F) {
         effectTypeKey = effectTypeKey + ".plural";
         key = key + ".plural";
         Component effectDescription = Component.translatable(effectTypeKey);
         return Component.translatable(key, new Object[]{bonusTooltip, this.formatNumber(divisor), effectDescription});
      } else {
         Component effectDescription = Component.translatable(effectTypeKey);
         return Component.translatable(key, new Object[]{bonusTooltip, effectDescription});
      }
   }

   @Override
   public MutableComponent getConditionTooltip(SkillBonus.Target target, FloatFunctionEntityPredicate.Logic logic, Component bonusTooltip, float requiredValue) {
      String key = "%s.condition.%s".formatted(this.getDescriptionId(), target.getName());
      String effectTypeKey = this.effectType.getDescriptionId();
      if ((requiredValue != 0.0F || logic != FloatFunctionEntityPredicate.Logic.MORE) && requiredValue != 1.0F) {
         effectTypeKey = effectTypeKey + ".plural";
      }

      Component effectDescription = Component.translatable(effectTypeKey);
      if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.EQUAL) {
         return Component.translatable(key + ".none", new Object[]{bonusTooltip, effectDescription});
      } else if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.MORE) {
         return Component.translatable(key + ".any", new Object[]{bonusTooltip, effectDescription});
      } else {
         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("effect_amount", valueDescription);
         return Component.translatable(key, new Object[]{bonusTooltip, logicDescription, effectDescription});
      }
   }

   @Override
   public MutableComponent getRequirementTooltip(FloatFunctionEntityPredicate.Logic logic, float requiredValue) {
      String key = "%s.requirement".formatted(this.getDescriptionId());
      String effectTypeKey = this.effectType.getDescriptionId();
      if ((requiredValue != 0.0F || logic != FloatFunctionEntityPredicate.Logic.MORE) && requiredValue != 1.0F) {
         effectTypeKey = effectTypeKey + ".plural";
      }

      Component effectDescription = Component.translatable(effectTypeKey);
      if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.EQUAL) {
         return Component.translatable(key + ".none", new Object[]{effectDescription});
      } else if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.MORE) {
         return Component.translatable(key + ".any", new Object[]{effectDescription});
      } else {
         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("effect_amount", valueDescription);
         return Component.translatable(key, new Object[]{logicDescription, effectDescription});
      }
   }

   @Override
   public FloatFunction.Serializer getSerializer() {
      return (FloatFunction.Serializer)PSTFloatFunctions.EFFECT_AMOUNT.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer) {
      editor.addLabel(0, 0, "Effect Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.effectType)
         .setElementNameGetter(effectType -> Component.literal(effectType.name()))
         .setResponder(effectType -> this.selectEffectType(consumer, effectType));
      editor.increaseHeight(19);
   }

   private void selectEffectType(Consumer<FloatFunction<?>> consumer, EffectType type) {
      this.setEffectType(type);
      consumer.accept(this);
   }

   public void setEffectType(EffectType type) {
      this.effectType = type;
   }

   public static class Serializer implements FloatFunction.Serializer {
      public FloatFunction<?> deserialize(JsonObject json) throws JsonParseException {
         EffectType type = EffectType.fromName(json.get("effect_type").getAsString());
         return new EffectAmountFunction(type);
      }

      public void serialize(JsonObject json, FloatFunction<?> provider) {
         if (provider instanceof EffectAmountFunction aProvider) {
            json.addProperty("effect_type", aProvider.effectType.getName());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(CompoundTag tag) {
         EffectType type = EffectType.fromName(tag.getString("effect_type"));
         return new EffectAmountFunction(type);
      }

      public CompoundTag serialize(FloatFunction<?> provider) {
         if (provider instanceof EffectAmountFunction aProvider) {
            CompoundTag tag = new CompoundTag();
            tag.putString("effect_type", aProvider.effectType.getName());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(FriendlyByteBuf buf) {
         EffectType type = EffectType.values()[buf.readInt()];
         return new EffectAmountFunction(type);
      }

      public void serialize(FriendlyByteBuf buf, FloatFunction<?> provider) {
         if (provider instanceof EffectAmountFunction aProvider) {
            buf.writeInt(aProvider.effectType.ordinal());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public FloatFunction<?> createDefaultInstance() {
         return new EffectAmountFunction(EffectType.ANY);
      }
   }
}
