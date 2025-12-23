package daripher.skilltree.skill.bonus.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.item.PotionStackPredicate;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemUsedEventListener implements SkillEventListener {
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   private ItemStackPredicate itemStackPredicate;

   public ItemUsedEventListener(ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public void onEvent(@Nonnull Player player, @Nonnull ItemStack stack, @Nonnull EventListenerBonus<?> skill) {
      if (this.playerCondition.test(player)) {
         if (this.itemStackPredicate.test(stack)) {
            skill.multiply((double)this.playerMultiplier.getValue(player)).applyEffect(player);
         }
      }
   }

   @Override
   public MutableComponent getTooltip(Component bonusTooltip) {
      Component itemTooltip = this.itemStackPredicate.getTooltip();
      MutableComponent eventTooltip = Component.translatable(this.getDescriptionId(), new Object[]{bonusTooltip, itemTooltip});
      eventTooltip = this.playerCondition.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
      return this.playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
   }

   @Override
   public SkillEventListener.Serializer getSerializer() {
      return (SkillEventListener.Serializer)PSTEventListeners.ITEM_USED.get();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemUsedEventListener listener = (ItemUsedEventListener)o;
         return Objects.equals(this.playerCondition, listener.playerCondition)
            && Objects.equals(this.playerMultiplier, listener.playerMultiplier)
            && Objects.equals(this.itemStackPredicate, listener.itemStackPredicate);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerCondition, this.playerMultiplier, this.itemStackPredicate);
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Item Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.itemStackPredicate)
         .setResponder(condition -> this.selectItemCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addItemConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addItemConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      this.itemStackPredicate.addEditorWidgets(editor, condition -> {
         this.setItemCondition(condition);
         consumer.accept(this);
      });
   }

   private void selectItemCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, ItemStackPredicate condition) {
      this.setItemCondition(condition);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this);
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      this.playerCondition.addEditorWidgets(editor, condition -> {
         this.setPlayerCondition(condition);
         consumer.accept(this);
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this);
      editor.rebuildWidgets();
   }

   @Override
   public SkillBonus.Target getTarget() {
      return SkillBonus.Target.PLAYER;
   }

   public void setPlayerCondition(LivingEntityPredicate playerCondition) {
      this.playerCondition = playerCondition;
   }

   public void setPlayerMultiplier(LivingMultiplier playerMultiplier) {
      this.playerMultiplier = playerMultiplier;
   }

   public void setItemCondition(ItemStackPredicate itemStackPredicate) {
      this.itemStackPredicate = itemStackPredicate;
   }

   public static class Serializer implements SkillEventListener.Serializer {
      public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
         ItemStackPredicate itemStackPredicate = SerializationHelper.deserializeItemCondition(json);
         ItemUsedEventListener listener = new ItemUsedEventListener(itemStackPredicate);
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
         return listener;
      }

      public void serialize(JsonObject json, SkillEventListener listener) {
         if (listener instanceof ItemUsedEventListener aListener) {
            SerializationHelper.serializeItemCondition(json, aListener.itemStackPredicate);
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(CompoundTag tag) {
         ItemStackPredicate itemStackPredicate = SerializationHelper.deserializeItemCondition(tag);
         ItemUsedEventListener listener = new ItemUsedEventListener(itemStackPredicate);
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
         return listener;
      }

      public CompoundTag serialize(SkillEventListener listener) {
         if (listener instanceof ItemUsedEventListener aListener) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aListener.itemStackPredicate);
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(FriendlyByteBuf buf) {
         ItemStackPredicate itemStackPredicate = NetworkHelper.readItemCondition(buf);
         ItemUsedEventListener listener = new ItemUsedEventListener(itemStackPredicate);
         listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
         listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
         return listener;
      }

      public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
         if (listener instanceof ItemUsedEventListener aListener) {
            NetworkHelper.writeItemCondition(buf, aListener.itemStackPredicate);
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillEventListener createDefaultInstance() {
         return new ItemUsedEventListener(new PotionStackPredicate(PotionStackPredicate.Type.ANY));
      }
   }
}
