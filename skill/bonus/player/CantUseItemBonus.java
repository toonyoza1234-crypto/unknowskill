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

public final class CantUseItemBonus implements SkillBonus<CantUseItemBonus> {
   @Nonnull
   private ItemStackPredicate itemStackPredicate;

   public CantUseItemBonus(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.CANT_USE_ITEM.get();
   }

   public CantUseItemBonus copy() {
      return new CantUseItemBonus(this.itemStackPredicate);
   }

   public CantUseItemBonus multiply(double multiplier) {
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof CantUseItemBonus otherBonus ? Objects.equals(otherBonus.itemStackPredicate, this.itemStackPredicate) : false;
   }

   @Override
   public SkillBonus<CantUseItemBonus> merge(SkillBonus<?> other) {
      return this;
   }

   @Override
   public MutableComponent getTooltip() {
      Component itemDescription = this.itemStackPredicate.getTooltip("plural");
      return Component.translatable(this.getDescriptionId(), new Object[]{itemDescription}).withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return false;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<CantUseItemBonus> consumer) {
      editor.addLabel(0, 0, "Item Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<CantUseItemBonus> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<CantUseItemBonus> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, c -> {
         this.setItemCondition(c);
         consumer.accept(this.copy());
      });
   }

   public void setItemCondition(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   @Nonnull
   public ItemStackPredicate getItemCondition() {
      return this.itemStackPredicate;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         CantUseItemBonus that = (CantUseItemBonus)obj;
         return Objects.equals(this.itemStackPredicate, that.itemStackPredicate);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemStackPredicate);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public CantUseItemBonus deserialize(JsonObject json) throws JsonParseException {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(json);
         return new CantUseItemBonus(condition);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof CantUseItemBonus aBonus) {
            SerializationHelper.serializeItemCondition(json, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public CantUseItemBonus deserialize(CompoundTag tag) {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(tag);
         return new CantUseItemBonus(condition);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof CantUseItemBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aBonus.itemStackPredicate);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public CantUseItemBonus deserialize(FriendlyByteBuf buf) {
         return new CantUseItemBonus(NetworkHelper.readItemCondition(buf));
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof CantUseItemBonus aBonus) {
            NetworkHelper.writeItemCondition(buf, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new CantUseItemBonus(new EquipmentPredicate(EquipmentPredicate.Type.BOW));
      }
   }
}
