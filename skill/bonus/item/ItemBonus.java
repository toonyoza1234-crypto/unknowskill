package daripher.skilltree.skill.bonus.item;

import daripher.skilltree.init.PSTRegistries;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface ItemBonus<T extends ItemBonus<T>> {
   boolean canMerge(ItemBonus<?> var1);

   default boolean sameBonus(ItemBonus<?> other) {
      return this.canMerge(other);
   }

   T merge(ItemBonus<?> var1);

   T copy();

   T multiply(double var1);

   ItemBonus.Serializer getSerializer();

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.ITEM_BONUSES.get().getKey(this.getSerializer());
      return "item_bonus.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   void addTooltip(Consumer<MutableComponent> var1);

   boolean isPositive();

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<ItemBonus<?>> {
      ItemBonus<?> createDefaultInstance();
   }
}
