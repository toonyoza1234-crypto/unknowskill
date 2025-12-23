package daripher.skilltree.skill.bonus.predicate.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTDamageConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

public record ThornsDamageCondition() implements DamageCondition {
   @Override
   public boolean met(DamageSource source) {
      return source.is(DamageTypes.THORNS);
   }

   @Override
   public DamageCondition.Serializer getSerializer() {
      return (DamageCondition.Serializer)PSTDamageConditions.THORNS.get();
   }

   @Override
   public boolean equals(Object o) {
      return this == o ? true : o != null && this.getClass() == o.getClass();
   }

   @Override
   public DamageSource createDamageSource(Player player) {
      Registry<DamageType> damageTypes = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
      Reference<DamageType> damageType = damageTypes.getHolderOrThrow(DamageTypes.THORNS);
      return new DamageSource(damageType, null, player);
   }

   @Override
   public boolean canCreateDamageSource() {
      return true;
   }

   @Override
   public int hashCode() {
      return this.getSerializer().hashCode();
   }

   public static class Serializer implements DamageCondition.Serializer {
      public DamageCondition deserialize(JsonObject json) throws JsonParseException {
         return new ThornsDamageCondition();
      }

      public void serialize(JsonObject json, DamageCondition condition) {
         if (!(condition instanceof ThornsDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      public DamageCondition deserialize(CompoundTag tag) {
         return new ThornsDamageCondition();
      }

      public CompoundTag serialize(DamageCondition condition) {
         if (!(condition instanceof ThornsDamageCondition)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public DamageCondition deserialize(FriendlyByteBuf buf) {
         return new ThornsDamageCondition();
      }

      public void serialize(FriendlyByteBuf buf, DamageCondition condition) {
         if (!(condition instanceof ThornsDamageCondition)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public DamageCondition createDefaultInstance() {
         return new ThornsDamageCondition();
      }
   }
}
