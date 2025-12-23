package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.item.NoneItemStackPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class RepairEfficiencyBonus implements SkillBonus<RepairEfficiencyBonus> {
   @Nonnull
   private ItemStackPredicate itemStackPredicate;
   private float multiplier;

   public RepairEfficiencyBonus(@Nonnull ItemStackPredicate itemStackPredicate, float multiplier) {
      this.itemStackPredicate = itemStackPredicate;
      this.multiplier = multiplier;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.REPAIR_EFFICIENCY.get();
   }

   public RepairEfficiencyBonus copy() {
      return new RepairEfficiencyBonus(this.itemStackPredicate, this.multiplier);
   }

   public RepairEfficiencyBonus multiply(double multiplier) {
      return new RepairEfficiencyBonus(this.itemStackPredicate, (float)(multiplier * multiplier));
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof RepairEfficiencyBonus otherBonus ? Objects.equals(otherBonus.itemStackPredicate, this.itemStackPredicate) : false;
   }

   @Override
   public SkillBonus<RepairEfficiencyBonus> merge(SkillBonus<?> other) {
      if (other instanceof RepairEfficiencyBonus otherBonus) {
         return new RepairEfficiencyBonus(this.itemStackPredicate, otherBonus.multiplier + this.multiplier);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component itemDescription = this.itemStackPredicate.getTooltip("plural.type");
      Operation operation = Operation.MULTIPLY_BASE;
      Component bonusDescription = Component.translatable(this.getDescriptionId() + ".bonus");
      bonusDescription = TooltipHelper.getSkillBonusTooltip(bonusDescription, (double)this.multiplier, operation)
         .withStyle(TooltipHelper.getSkillBonusSecondStyle(this.isPositive()));
      return Component.translatable(this.getDescriptionId(), new Object[]{itemDescription, bonusDescription})
         .withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.multiplier > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<RepairEfficiencyBonus> consumer) {
      editor.addLabel(0, 0, "Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.multiplier).setNumericResponder(value -> this.selectMultiplier(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Item Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<RepairEfficiencyBonus> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, condition -> {
         this.setItemCondition(condition);
         consumer.accept(this.copy());
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<RepairEfficiencyBonus> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectMultiplier(Consumer<RepairEfficiencyBonus> consumer, Double value) {
      this.setMultiplier(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setItemCondition(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   @Nonnull
   public ItemStackPredicate getItemCondition() {
      return this.itemStackPredicate;
   }

   public float getMultiplier() {
      return this.multiplier;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         RepairEfficiencyBonus that = (RepairEfficiencyBonus)obj;
         return !Objects.equals(this.itemStackPredicate, that.itemStackPredicate) ? false : this.multiplier == that.multiplier;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemStackPredicate, this.multiplier);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public RepairEfficiencyBonus deserialize(JsonObject json) throws JsonParseException {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(json);
         float multiplier = SerializationHelper.getElement(json, "multiplier").getAsFloat();
         return new RepairEfficiencyBonus(condition, multiplier);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof RepairEfficiencyBonus aBonus) {
            SerializationHelper.serializeItemCondition(json, aBonus.itemStackPredicate);
            json.addProperty("multiplier", aBonus.multiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public RepairEfficiencyBonus deserialize(CompoundTag tag) {
         ItemStackPredicate condition = SerializationHelper.deserializeItemCondition(tag);
         float multiplier = tag.getFloat("multiplier");
         return new RepairEfficiencyBonus(condition, multiplier);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof RepairEfficiencyBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aBonus.itemStackPredicate);
            tag.putFloat("multiplier", aBonus.multiplier);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public RepairEfficiencyBonus deserialize(FriendlyByteBuf buf) {
         return new RepairEfficiencyBonus(NetworkHelper.readItemCondition(buf), buf.readFloat());
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof RepairEfficiencyBonus aBonus) {
            NetworkHelper.writeItemCondition(buf, aBonus.itemStackPredicate);
            buf.writeFloat(aBonus.multiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new RepairEfficiencyBonus(NoneItemStackPredicate.INSTANCE, 0.1F);
      }
   }
}
