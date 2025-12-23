package daripher.skilltree.skill.bonus.event;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface SkillEventListener {
   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.EVENT_LISTENERS.get().getKey(this.getSerializer());
      return "event_listener.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   default MutableComponent getTooltip(Component bonusTooltip) {
      return Component.translatable(this.getDescriptionId(), new Object[]{bonusTooltip});
   }

   SkillBonus.Target getTarget();

   SkillEventListener.Serializer getSerializer();

   void addEditorWidgets(SkillTreeEditor var1, Consumer<SkillEventListener> var2);

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<SkillEventListener> {
      SkillEventListener createDefaultInstance();
   }
}
