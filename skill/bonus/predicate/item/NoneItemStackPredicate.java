package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTItemConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public enum NoneItemStackPredicate implements ItemStackPredicate {
   INSTANCE;

   public boolean test(ItemStack stack) {
      return true;
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.NONE.get();
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         return NoneItemStackPredicate.INSTANCE;
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition != NoneItemStackPredicate.INSTANCE) {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         return NoneItemStackPredicate.INSTANCE;
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition != NoneItemStackPredicate.INSTANCE) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return NoneItemStackPredicate.INSTANCE;
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition != NoneItemStackPredicate.INSTANCE) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return NoneItemStackPredicate.INSTANCE;
      }
   }
}
