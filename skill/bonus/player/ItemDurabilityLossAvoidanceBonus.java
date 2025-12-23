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
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ItemDurabilityLossAvoidanceBonus implements SkillBonus<ItemDurabilityLossAvoidanceBonus> {
   private float chance;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   @Nonnull
   private ItemStackPredicate itemStackPredicate = NoneItemStackPredicate.INSTANCE;

   public ItemDurabilityLossAvoidanceBonus(float chance) {
      this.chance = chance;
   }

   public float getChance(Player player, ItemStack itemStack) {
      if (!this.playerCondition.test(player)) {
         return 0.0F;
      } else {
         return !this.itemStackPredicate.test(itemStack) ? 0.0F : this.chance * this.playerMultiplier.getValue(player);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.ITEM_DURABILITY_LOSS_AVOIDANCE.get();
   }

   public ItemDurabilityLossAvoidanceBonus copy() {
      ItemDurabilityLossAvoidanceBonus bonus = new ItemDurabilityLossAvoidanceBonus(this.chance);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      bonus.itemStackPredicate = this.itemStackPredicate;
      return bonus;
   }

   public ItemDurabilityLossAvoidanceBonus multiply(double multiplier) {
      this.chance *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof ItemDurabilityLossAvoidanceBonus otherBonus) {
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
   public SkillBonus<ItemDurabilityLossAvoidanceBonus> merge(SkillBonus<?> other) {
      if (other instanceof ItemDurabilityLossAvoidanceBonus otherBonus) {
         float mergedChance = otherBonus.chance + this.chance;
         ItemDurabilityLossAvoidanceBonus mergedBonus = new ItemDurabilityLossAvoidanceBonus(mergedChance);
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
      MutableComponent tooltip;
      if (this.chance < 1.0F) {
         tooltip = Component.translatable(this.getDescriptionId() + ".chance", new Object[]{this.itemStackPredicate.getTooltip()});
         tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      } else {
         tooltip = Component.translatable(this.getDescriptionId(), new Object[]{this.itemStackPredicate.getTooltip()});
      }

      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<ItemDurabilityLossAvoidanceBonus> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
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

   private void selectChance(Consumer<ItemDurabilityLossAvoidanceBonus> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, c -> {
         this.setItemCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<ItemDurabilityLossAvoidanceBonus> consumer, ItemStackPredicate condition) {
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

   public void setChance(float chance) {
      this.chance = chance;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public ItemDurabilityLossAvoidanceBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         ItemDurabilityLossAvoidanceBonus bonus = new ItemDurabilityLossAvoidanceBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         bonus.itemStackPredicate = SerializationHelper.deserializeItemCondition(json);
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof ItemDurabilityLossAvoidanceBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeItemCondition(json, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemDurabilityLossAvoidanceBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         ItemDurabilityLossAvoidanceBonus bonus = new ItemDurabilityLossAvoidanceBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         bonus.itemStackPredicate = SerializationHelper.deserializeItemCondition(tag);
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof ItemDurabilityLossAvoidanceBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            SerializationHelper.serializeItemCondition(tag, aBonus.itemStackPredicate);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemDurabilityLossAvoidanceBonus deserialize(FriendlyByteBuf buf) {
         float chance = buf.readFloat();
         ItemDurabilityLossAvoidanceBonus bonus = new ItemDurabilityLossAvoidanceBonus(chance);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         bonus.itemStackPredicate = NetworkHelper.readItemCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof ItemDurabilityLossAvoidanceBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
            NetworkHelper.writeItemCondition(buf, aBonus.itemStackPredicate);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new ItemDurabilityLossAvoidanceBonus(0.1F);
      }
   }
}
