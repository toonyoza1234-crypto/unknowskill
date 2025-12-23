package daripher.skilltree.skill.bonus.predicate.living;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityPredicate extends Predicate<LivingEntity> {
   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.LIVING_CONDITIONS.get().getKey(this.getSerializer());
      if (!<unrepresentable>.$assertionsDisabled && id == null) {
         throw new AssertionError();
      } else {
         return "living_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
      }
   }

   MutableComponent getTooltip(MutableComponent var1, SkillBonus.Target var2);

   LivingEntityPredicate.Serializer getSerializer();

   default void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer) {
   }

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<LivingEntityPredicate> {
      LivingEntityPredicate createDefaultInstance();
   }
}
