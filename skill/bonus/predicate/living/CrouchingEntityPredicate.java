package daripher.skilltree.skill.bonus.predicate.living;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public record CrouchingEntityPredicate() implements LivingEntityPredicate {
   public boolean test(LivingEntity living) {
      return living.isCrouching();
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      String key = this.getDescriptionId();
      Component targetDescription = Component.translatable("%s.target.%s".formatted(key, target.getName()));
      return Component.translatable(key, new Object[]{bonusTooltip, targetDescription});
   }

   @Override
   public LivingEntityPredicate.Serializer getSerializer() {
      return (LivingEntityPredicate.Serializer)PSTLivingConditions.CROUCHING.get();
   }

   @Override
   public boolean equals(Object o) {
      return this == o ? true : o != null && this.getClass() == o.getClass();
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getSerializer());
   }

   public static class Serializer implements LivingEntityPredicate.Serializer {
      public LivingEntityPredicate deserialize(JsonObject json) throws JsonParseException {
         return new CrouchingEntityPredicate();
      }

      public void serialize(JsonObject json, LivingEntityPredicate condition) {
         if (!(condition instanceof CrouchingEntityPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(CompoundTag tag) {
         return new CrouchingEntityPredicate();
      }

      public CompoundTag serialize(LivingEntityPredicate condition) {
         if (!(condition instanceof CrouchingEntityPredicate)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public LivingEntityPredicate deserialize(FriendlyByteBuf buf) {
         return new CrouchingEntityPredicate();
      }

      public void serialize(FriendlyByteBuf buf, LivingEntityPredicate condition) {
         if (!(condition instanceof CrouchingEntityPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingEntityPredicate createDefaultInstance() {
         return new CrouchingEntityPredicate();
      }
   }
}
