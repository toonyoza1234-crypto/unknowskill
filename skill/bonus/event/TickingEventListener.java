package daripher.skilltree.skill.bonus.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
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

public class TickingEventListener implements SkillEventListener {
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   private int cooldown;

   public TickingEventListener(int cooldown) {
      this.cooldown = cooldown;
   }

   public TickingEventListener() {
      this(100);
   }

   public void onEvent(@Nonnull Player player, @Nonnull EventListenerBonus<?> skill) {
      if (this.playerCondition.test(player)) {
         skill.multiply((double)this.playerMultiplier.getValue(player)).applyEffect(player);
      }
   }

   @Override
   public MutableComponent getTooltip(Component bonusTooltip) {
      String descriptionId = this.getDescriptionId();
      MutableComponent eventTooltip;
      if (this.cooldown <= 20) {
         descriptionId = descriptionId + ".second";
         eventTooltip = Component.translatable(descriptionId, new Object[]{bonusTooltip});
      } else if (this.cooldown < 1200) {
         descriptionId = descriptionId + ".seconds";
         String timeDescription = TooltipHelper.formatNumber((double)((float)this.cooldown / 20.0F));
         eventTooltip = Component.translatable(descriptionId, new Object[]{bonusTooltip, timeDescription});
      } else if (this.cooldown == 1200) {
         descriptionId = descriptionId + ".minute";
         eventTooltip = Component.translatable(descriptionId, new Object[]{bonusTooltip});
      } else {
         descriptionId = descriptionId + ".minutes";
         String timeDescription = TooltipHelper.formatNumber((double)((float)this.cooldown / 20.0F / 60.0F));
         eventTooltip = Component.translatable(descriptionId, new Object[]{bonusTooltip, timeDescription});
      }

      eventTooltip = this.playerCondition.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
      return this.playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
   }

   @Override
   public SkillEventListener.Serializer getSerializer() {
      return (SkillEventListener.Serializer)PSTEventListeners.TICKING.get();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TickingEventListener that = (TickingEventListener)o;
         return this.cooldown == that.cooldown
            && Objects.equals(this.playerCondition, that.playerCondition)
            && Objects.equals(this.playerMultiplier, that.playerMultiplier);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerCondition, this.playerMultiplier, this.cooldown);
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      editor.addLabel(0, 0, "Cooldown", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.cooldown)
         .setNumericFilter(value -> value.intValue() >= 1 && (double)value.intValue() == value)
         .setNumericResponder(value -> this.selectCooldown(consumer, value));
      editor.increaseHeight(19);
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
   }

   private void selectCooldown(Consumer<SkillEventListener> consumer, Double value) {
      this.setCooldown(value.intValue());
      consumer.accept(this);
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

   public int getCooldown() {
      return this.cooldown;
   }

   public void setCooldown(int cooldown) {
      this.cooldown = cooldown;
   }

   public void setPlayerCondition(LivingEntityPredicate playerCondition) {
      this.playerCondition = playerCondition;
   }

   public void setPlayerMultiplier(LivingMultiplier playerMultiplier) {
      this.playerMultiplier = playerMultiplier;
   }

   public static class Serializer implements SkillEventListener.Serializer {
      public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
         TickingEventListener listener = new TickingEventListener();
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
         if (!json.has("cooldown")) {
            listener.setCooldown(10);
         } else {
            listener.setCooldown(json.get("cooldown").getAsInt());
         }

         return listener;
      }

      public void serialize(JsonObject json, SkillEventListener listener) {
         if (listener instanceof TickingEventListener aListener) {
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
            json.addProperty("cooldown", aListener.cooldown);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(CompoundTag tag) {
         TickingEventListener listener = new TickingEventListener();
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
         if (!tag.contains("cooldown")) {
            listener.setCooldown(10);
         } else {
            listener.setCooldown(tag.getInt("cooldown"));
         }

         return listener;
      }

      public CompoundTag serialize(SkillEventListener listener) {
         if (listener instanceof TickingEventListener aListener) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            tag.putInt("cooldown", aListener.cooldown);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(FriendlyByteBuf buf) {
         TickingEventListener listener = new TickingEventListener();
         listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
         listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
         listener.setCooldown(buf.readInt());
         return listener;
      }

      public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
         if (listener instanceof TickingEventListener aListener) {
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
            buf.writeInt(aListener.cooldown);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillEventListener createDefaultInstance() {
         return new TickingEventListener();
      }
   }
}
