package daripher.skilltree.skill.requirement;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface SkillRequirement<T extends SkillRequirement<T>> extends Predicate<Player> {
   MutableComponent getTooltip();

   void addEditorWidgets(SkillTreeEditor var1, Consumer<T> var2);

   SkillRequirement.Serializer getSerializer();

   T copy();

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.SKILL_REQUIREMENTS.get().getKey(this.getSerializer());
      return "skill_requirements.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<SkillRequirement<?>> {
      SkillRequirement<?> createDefaultInstance();
   }
}
