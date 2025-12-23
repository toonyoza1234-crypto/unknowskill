package daripher.skilltree.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface EquippedEntity {
   boolean hasItemEquipped(ItemStack var1);

   default boolean hasItemEquipped(ItemEntity entity) {
      return this.hasItemEquipped(entity.getItem());
   }
}
