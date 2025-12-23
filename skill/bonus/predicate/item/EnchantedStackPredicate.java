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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantedStackPredicate implements ItemStackPredicate {
   private ItemStackPredicate itemStackPredicate;

   public EnchantedStackPredicate(ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public boolean test(ItemStack stack) {
      return !EnchantmentHelper.getEnchantments(stack).isEmpty() && this.itemStackPredicate.test(stack);
   }

   @Override
   public Component getTooltip() {
      return Component.translatable(this.getDescriptionId(), new Object[]{this.itemStackPredicate.getTooltip("type")});
   }

   @Override
   public Component getTooltip(String type) {
      return Component.translatable(this.getDescriptionId(), new Object[]{this.itemStackPredicate.getTooltip(type + ".type")});
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EnchantedStackPredicate that = (EnchantedStackPredicate)o;
         return this.itemStackPredicate.equals(that.itemStackPredicate);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemStackPredicate);
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.ENCHANTED.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      editor.addLabel(0, 0, "Inner Item Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, condition -> {
         this.setItemCondition(condition);
         consumer.accept(this);
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   public void setItemCondition(ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         return new EnchantedStackPredicate(SerializationHelper.deserializeItemCondition(json));
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition instanceof EnchantedStackPredicate aCondition) {
            SerializationHelper.serializeItemCondition(json, aCondition.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         return new EnchantedStackPredicate(SerializationHelper.deserializeItemCondition(tag));
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition instanceof EnchantedStackPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aCondition.itemStackPredicate);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return new EnchantedStackPredicate(NetworkHelper.readItemCondition(buf));
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition instanceof EnchantedStackPredicate aCondition) {
            NetworkHelper.writeItemCondition(buf, aCondition.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new EnchantedStackPredicate(new ItemTagPredicate(ItemTags.SWORDS.location()));
      }
   }
}
