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
import net.minecraft.world.entity.player.Player;

public record FishingEntityPredicate() implements LivingEntityPredicate {
   public boolean test(LivingEntity living) {
      if (living instanceof Player player && player.fishing != null) {
         return true;
      }

      return false;
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      String key = this.getDescriptionId();
      MutableComponent targetDescription = Component.translatable("%s.target.%s".formatted(key, target.getName()));
      return Component.translatable(key, new Object[]{bonusTooltip, targetDescription});
   }

   @Override
   public LivingEntityPredicate.Serializer getSerializer() {
      return (LivingEntityPredicate.Serializer)PSTLivingConditions.FISHING.get();
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
         return new FishingEntityPredicate();
      }

      public void serialize(JsonObject json, LivingEntityPredicate condition) {
         if (!(condition instanceof FishingEntityPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(CompoundTag tag) {
         return new FishingEntityPredicate();
      }

      public CompoundTag serialize(LivingEntityPredicate condition) {
         if (!(condition instanceof FishingEntityPredicate)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public LivingEntityPredicate deserialize(FriendlyByteBuf buf) {
         return new FishingEntityPredicate();
      }

      public void serialize(FriendlyByteBuf buf, LivingEntityPredicate condition) {
         if (!(condition instanceof FishingEntityPredicate)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingEntityPredicate createDefaultInstance() {
         return new FishingEntityPredicate();
      }
   }
}
