package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class MoreItemBonusesBonus implements SkillBonus<MoreItemBonusesBonus> {
   @Nonnull
   private ItemStackPredicate itemStackPredicate;
   private int amount;

   public MoreItemBonusesBonus(@Nonnull ItemStackPredicate itemStackPredicate, int amount) {
      this.itemStackPredicate = itemStackPredicate;
      this.amount = amount;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.MORE_ITEM_BONUSES.get();
   }

   public MoreItemBonusesBonus copy() {
      return new MoreItemBonusesBonus(this.itemStackPredicate, this.amount);
   }

   public MoreItemBonusesBonus multiply(double multiplier) {
      return new MoreItemBonusesBonus(this.itemStackPredicate, (int)((double)this.amount * multiplier));
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof MoreItemBonusesBonus otherBonus ? Objects.equals(otherBonus.itemStackPredicate, this.itemStackPredicate) : false;
   }

   @Override
   public SkillBonus<MoreItemBonusesBonus> merge(SkillBonus<?> other) {
      if (other instanceof MoreItemBonusesBonus otherBonus) {
         return new MoreItemBonusesBonus(this.itemStackPredicate, otherBonus.amount + this.amount);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component itemDescription = this.itemStackPredicate.getTooltip("plural");
      MutableComponent bonusDescription;
      if (this.amount == 1) {
         bonusDescription = Component.translatable(this.getDescriptionId() + ".one", new Object[]{itemDescription});
      } else {
         bonusDescription = Component.translatable(this.getDescriptionId(), new Object[]{itemDescription, this.amount});
      }

      return bonusDescription.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.amount > 0;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<MoreItemBonusesBonus> consumer) {
      editor.addLabel(0, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.amount).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Item Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<MoreItemBonusesBonus> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, condition -> {
         this.setItemCondition(condition);
         consumer.accept(this.copy());
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<MoreItemBonusesBonus> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectAmount(Consumer<MoreItemBonusesBonus> consumer, Double value) {
      this.setAmount(value.intValue());
      consumer.accept(this.copy());
   }

   public void setItemCondition(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Nonnull
   public ItemStackPredicate getItemCondition() {
      return this.itemStackPredicate;
   }

   public int getAmount() {
      return this.amount;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         MoreItemBonusesBonus that = (MoreItemBonusesBonus)obj;
         return !Objects.equals(this.itemStackPredicate, that.itemStackPredicate) ? false : this.amount == that.amount;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemStackPredicate, this.amount);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public MoreItemBonusesBonus deserialize(JsonObject json) throws JsonParseException {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(json);
         int amount = SerializationHelper.getElement(json, "amount").getAsInt();
         return new MoreItemBonusesBonus(condition, amount);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof MoreItemBonusesBonus aBonus) {
            SerializationHelper.serializeItemCondition(json, aBonus.itemStackPredicate);
            json.addProperty("amount", aBonus.amount);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public MoreItemBonusesBonus deserialize(CompoundTag tag) {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(tag);
         int amount = tag.getInt("amount");
         return new MoreItemBonusesBonus(condition, amount);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof MoreItemBonusesBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aBonus.itemStackPredicate);
            tag.putInt("amount", aBonus.amount);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public MoreItemBonusesBonus deserialize(FriendlyByteBuf buf) {
         return new MoreItemBonusesBonus(NetworkHelper.readItemCondition(buf), buf.readInt());
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof MoreItemBonusesBonus aBonus) {
            NetworkHelper.writeItemCondition(buf, aBonus.itemStackPredicate);
            buf.writeInt(aBonus.amount);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new MoreItemBonusesBonus(new EquipmentPredicate(EquipmentPredicate.Type.SHIELD), 1);
      }
   }
}
