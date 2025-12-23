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
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EvasionEventListener implements SkillEventListener {
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
   private LivingEntityPredicate enemyCondition = NoneLivingEntityPredicate.INSTANCE;
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   private LivingMultiplier enemyMultiplier = NoneLivingMultiplier.INSTANCE;
   private SkillBonus.Target target = SkillBonus.Target.ENEMY;

   public void onEvent(@Nonnull Player player, @Nullable LivingEntity enemy, @Nonnull EventListenerBonus<?> skill) {
      if (this.enemyCondition == NoneLivingEntityPredicate.INSTANCE || enemy != null) {
         if (this.playerCondition.test(player)) {
            if (this.enemyCondition.test(enemy)) {
               LivingEntity target = (LivingEntity)(this.target == SkillBonus.Target.PLAYER ? player : enemy);
               if (target != null) {
                  skill.multiply((double)(this.playerMultiplier.getValue(player) * this.enemyMultiplier.getValue(enemy))).applyEffect(target);
               }
            }
         }
      }
   }

   @Override
   public MutableComponent getTooltip(Component bonusTooltip) {
      MutableComponent eventTooltip = Component.translatable(this.getDescriptionId(), new Object[]{bonusTooltip});
      eventTooltip = this.playerCondition.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
      eventTooltip = this.enemyCondition.getTooltip(eventTooltip, SkillBonus.Target.ENEMY);
      eventTooltip = this.playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
      return this.enemyMultiplier.getTooltip(eventTooltip, SkillBonus.Target.ENEMY);
   }

   @Override
   public SkillEventListener.Serializer getSerializer() {
      return (SkillEventListener.Serializer)PSTEventListeners.EVASION.get();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EvasionEventListener listener = (EvasionEventListener)o;
         return Objects.equals(this.playerCondition, listener.playerCondition)
            && Objects.equals(this.enemyCondition, listener.enemyCondition)
            && Objects.equals(this.playerMultiplier, listener.playerMultiplier)
            && Objects.equals(this.enemyMultiplier, listener.enemyMultiplier)
            && this.target == listener.target;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerCondition, this.enemyCondition, this.playerMultiplier, this.enemyMultiplier, this.target);
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      editor.addLabel(0, 0, "Player Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerCondition)
         .setResponder(condition -> this.selectPlayerCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addPlayerConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Enemy Condition", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.enemyCondition)
         .setResponder(condition -> this.selectTargetCondition(editor, consumer, condition))
         .setMenuInitFunc(() -> this.addTargetConditionWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Enemy Multiplier", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.enemyMultiplier)
         .setResponder(multiplier -> this.selectTargetMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addTargetMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Target", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelection(0, 0, 80, 1, this.target).setNameGetter(TooltipHelper::getTargetName).setResponder(target -> this.selectTarget(consumer, target));
      editor.increaseHeight(29);
   }

   private void selectTarget(Consumer<SkillEventListener> consumer, SkillBonus.Target target) {
      this.setTarget(target);
      consumer.accept(this);
   }

   private void addTargetMultiplierWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      this.enemyMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this);
      });
   }

   private void selectTargetMultiplier(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingMultiplier multiplier) {
      this.setEnemyMultiplier(multiplier);
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

   private void addTargetConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
      this.enemyCondition.addEditorWidgets(editor, condition -> {
         this.setEnemyCondition(condition);
         consumer.accept(this);
      });
   }

   private void selectTargetCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingEntityPredicate condition) {
      this.setEnemyCondition(condition);
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
      return this.target;
   }

   public void setEnemyCondition(LivingEntityPredicate enemyCondition) {
      this.enemyCondition = enemyCondition;
   }

   public void setPlayerCondition(LivingEntityPredicate playerCondition) {
      this.playerCondition = playerCondition;
   }

   public void setEnemyMultiplier(LivingMultiplier enemyMultiplier) {
      this.enemyMultiplier = enemyMultiplier;
   }

   public void setPlayerMultiplier(LivingMultiplier playerMultiplier) {
      this.playerMultiplier = playerMultiplier;
   }

   public void setTarget(SkillBonus.Target target) {
      this.target = target;
   }

   public static class Serializer implements SkillEventListener.Serializer {
      public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
         EvasionEventListener listener = new EvasionEventListener();
         listener.setEnemyCondition(SerializationHelper.deserializeLivingCondition(json, "enemy_condition"));
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
         listener.setEnemyMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "enemy_multiplier"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
         listener.setTarget(SkillBonus.Target.valueOf(json.get("target").getAsString().toUpperCase()));
         return listener;
      }

      public void serialize(JsonObject json, SkillEventListener listener) {
         if (listener instanceof EvasionEventListener aListener) {
            SerializationHelper.serializeLivingCondition(json, aListener.enemyCondition, "enemy_condition");
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.enemyMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
            json.addProperty("target", aListener.target.name().toLowerCase());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(CompoundTag tag) {
         EvasionEventListener listener = new EvasionEventListener();
         listener.setEnemyCondition(SerializationHelper.deserializeLivingCondition(tag, "enemy_condition"));
         listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
         listener.setEnemyMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "enemy_multiplier"));
         listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
         listener.setTarget(SkillBonus.Target.valueOf(tag.getString("target").toUpperCase()));
         return listener;
      }

      public CompoundTag serialize(SkillEventListener listener) {
         if (listener instanceof EvasionEventListener aListener) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aListener.enemyCondition, "enemy_condition");
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.enemyMultiplier, "enemy_multiplier");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            tag.putString("target", aListener.target.name().toLowerCase());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public SkillEventListener deserialize(FriendlyByteBuf buf) {
         EvasionEventListener listener = new EvasionEventListener();
         listener.setEnemyCondition(NetworkHelper.readLivingCondition(buf));
         listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
         listener.setEnemyMultiplier(NetworkHelper.readLivingMultiplier(buf));
         listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
         listener.setTarget(SkillBonus.Target.values()[buf.readInt()]);
         return listener;
      }

      public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
         if (listener instanceof EvasionEventListener aListener) {
            NetworkHelper.writeLivingCondition(buf, aListener.enemyCondition);
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.enemyMultiplier);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
            buf.writeInt(aListener.target.ordinal());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillEventListener createDefaultInstance() {
         return new EvasionEventListener();
      }
   }
}
