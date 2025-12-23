package daripher.skilltree.skill.bonus.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.living.FloatFunctionEntityPredicate;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentAmountFunction implements FloatFunction<EnchantmentAmountFunction> {
   @Nonnull
   private ItemStackPredicate itemStackPredicate;

   public EnchantmentAmountFunction(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   @Override
   public float apply(LivingEntity entity) {
      return (float)this.getEnchants(PlayerHelper.getAllEquipment(entity).filter(this.itemStackPredicate));
   }

   private int getEnchants(Stream<ItemStack> items) {
      return items.<Map>map(EnchantmentHelper::getEnchantments).map(Map::size).reduce(Integer::sum).orElse(0);
   }

   @Override
   public MutableComponent getMultiplierTooltip(SkillBonus.Target target, float divisor, Component bonusTooltip) {
      String key = "%s.multiplier.%s".formatted(this.getDescriptionId(), target.getName());
      Component itemDescription = this.itemStackPredicate.getTooltip("prepositional");
      if (divisor != 1.0F) {
         key = key + ".plural";
         return Component.translatable(key, new Object[]{bonusTooltip, this.formatNumber(divisor), itemDescription});
      } else {
         return Component.translatable(key, new Object[]{bonusTooltip, itemDescription});
      }
   }

   @Override
   public MutableComponent getConditionTooltip(SkillBonus.Target target, FloatFunctionEntityPredicate.Logic logic, Component bonusTooltip, float requiredValue) {
      String key = "%s.condition.%s".formatted(this.getDescriptionId(), target.getName());
      String enchantmentsKey = this.getDescriptionId() + ".enchantment";
      if (requiredValue != 1.0F) {
         enchantmentsKey = enchantmentsKey + ".plural";
      }

      Component enchantmentsDescription = Component.translatable(enchantmentsKey);
      Component itemDescription = this.itemStackPredicate.getTooltip();
      if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.EQUAL) {
         return Component.translatable(key + ".none", new Object[]{bonusTooltip, itemDescription});
      } else if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.MORE) {
         return Component.translatable(key + ".any", new Object[]{bonusTooltip, itemDescription});
      } else {
         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("enchantment_amount", valueDescription);
         return Component.translatable(key, new Object[]{bonusTooltip, logicDescription, enchantmentsDescription, itemDescription});
      }
   }

   @Override
   public MutableComponent getRequirementTooltip(FloatFunctionEntityPredicate.Logic logic, float requiredValue) {
      String key = "%s.requirement".formatted(this.getDescriptionId());
      String enchantmentsKey = this.getDescriptionId() + ".enchantment";
      if (requiredValue != 1.0F) {
         enchantmentsKey = enchantmentsKey + ".plural";
      }

      Component enchantmentsDescription = Component.translatable(enchantmentsKey);
      Component itemDescription = this.itemStackPredicate.getTooltip();
      if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.EQUAL) {
         return Component.translatable(key + ".none", new Object[]{itemDescription});
      } else if (requiredValue == 0.0F && logic == FloatFunctionEntityPredicate.Logic.MORE) {
         return Component.translatable(key + ".any", new Object[]{itemDescription});
      } else {
         String valueDescription = this.formatNumber(requiredValue);
         Component logicDescription = logic.getTooltip("enchantment_amount", valueDescription);
         return Component.translatable(key, new Object[]{logicDescription, enchantmentsDescription, itemDescription});
      }
   }

   @Override
   public FloatFunction.Serializer getSerializer() {
      return (FloatFunction.Serializer)PSTFloatFunctions.ENCHANTMENT_AMOUNT.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer) {
      editor.addLabel(0, 0, "Item Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, condition -> {
         this.setItemCondition(condition);
         consumer.accept(this);
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<FloatFunction<?>> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EnchantmentAmountFunction that = (EnchantmentAmountFunction)o;
         return Objects.equals(this.itemStackPredicate, that.itemStackPredicate);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemStackPredicate);
   }

   public void setItemCondition(@Nonnull ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public static class Serializer implements FloatFunction.Serializer {
      public FloatFunction<?> deserialize(JsonObject json) throws JsonParseException {
         ItemStackPredicate itemStackPredicate = SerializationHelper.deserializeItemCondition(json);
         return new EnchantmentAmountFunction(itemStackPredicate);
      }

      public void serialize(JsonObject json, FloatFunction<?> provider) {
         if (provider instanceof EnchantmentAmountFunction aProvider) {
            SerializationHelper.serializeItemCondition(json, aProvider.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(CompoundTag tag) {
         ItemStackPredicate itemStackPredicate = SerializationHelper.deserializeItemCondition(tag);
         return new EnchantmentAmountFunction(itemStackPredicate);
      }

      public CompoundTag serialize(FloatFunction<?> provider) {
         if (provider instanceof EnchantmentAmountFunction aProvider) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aProvider.itemStackPredicate);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public FloatFunction<?> deserialize(FriendlyByteBuf buf) {
         ItemStackPredicate itemStackPredicate = NetworkHelper.readItemCondition(buf);
         return new EnchantmentAmountFunction(itemStackPredicate);
      }

      public void serialize(FriendlyByteBuf buf, FloatFunction<?> provider) {
         if (provider instanceof EnchantmentAmountFunction aProvider) {
            NetworkHelper.writeItemCondition(buf, aProvider.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public FloatFunction<?> createDefaultInstance() {
         return new EnchantmentAmountFunction(new EquipmentPredicate(EquipmentPredicate.Type.WEAPON));
      }
   }
}
