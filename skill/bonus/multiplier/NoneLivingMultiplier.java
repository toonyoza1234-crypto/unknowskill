package daripher.skilltree.skill.bonus.multiplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.skill.bonus.SkillBonus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public enum NoneLivingMultiplier implements LivingMultiplier {
   INSTANCE;

   @Override
   public float getValue(LivingEntity entity) {
      return 1.0F;
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      return bonusTooltip;
   }

   @Override
   public LivingMultiplier.Serializer getSerializer() {
      return (LivingMultiplier.Serializer)PSTLivingMultipliers.NONE.get();
   }

   public static class Serializer implements LivingMultiplier.Serializer {
      public LivingMultiplier deserialize(JsonObject json) throws JsonParseException {
         return NoneLivingMultiplier.INSTANCE;
      }

      public void serialize(JsonObject json, LivingMultiplier multiplier) {
         if (multiplier != NoneLivingMultiplier.INSTANCE) {
            throw new IllegalArgumentException();
         }
      }

      public LivingMultiplier deserialize(CompoundTag tag) {
         return NoneLivingMultiplier.INSTANCE;
      }

      public CompoundTag serialize(LivingMultiplier multiplier) {
         if (multiplier != NoneLivingMultiplier.INSTANCE) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public LivingMultiplier deserialize(FriendlyByteBuf buf) {
         return NoneLivingMultiplier.INSTANCE;
      }

      public void serialize(FriendlyByteBuf buf, LivingMultiplier multiplier) {
         if (multiplier != NoneLivingMultiplier.INSTANCE) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingMultiplier createDefaultInstance() {
         return NoneLivingMultiplier.INSTANCE;
      }
   }
}
