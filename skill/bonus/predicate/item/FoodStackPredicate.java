package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTItemConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record FoodStackPredicate() implements ItemStackPredicate {
   public boolean test(ItemStack stack) {
      return stack.getFoodProperties(null) != null;
   }

   @Override
   public boolean equals(Object obj) {
      return obj.getClass() == this.getClass();
   }

   @Override
   public int hashCode() {
      return this.getSerializer().hashCode();
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.FOOD.get();
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         return new FoodStackPredicate();
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (!(condition instanceof FoodStackPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         return new FoodStackPredicate();
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (!(condition instanceof FoodStackPredicate)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return new FoodStackPredicate();
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (!(condition instanceof FoodStackPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new FoodStackPredicate();
      }
   }
}
