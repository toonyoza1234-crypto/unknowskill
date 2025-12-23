package daripher.skilltree.skill.bonus.function;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface FloatFunction<T> {
   float apply(LivingEntity var1);

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.FLOAT_FUNCTIONS.get().getKey(this.getSerializer());
      if (!<unrepresentable>.$assertionsDisabled && id == null) {
         throw new AssertionError();
      } else {
         return "value_provider.%s.%s".formatted(id.getNamespace(), id.getPath());
      }
   }

   default String formatNumber(float number) {
      return TooltipHelper.formatNumber((double)number);
   }

   MutableComponent getMultiplierTooltip(SkillBonus.Target var1, float var2, Component var3);

   MutableComponent getConditionTooltip(SkillBonus.Target var1, FloatFunctionEntityPredicate.Logic var2, Component var3, float var4);

   MutableComponent getRequirementTooltip(FloatFunctionEntityPredicate.Logic var1, float var2);

   FloatFunction.Serializer getSerializer();

   default T createDefaultInstance() {
      return (T)this.getSerializer().createDefaultInstance();
   }

   void addEditorWidgets(SkillTreeEditor var1, Consumer<FloatFunction<?>> var2);

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<FloatFunction<?>> {
      FloatFunction<?> createDefaultInstance();
   }
}
