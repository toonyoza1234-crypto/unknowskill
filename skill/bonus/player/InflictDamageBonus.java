package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTDamageConditions;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.BlockEventListener;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.predicate.damage.DamageCondition;
import daripher.skilltree.skill.bonus.predicate.damage.MagicDamageCondition;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class InflictDamageBonus implements EventListenerBonus<InflictDamageBonus> {
   private float chance;
   private float damage;
   private SkillEventListener eventListener;
   private DamageCondition damageType;

   public InflictDamageBonus(float chance, float damage, SkillEventListener eventListener, DamageCondition damageType) {
      this.chance = chance;
      this.damage = damage;
      this.eventListener = eventListener;
      this.damageType = damageType;
   }

   public InflictDamageBonus(float chance, float damage) {
      this(chance, damage, new BlockEventListener(), new MagicDamageCondition());
   }

   @Override
   public void applyEffect(LivingEntity target) {
      target.hurt(target.level().damageSources().magic(), this.damage);
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.INFLICT_DAMAGE.get();
   }

   public InflictDamageBonus copy() {
      return new InflictDamageBonus(this.chance, this.damage, this.eventListener, this.damageType);
   }

   public InflictDamageBonus multiply(double multiplier) {
      if (this.chance < 1.0F) {
         this.chance *= (float)multiplier;
      } else {
         this.damage *= (float)multiplier;
      }

      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof InflictDamageBonus otherBonus) {
         if (otherBonus.chance < 1.0F && this.chance < 1.0F && otherBonus.damage != this.damage) {
            return false;
         } else {
            return !Objects.equals(otherBonus.eventListener, this.eventListener) ? false : Objects.equals(this.damageType, otherBonus.damageType);
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<EventListenerBonus<InflictDamageBonus>> merge(SkillBonus<?> other) {
      if (other instanceof InflictDamageBonus otherBonus) {
         return otherBonus.chance < 1.0F && this.chance < 1.0F
            ? new InflictDamageBonus(otherBonus.chance + this.chance, this.damage, this.eventListener, this.damageType)
            : new InflictDamageBonus(this.chance, otherBonus.damage + this.damage, this.eventListener, this.damageType);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String targetDescription = this.eventListener.getTarget().getName();
      String key = this.getDescriptionId() + "." + targetDescription;
      String damageDescription = TooltipHelper.formatNumber((double)this.damage);
      Component damageTypeDescription = this.damageType.getTooltip("type");
      if (this.chance < 1.0F) {
         key = key + ".chance";
      }

      MutableComponent tooltip = Component.translatable(key, new Object[]{damageDescription, damageTypeDescription});
      if (this.chance < 1.0F) {
         tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      }

      tooltip = this.eventListener.getTooltip(tooltip);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F ^ this.eventListener.getTarget() == SkillBonus.Target.PLAYER;
   }

   @Override
   public SkillEventListener getEventListener() {
      return this.eventListener;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<EventListenerBonus<InflictDamageBonus>> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.addLabel(110, 0, "Damage", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.addNumericTextField(110, 0, 90, 14, (double)this.damage).setNumericResponder(value -> this.selectDamage(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Damage Type", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      List<DamageCondition> damageTypes = PSTDamageConditions.conditionsList().stream().filter(DamageCondition::canCreateDamageSource).toList();
      editor.addSelectionMenu(0, 0, 200, damageTypes)
         .setValue(this.damageType)
         .setElementNameGetter(c -> Component.translatable(PSTDamageConditions.getName(c)))
         .setResponder(damageType -> this.selectDamageType(editor, consumer, damageType));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.eventListener)
         .setResponder(eventListener -> this.selectEventListener(editor, consumer, eventListener))
         .setMenuInitFunc(() -> this.addEventListenerWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addEventListenerWidgets(SkillTreeEditor editor, Consumer<EventListenerBonus<InflictDamageBonus>> consumer) {
      this.eventListener.addEditorWidgets(editor, eventListener -> {
         this.setEventListener(eventListener);
         consumer.accept(this.copy());
      });
   }

   private void selectEventListener(SkillTreeEditor editor, Consumer<EventListenerBonus<InflictDamageBonus>> consumer, SkillEventListener eventListener) {
      this.setEventListener(eventListener);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectDamageType(SkillTreeEditor editor, Consumer<EventListenerBonus<InflictDamageBonus>> consumer, DamageCondition damageType) {
      this.setDamageType(damageType);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectDamage(Consumer<EventListenerBonus<InflictDamageBonus>> consumer, Double value) {
      this.setDamage((float)value.intValue());
      consumer.accept(this.copy());
   }

   private void selectChance(Consumer<EventListenerBonus<InflictDamageBonus>> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setEventListener(SkillEventListener eventListener) {
      this.eventListener = eventListener;
   }

   public void setDamageType(DamageCondition damageType) {
      this.damageType = damageType;
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public void setDamage(float damage) {
      this.damage = damage;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public InflictDamageBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = json.get("chance").getAsFloat();
         float damage = (float)json.get("damage").getAsInt();
         InflictDamageBonus bonus = new InflictDamageBonus(chance, damage);
         bonus.eventListener = SerializationHelper.deserializeEventListener(json);
         if (json.has("damage_type")) {
            bonus.damageType = SerializationHelper.deserializeDamageCondition(json, "damage_type");
         }

         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof InflictDamageBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            json.addProperty("damage", aBonus.damage);
            SerializationHelper.serializeEventListener(json, aBonus.eventListener);
            SerializationHelper.serializeDamageCondition(json, aBonus.damageType, "damage_type");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public InflictDamageBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         float damage = tag.getFloat("damage");
         InflictDamageBonus bonus = new InflictDamageBonus(chance, damage);
         bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
         if (tag.contains("damage_type")) {
            bonus.damageType = SerializationHelper.deserializeDamageCondition(tag, "damage_type");
         }

         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof InflictDamageBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            tag.putFloat("damage", aBonus.damage);
            SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
            SerializationHelper.serializeDamageCondition(tag, aBonus.damageType, "damage_type");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public InflictDamageBonus deserialize(FriendlyByteBuf buf) {
         float amount = buf.readFloat();
         float damage = buf.readFloat();
         InflictDamageBonus bonus = new InflictDamageBonus(amount, damage);
         bonus.eventListener = NetworkHelper.readEventListener(buf);
         bonus.damageType = NetworkHelper.readDamageCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof InflictDamageBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            buf.writeFloat(aBonus.damage);
            NetworkHelper.writeEventListener(buf, aBonus.eventListener);
            NetworkHelper.writeDamageCondition(buf, aBonus.damageType);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new InflictDamageBonus(0.05F, 5.0F);
      }
   }
}
