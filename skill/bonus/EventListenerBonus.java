package daripher.skilltree.skill.bonus;

import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.event.SkillLearnedEventListener;
import daripher.skilltree.skill.bonus.event.SkillRemovedEventListener;
import daripher.skilltree.skill.bonus.event.TickingEventListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface EventListenerBonus<T> extends TickingSkillBonus, SkillBonus<EventListenerBonus<T>> {
   @Override
   default void onSkillLearned(ServerPlayer player, boolean firstTime) {
      if (firstTime && this.getEventListener() instanceof SkillLearnedEventListener listener) {
         listener.onEvent(player, this);
      }
   }

   @Override
   default void onSkillRemoved(ServerPlayer player) {
      if (this.getEventListener() instanceof SkillRemovedEventListener listener) {
         listener.onEvent(player, this);
      }
   }

   @Override
   default void tick(ServerPlayer player) {
      if (this.getEventListener() instanceof TickingEventListener listener && player.tickCount % listener.getCooldown() == 0) {
         listener.onEvent(player, this);
      }
   }

   SkillEventListener getEventListener();

   void applyEffect(LivingEntity var1);
}
