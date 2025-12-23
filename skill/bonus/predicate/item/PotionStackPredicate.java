package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTItemConditions;
import daripher.skilltree.network.NetworkHelper;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;

public final class PotionStackPredicate implements ItemStackPredicate {
   private PotionStackPredicate.Type type;

   public PotionStackPredicate(PotionStackPredicate.Type type) {
      this.type = type;
   }

   public boolean test(ItemStack stack) {
      if (!(stack.getItem() instanceof PotionItem)) {
         return false;
      } else {
         return switch (this.type) {
            case ANY -> true;
            case NEUTRAL -> this.hasEffects(stack, MobEffectCategory.NEUTRAL);
            case HARMFUL -> this.hasEffects(stack, MobEffectCategory.HARMFUL);
            case BENEFICIAL -> this.hasEffects(stack, MobEffectCategory.BENEFICIAL);
         };
      }
   }

   private boolean hasEffects(ItemStack stack, MobEffectCategory category) {
      return PotionUtils.getAllEffects(stack.getOrCreateTag())
         .stream()
         .<MobEffect>map(MobEffectInstance::getEffect)
         .anyMatch(effect -> effect.getCategory() == category);
   }

   @Override
   public String getDescriptionId() {
      return "%s.%s".formatted(ItemStackPredicate.super.getDescriptionId(), this.type.getName());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PotionStackPredicate that = (PotionStackPredicate)o;
         return this.type == that.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.POTIONS.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      editor.addLabel(0, 0, "Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelection(0, 0, 190, 1, this.type)
         .setNameGetter(PotionStackPredicate.Type::getFormattedName)
         .setResponder(type -> this.selectPotionType(consumer, type));
      editor.increaseHeight(29);
   }

   private void selectPotionType(Consumer<ItemStackPredicate> consumer, PotionStackPredicate.Type type) {
      this.setType(type);
      consumer.accept(this);
   }

   public void setType(PotionStackPredicate.Type type) {
      this.type = type;
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         return new PotionStackPredicate(SerializationHelper.deserializePotionType(json));
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition instanceof PotionStackPredicate aCondition) {
            SerializationHelper.serializePotionType(json, aCondition.type);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         return new PotionStackPredicate(SerializationHelper.deserializePotionType(tag));
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition instanceof PotionStackPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializePotionType(tag, aCondition.type);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return new PotionStackPredicate(NetworkHelper.readEnum(buf, PotionStackPredicate.Type.class));
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition instanceof PotionStackPredicate aCondition) {
            NetworkHelper.writeEnum(buf, aCondition.type);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new PotionStackPredicate(PotionStackPredicate.Type.ANY);
      }
   }

   public static enum Type {
      HARMFUL("harmful"),
      NEUTRAL("neutral"),
      BENEFICIAL("beneficial"),
      ANY("any");

      final String name;

      private Type(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public Component getFormattedName() {
         return Component.literal(this.getName().substring(0, 1).toUpperCase() + this.getName().substring(1));
      }

      public static PotionStackPredicate.Type byName(String name) {
         for (PotionStackPredicate.Type type : values()) {
            if (type.name.equals(name)) {
               return type;
            }
         }

         return ANY;
      }
   }
}
