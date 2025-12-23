package daripher.skilltree.skill.bonus.predicate.living;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class HasEffectEntityPredicate implements LivingEntityPredicate {
   private MobEffect effect;
   private int amplifier;

   public HasEffectEntityPredicate(@Nonnull MobEffect effect) {
      this(effect, 0);
   }

   public HasEffectEntityPredicate(@Nonnull MobEffect effect, int amplifier) {
      this.effect = effect;
      this.amplifier = amplifier;
   }

   public boolean test(LivingEntity living) {
      if (this.amplifier == 0) {
         return living.hasEffect(this.effect);
      } else {
         MobEffectInstance effect = living.getEffect(this.effect);
         return effect != null && effect.getAmplifier() >= this.amplifier;
      }
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      String key = this.getDescriptionId();
      Component targetDescription = Component.translatable("%s.target.%s".formatted(key, target.getName()));
      Component effectDescription = this.effect.getDisplayName();
      if (this.amplifier == 0) {
         return Component.translatable(key, new Object[]{bonusTooltip, targetDescription, effectDescription});
      } else {
         Component amplifierDescription = Component.translatable("potion.potency." + this.amplifier);
         Component var7 = Component.translatable("potion.withAmplifier", new Object[]{effectDescription, amplifierDescription});
         return Component.translatable(key + ".amplifier", new Object[]{bonusTooltip, targetDescription, var7});
      }
   }

   @Override
   public LivingEntityPredicate.Serializer getSerializer() {
      return (LivingEntityPredicate.Serializer)PSTLivingConditions.HAS_EFFECT.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer) {
      editor.addLabel(0, 0, "Effect", ChatFormatting.GREEN);
      editor.addLabel(150, 0, "Level", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 145, this.effect).setResponder(effect -> this.selectEffect(consumer, effect));
      editor.addNumericTextField(150, 0, 50, 14, (double)this.amplifier)
         .setNumericFilter(value -> value >= 0.0 && value == (double)value.intValue())
         .setNumericResponder(value -> this.selectAmplifier(consumer, value));
      editor.increaseHeight(19);
   }

   private void selectAmplifier(Consumer<LivingEntityPredicate> consumer, Double value) {
      this.setAmplifier(value.intValue());
      consumer.accept(this);
   }

   private void selectEffect(Consumer<LivingEntityPredicate> consumer, MobEffect effect) {
      this.setEffect(effect);
      consumer.accept(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HasEffectEntityPredicate that = (HasEffectEntityPredicate)o;
         return this.amplifier == that.amplifier && Objects.equals(this.effect, that.effect);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.effect, this.amplifier);
   }

   public void setEffect(MobEffect effect) {
      this.effect = effect;
   }

   public void setAmplifier(int amplifier) {
      this.amplifier = amplifier;
   }

   public static class Serializer implements LivingEntityPredicate.Serializer {
      public LivingEntityPredicate deserialize(JsonObject json) throws JsonParseException {
         MobEffect effect = SerializationHelper.deserializeEffect(json);
         int amplifier = !json.has("amplifier") ? 0 : json.get("amplifier").getAsInt();
         Objects.requireNonNull(effect);
         return new HasEffectEntityPredicate(effect, amplifier);
      }

      public void serialize(JsonObject json, LivingEntityPredicate condition) {
         if (condition instanceof HasEffectEntityPredicate aCondition) {
            SerializationHelper.serializeEffect(json, aCondition.effect);
            json.addProperty("amplifier", aCondition.amplifier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(CompoundTag tag) {
         MobEffect effect = SerializationHelper.deserializeEffect(tag);
         int amplifier = !tag.contains("amplifier") ? 0 : tag.getInt("amplifier");
         Objects.requireNonNull(effect);
         return new HasEffectEntityPredicate(effect, amplifier);
      }

      public CompoundTag serialize(LivingEntityPredicate condition) {
         if (condition instanceof HasEffectEntityPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeEffect(tag, aCondition.effect);
            tag.putInt("amplifier", aCondition.amplifier);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(FriendlyByteBuf buf) {
         MobEffect effect = NetworkHelper.readEffect(buf);
         Objects.requireNonNull(effect);
         return new HasEffectEntityPredicate(effect, buf.readInt());
      }

      public void serialize(FriendlyByteBuf buf, LivingEntityPredicate condition) {
         if (condition instanceof HasEffectEntityPredicate aCondition) {
            NetworkHelper.writeEffect(buf, aCondition.effect);
            buf.writeInt(aCondition.amplifier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingEntityPredicate createDefaultInstance() {
         return new HasEffectEntityPredicate(MobEffects.POISON);
      }
   }
}
