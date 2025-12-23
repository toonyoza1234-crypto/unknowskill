package daripher.skilltree.skill.bonus.predicate.enchantment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTEnchantmentConditions;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ArmorEnchantmentCondition implements EnchantmentCondition {
   @Override
   public boolean met(EnchantmentCategory category) {
      return category == EnchantmentCategory.ARMOR
         || category == EnchantmentCategory.ARMOR_CHEST
         || category == EnchantmentCategory.ARMOR_FEET
         || category == EnchantmentCategory.ARMOR_HEAD
         || category == EnchantmentCategory.ARMOR_LEGS;
   }

   @Override
   public boolean equals(Object o) {
      return this == o ? true : o != null && this.getClass() == o.getClass();
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getSerializer());
   }

   @Override
   public EnchantmentCondition.Serializer getSerializer() {
      return (EnchantmentCondition.Serializer)PSTEnchantmentConditions.ARMOR.get();
   }

   public static class Serializer implements EnchantmentCondition.Serializer {
      public EnchantmentCondition deserialize(JsonObject json) throws JsonParseException {
         return new ArmorEnchantmentCondition();
      }

      public void serialize(JsonObject json, EnchantmentCondition condition) {
         if (!(condition instanceof ArmorEnchantmentCondition)) {
            throw new IllegalArgumentException();
         }
      }

      public EnchantmentCondition deserialize(CompoundTag tag) {
         return new ArmorEnchantmentCondition();
      }

      public CompoundTag serialize(EnchantmentCondition condition) {
         if (!(condition instanceof ArmorEnchantmentCondition)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public EnchantmentCondition deserialize(FriendlyByteBuf buf) {
         return new ArmorEnchantmentCondition();
      }

      public void serialize(FriendlyByteBuf buf, EnchantmentCondition condition) {
         if (!(condition instanceof ArmorEnchantmentCondition)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public EnchantmentCondition createDefaultInstance() {
         return new ArmorEnchantmentCondition();
      }
   }
}
