package daripher.skilltree.skill.bonus.predicate.damage;

import daripher.skilltree.init.PSTRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface DamageCondition {
   boolean met(DamageSource var1);

   default String getDescriptionId() {
      ResourceLocation id = PSTRegistries.DAMAGE_CONDITIONS.get().getKey(this.getSerializer());
      return "damage_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   default MutableComponent getTooltip() {
      return Component.translatable(this.getDescriptionId());
   }

   default MutableComponent getTooltip(String type) {
      return Component.translatable(this.getDescriptionId() + "." + type);
   }

   DamageCondition.Serializer getSerializer();

   default DamageSource createDamageSource(Player player) {
      throw new UnsupportedOperationException("Can not create damage source from " + this.getDescriptionId());
   }

   default boolean canCreateDamageSource() {
      return false;
   }

   public interface Serializer extends daripher.skilltree.data.serializers.Serializer<DamageCondition> {
      DamageCondition createDefaultInstance();
   }
}
