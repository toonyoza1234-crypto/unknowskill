package daripher.skilltree.skill.bonus.predicate.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTDamageConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public record FallDamageCondition() implements DamageCondition {
   @Override
   public boolean met(DamageSource source) {
      return source.is(DamageTypeTags.IS_FALL);
   }

   @Override
   public DamageCondition.Serializer getSerializer() {
      return (DamageCondition.Serializer)PSTDamageConditions.FALL.get();
   }

   @Override
   public boolean equals(Object o) {
      return this == o ? true : o != null && this.getClass() == o.getClass();
   }

   @Override
   public int hashCode() {
      return this.getSerializer().hashCode();
   }

   public static class Serializer implements DamageCondition.Serializer {
      public DamageCondition deserialize(JsonObject json) throws JsonParseException {
         return new FallDamageCondition();
      }

      public void serialize(JsonObject json, DamageCondition condition) {
         if (!(condition instanceof FallDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      public DamageCondition deserialize(CompoundTag tag) {
         return new FallDamageCondition();
      }

      public CompoundTag serialize(DamageCondition condition) {
         if (!(condition instanceof FallDamageCondition)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public DamageCondition deserialize(FriendlyByteBuf buf) {
         return new FallDamageCondition();
      }

      public void serialize(FriendlyByteBuf buf, DamageCondition condition) {
         if (!(condition instanceof FallDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public DamageCondition createDefaultInstance() {
         return new FallDamageCondition();
      }
   }
}
