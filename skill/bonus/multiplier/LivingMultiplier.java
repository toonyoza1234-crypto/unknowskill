package daripher.skilltree.skill.bonus.multiplier;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface LivingMultiplier {
   float getValue(LivingEntity var1);

   LivingMultiplier.Serializer getSerializer();

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.LIVING_MULTIPLIERS.get().getKey(this.getSerializer());
      return "skill_bonus_multiplier.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   MutableComponent getTooltip(MutableComponent var1, SkillBonus.Target var2);

   default void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<LivingMultiplier> {
      LivingMultiplier createDefaultInstance();
   }
}
