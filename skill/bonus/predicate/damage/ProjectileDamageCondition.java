package daripher.skilltree.skill.bonus.predicate.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTDamageConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileDamageCondition() implements DamageCondition {
   @Override
   public boolean met(DamageSource source) {
      return source.getDirectEntity() instanceof Projectile;
   }

   @Override
   public DamageCondition.Serializer getSerializer() {
      return (DamageCondition.Serializer)PSTDamageConditions.PROJECTILE.get();
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
         return new ProjectileDamageCondition();
      }

      public void serialize(JsonObject json, DamageCondition condition) {
         if (!(condition instanceof ProjectileDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      public DamageCondition deserialize(CompoundTag tag) {
         return new ProjectileDamageCondition();
      }

      public CompoundTag serialize(DamageCondition condition) {
         if (!(condition instanceof ProjectileDamageCondition)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public DamageCondition deserialize(FriendlyByteBuf buf) {
         return new ProjectileDamageCondition();
      }

      public void serialize(FriendlyByteBuf buf, DamageCondition condition) {
         if (!(condition instanceof ProjectileDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public DamageCondition createDefaultInstance() {
         return new ProjectileDamageCondition();
      }
   }
}
