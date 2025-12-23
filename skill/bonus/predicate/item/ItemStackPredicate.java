package daripher.skilltree.skill.bonus.predicate.item;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTRegistries;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ItemStackPredicate extends Predicate<ItemStack> {
   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.ITEM_CONDITIONS.get().getKey(this.getSerializer());
      return "item_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   default Component getTooltip() {
      return Component.translatable(this.getDescriptionId());
   }

   default Component getTooltip(String type) {
      return TooltipHelper.getOptionalTooltip(this.getDescriptionId(), type);
   }

   ItemStackPredicate.Serializer getSerializer();

   default void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<ItemStackPredicate> {
      ItemStackPredicate createDefaultInstance();
   }
}
