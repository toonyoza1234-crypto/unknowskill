package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.item.NoneItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ItemUseMovementSpeedBonus implements SkillBonus<ItemUseMovementSpeedBonus> {
   private float multiplier;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private ItemStackPredicate itemStackPredicate = NoneItemStackPredicate.INSTANCE;

   public ItemUseMovementSpeedBonus(float multiplier) {
      this.multiplier = multiplier;
   }

   public float getMultiplier(Player player, ItemStack itemStack) {
      if (!this.playerCondition.test(player)) {
         return 0.0F;
      } else {
         return !this.itemStackPredicate.test(itemStack) ? 0.0F : this.multiplier * this.playerMultiplier.getValue(player);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.ITEM_USE_MOVEMENT_SPEED.get();
   }

   public ItemUseMovementSpeedBonus copy() {
      ItemUseMovementSpeedBonus bonus = new ItemUseMovementSpeedBonus(this.multiplier);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      bonus.itemStackPredicate = this.itemStackPredicate;
      return bonus;
   }

   public ItemUseMovementSpeedBonus multiply(double multiplier) {
      this.multiplier *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof ItemUseMovementSpeedBonus otherBonus) {
         if (!Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier)) {
            return false;
         } else {
            return !Objects.equals(otherBonus.itemStackPredicate, this.itemStackPredicate)
               ? false
               : Objects.equals(otherBonus.playerCondition, this.playerCondition);
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<ItemUseMovementSpeedBonus> merge(SkillBonus<?> other) {
      if (other instanceof ItemUseMovementSpeedBonus otherBonus) {
         float mergedMultiplier = otherBonus.multiplier + this.multiplier;
         ItemUseMovementSpeedBonus mergedBonus = new ItemUseMovementSpeedBonus(mergedMultiplier);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         mergedBonus.itemStackPredicate = this.itemStackPredicate;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String keySuffix = this.isPositive() ? "positive" : "negative";
      String multiplierString = TooltipHelper.formatNumber((double)(Mth.abs(this.multiplier) * 100.0F));
      String descriptionKey = this.getDescriptionId() + "." + keySuffix;
      Component itemConditionTooltip = this.itemStackPredicate.getTooltip("plural");
      MutableComponent tooltip;
      if (this.isPositive() && this.multiplier == -1.0F) {
         descriptionKey = this.getDescriptionId() + ".remove";
         tooltip = Component.translatable(descriptionKey, new Object[]{itemConditionTooltip});
      } else {
         tooltip = Component.translatable(descriptionKey, new Object[]{itemConditionTooltip, multiplierString});
      }

      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.multiplier < 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<ItemUseMovementSpeedBonus> consumer) {
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
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void selectMultiplier(Consumer<ItemUseMovementSpeedBonus> consumer, Double value) {
      this.setMultiplier(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, c -> {
         this.setItemCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<ItemUseMovementSpeedBonus> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setItemCondition(ItemStackPredicate condition) {
      this.itemStackPredicate = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public ItemUseMovementSpeedBonus deserialize(JsonObject json) throws JsonParseException {
         float multiplier = SerializationHelper.getElement(json, "multiplier").getAsFloat();
         ItemUseMovementSpeedBonus bonus = new ItemUseMovementSpeedBonus(multiplier);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         bonus.itemStackPredicate = SerializationHelper.deserializeItemCondition(json);
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof ItemUseMovementSpeedBonus aBonus) {
            json.addProperty("multiplier", aBonus.multiplier);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeItemCondition(json, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemUseMovementSpeedBonus deserialize(CompoundTag tag) {
         float multiplier = tag.getFloat("multiplier");
         ItemUseMovementSpeedBonus bonus = new ItemUseMovementSpeedBonus(multiplier);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         bonus.itemStackPredicate = SerializationHelper.deserializeItemCondition(tag);
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof ItemUseMovementSpeedBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("multiplier", aBonus.multiplier);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeItemCondition(tag, aBonus.itemStackPredicate);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemUseMovementSpeedBonus deserialize(FriendlyByteBuf buf) {
         float multiplier = buf.readFloat();
         ItemUseMovementSpeedBonus bonus = new ItemUseMovementSpeedBonus(multiplier);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         bonus.itemStackPredicate = NetworkHelper.readItemCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof ItemUseMovementSpeedBonus aBonus) {
            buf.writeFloat(aBonus.multiplier);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            NetworkHelper.writeItemCondition(buf, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new ItemUseMovementSpeedBonus(-0.1F).setItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.SHIELD));
      }
   }
}
