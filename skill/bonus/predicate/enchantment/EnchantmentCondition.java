package daripher.skilltree.skill.bonus.predicate.enchantment;

import daripher.skilltree.init.PSTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public interface EnchantmentCondition {
   boolean met(EnchantmentCategory var1);

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.ENCHANTMENT_CONDITIONS.get().getKey(this.getSerializer());
      return "enchantment_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   EnchantmentCondition.Serializer getSerializer();

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<EnchantmentCondition> {
      EnchantmentCondition createDefaultInstance();
   }
}
