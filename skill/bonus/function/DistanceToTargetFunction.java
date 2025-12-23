package daripher.skilltree.skill.bonus.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DistanceToTargetFunction implements FloatFunction<DistanceToTargetFunction> {
   @Override
   public float apply(LivingEntity entity) {
      if (entity instanceof Player player) {
         int lastTargetId = player.getPersistentData().getInt("LastAttackTarget");
         Entity target = entity.level().getEntity(lastTargetId);
         return target == null ? 0.0F : target.distanceTo(entity);
      } else {
         return 0.0F;
      }
   }

   @Override
   public MutableComponent getMultiplierTooltip(SkillBonus.Target target, float divisor, Component bonusTooltip) {
      String key = "%s.multiplier.%s".formatted(this.getDescriptionId(), target.getName());
      if (divisor != 1.0F) {
         key = key + ".plural";
         return Component.translatable(key, new Object[]{bonusTooltip, this.formatNumber(divisor)});
      } else {
         return Component.translatable(key, new Object[]{bonusTooltip});
      }
   }

   @Override
   public MutableComponent getConditionTooltip(SkillBonus.Target target, FloatFunctionEntityPredicate.Logic logic, Component bonusTooltip, float requiredValue) {
      String key = "%s.condition.%s".formatted(this.getDescriptionId(), target.getName());
      String valueDescription = this.formatNumber(requiredValue);
      Component logicDescription = logic.getTooltip("distance_to_target", valueDescription);
      return Component.translatable(key, new Object[]{bonusTooltip, logicDescription});
   }

   @Override
   public MutableComponent getRequirementTooltip(FloatFunctionEntityPredicate.Logic logic, float requiredValue) {
      return Component.literal("Unsupported").withStyle(ChatFormatting.RED);
   }

   @Override
   public FloatFunction.Serializer getSerializer() {
      return (FloatFunction.Serializer)PSTFloatFunctions.DISTANCE_TO_TARGET.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer) {
   }

   public static class Serializer implements FloatFunction.Serializer {
      public FloatFunction<?> deserialize(JsonObject json) throws JsonParseException {
         return new DistanceToTargetFunction();
      }

      public void serialize(JsonObject json, FloatFunction<?> provider) {
         if (!(provider instanceof DistanceToTargetFunction)) {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(CompoundTag tag) {
         return new DistanceToTargetFunction();
      }

      public CompoundTag serialize(FloatFunction<?> provider) {
         if (!(provider instanceof DistanceToTargetFunction)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public FloatFunction<?> deserialize(FriendlyByteBuf buf) {
         return new DistanceToTargetFunction();
      }

      public void serialize(FriendlyByteBuf buf, FloatFunction<?> provider) {
         if (!(provider instanceof DistanceToTargetFunction)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public FloatFunction<?> createDefaultInstance() {
         return new DistanceToTargetFunction();
      }
   }
}
