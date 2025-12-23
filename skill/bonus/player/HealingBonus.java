package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.AttackEventListener;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class HealingBonus implements EventListenerBonus<HealingBonus> {
   private float chance;
   private float amount;
   private SkillEventListener eventListener;
   private boolean isPercentageHealing;

   public HealingBonus(float chance, float amount, SkillEventListener eventListener, boolean isPercentageHealing) {
      this.chance = chance;
      this.amount = amount;
      this.eventListener = eventListener;
      this.isPercentageHealing = isPercentageHealing;
   }

   public HealingBonus(float chance, float amount) {
      this(chance, amount, new AttackEventListener().setTarget(SkillBonus.Target.PLAYER), false);
   }

   @Override
   public void applyEffect(LivingEntity target) {
      if (target.getRandom().nextFloat() < this.chance) {
         float healAmount = this.amount;
         if (this.isPercentageHealing) {
            healAmount = this.amount * target.getMaxHealth();
         }

         if (target.getHealth() < target.getMaxHealth() && target instanceof Player player) {
            player.getFoodData().addExhaustion(healAmount / 2.0F);
         }

         target.heal(healAmount);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.HEALING.get();
   }

   public HealingBonus copy() {
      return new HealingBonus(this.chance, this.amount, this.eventListener, this.isPercentageHealing);
   }

   public HealingBonus multiply(double multiplier) {
      if (this.chance == 1.0F) {
         this.amount *= (float)multiplier;
      } else {
         this.chance *= (float)multiplier;
      }

      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof HealingBonus otherBonus) {
         if (otherBonus.amount != this.amount) {
            return false;
         } else {
            return otherBonus.isPercentageHealing != this.isPercentageHealing ? false : Objects.equals(otherBonus.eventListener, this.eventListener);
         }
      } else {
         return false;
      }
   }

   public HealingBonus merge(SkillBonus<?> other) {
      if (other instanceof HealingBonus otherBonus) {
         return otherBonus.chance == 1.0F && this.chance == 1.0F
            ? new HealingBonus(this.chance, otherBonus.amount + this.amount, this.eventListener, this.isPercentageHealing)
            : new HealingBonus(otherBonus.chance + this.chance, this.amount, this.eventListener, this.isPercentageHealing);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String targetDescription = this.eventListener.getTarget().name().toLowerCase();
      String bonusDescription = this.getDescriptionId() + "." + targetDescription;
      if (this.chance < 1.0F) {
         bonusDescription = bonusDescription + ".chance";
      }

      String amountDescription;
      if (this.isPercentageHealing) {
         amountDescription = TooltipHelper.formatNumber((double)(this.amount * 100.0F)) + "%";
      } else {
         amountDescription = TooltipHelper.formatNumber((double)this.amount);
      }

      MutableComponent tooltip = Component.translatable(bonusDescription, new Object[]{amountDescription});
      if (this.chance < 1.0F) {
         tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      }

      tooltip = this.eventListener.getTooltip(tooltip);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F ^ this.eventListener.getTarget() == SkillBonus.Target.ENEMY;
   }

   @Override
   public SkillEventListener getEventListener() {
      return this.eventListener;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<EventListenerBonus<HealingBonus>> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.addLabel(110, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.addNumericTextField(110, 0, 90, 14, (double)this.amount).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Percentage Healing", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addCheckBox(0, 0, this.isPercentageHealing).setResponder(value -> this.selectPercentageHealing(editor, consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.eventListener)
         .setResponder(eventListener -> this.selectEventListener(editor, consumer, eventListener))
         .setMenuInitFunc(() -> this.addEventListenerWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addEventListenerWidgets(SkillTreeEditor editor, Consumer<EventListenerBonus<HealingBonus>> consumer) {
      this.eventListener.addEditorWidgets(editor, eventListener -> {
         this.setEventListener(eventListener);
         consumer.accept(this.copy());
      });
   }

   private void selectEventListener(SkillTreeEditor editor, Consumer<EventListenerBonus<HealingBonus>> consumer, SkillEventListener eventListener) {
      this.setEventListener(eventListener);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectPercentageHealing(SkillTreeEditor editor, Consumer<EventListenerBonus<HealingBonus>> consumer, boolean isPercentageHealing) {
      this.setPercentageHealing(isPercentageHealing);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectAmount(Consumer<EventListenerBonus<HealingBonus>> consumer, Double value) {
      this.setAmount(value.floatValue());
      consumer.accept(this.copy());
   }

   private void selectChance(Consumer<EventListenerBonus<HealingBonus>> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setPercentageHealing(boolean percentageHealing) {
      this.isPercentageHealing = percentageHealing;
   }

   public void setEventListener(SkillEventListener eventListener) {
      this.eventListener = eventListener;
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public void setAmount(float amount) {
      this.amount = amount;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public HealingBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         float amount = SerializationHelper.getElement(json, "amount").getAsFloat();
         HealingBonus bonus = new HealingBonus(chance, amount);
         bonus.eventListener = SerializationHelper.deserializeEventListener(json);
         if (json.has("percentage_healing")) {
            bonus.setPercentageHealing(json.get("percentage_healing").getAsBoolean());
         }

         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof HealingBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            json.addProperty("amount", aBonus.amount);
            SerializationHelper.serializeEventListener(json, aBonus.eventListener);
            json.addProperty("percentage_healing", aBonus.isPercentageHealing);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public HealingBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         float amount = tag.getFloat("amount");
         HealingBonus bonus = new HealingBonus(chance, amount);
         bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
         if (tag.contains("percentage_healing")) {
            bonus.setPercentageHealing(tag.getBoolean("percentage_healing"));
         }

         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof HealingBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            tag.putFloat("amount", aBonus.amount);
            SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
            tag.putBoolean("percentage_healing", aBonus.isPercentageHealing);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public HealingBonus deserialize(FriendlyByteBuf buf) {
         float chance = buf.readFloat();
         float amount = buf.readFloat();
         HealingBonus bonus = new HealingBonus(chance, amount);
         bonus.eventListener = NetworkHelper.readEventListener(buf);
         bonus.isPercentageHealing = buf.readBoolean();
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof HealingBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            buf.writeFloat(aBonus.amount);
            NetworkHelper.writeEventListener(buf, aBonus.eventListener);
            buf.writeBoolean(aBonus.isPercentageHealing);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new HealingBonus(0.05F, 5.0F);
      }
   }
}
