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

public final class GainExperienceBonus implements EventListenerBonus<GainExperienceBonus> {
   private float chance;
   private int amount;
   private SkillEventListener eventListener;

   public GainExperienceBonus(float chance, int amount, SkillEventListener eventListener) {
      this.chance = chance;
      this.amount = amount;
      this.eventListener = eventListener;
   }

   public GainExperienceBonus(float chance, int amount) {
      this(chance, amount, new AttackEventListener().setTarget(SkillBonus.Target.PLAYER));
   }

   @Override
   public void applyEffect(LivingEntity target) {
      if (target instanceof Player player) {
         if (target.getRandom().nextFloat() < this.chance) {
            player.giveExperiencePoints(this.amount);
         }
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.GAIN_EXPERIENCE.get();
   }

   public GainExperienceBonus copy() {
      return new GainExperienceBonus(this.chance, this.amount, this.eventListener);
   }

   public GainExperienceBonus multiply(double multiplier) {
      if (this.chance == 1.0F) {
         this.amount = (int)((double)this.amount * multiplier);
      } else {
         this.chance *= (float)multiplier;
      }

      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof GainExperienceBonus otherBonus) {
         return otherBonus.amount != this.amount ? false : Objects.equals(otherBonus.eventListener, this.eventListener);
      } else {
         return false;
      }
   }

   public GainExperienceBonus merge(SkillBonus<?> other) {
      if (other instanceof GainExperienceBonus otherBonus) {
         return otherBonus.chance == 1.0F && this.chance == 1.0F
            ? new GainExperienceBonus(this.chance, otherBonus.amount + this.amount, this.eventListener)
            : new GainExperienceBonus(otherBonus.chance + this.chance, this.amount, this.eventListener);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      String bonusDescription = this.getDescriptionId();
      if (this.chance < 1.0F) {
         bonusDescription = bonusDescription + ".chance";
      }

      MutableComponent tooltip = Component.translatable(bonusDescription, new Object[]{this.amount});
      if (this.chance < 1.0F) {
         tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      }

      tooltip = this.eventListener.getTooltip(tooltip);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public SkillEventListener getEventListener() {
      return this.eventListener;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<EventListenerBonus<GainExperienceBonus>> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.addLabel(110, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.addNumericTextField(110, 0, 90, 14, (double)this.amount)
         .setNumericFilter(value -> (double)value.intValue() == value)
         .setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.eventListener)
         .setResponder(eventListener -> this.selectEventListener(editor, consumer, eventListener))
         .setMenuInitFunc(() -> this.addEventListenerWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addEventListenerWidgets(SkillTreeEditor editor, Consumer<EventListenerBonus<GainExperienceBonus>> consumer) {
      this.eventListener.addEditorWidgets(editor, eventListener -> {
         this.setEventListener(eventListener);
         consumer.accept(this.copy());
      });
   }

   private void selectEventListener(SkillTreeEditor editor, Consumer<EventListenerBonus<GainExperienceBonus>> consumer, SkillEventListener eventListener) {
      this.setEventListener(eventListener);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectAmount(Consumer<EventListenerBonus<GainExperienceBonus>> consumer, Double value) {
      this.setAmount(value.intValue());
      consumer.accept(this.copy());
   }

   private void selectChance(Consumer<EventListenerBonus<GainExperienceBonus>> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setEventListener(SkillEventListener eventListener) {
      this.eventListener = eventListener;
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public GainExperienceBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         int amount = SerializationHelper.getElement(json, "amount").getAsInt();
         GainExperienceBonus bonus = new GainExperienceBonus(chance, amount);
         bonus.eventListener = SerializationHelper.deserializeEventListener(json);
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof GainExperienceBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            json.addProperty("amount", aBonus.amount);
            SerializationHelper.serializeEventListener(json, aBonus.eventListener);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GainExperienceBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         int amount = tag.getInt("amount");
         GainExperienceBonus bonus = new GainExperienceBonus(chance, amount);
         bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof GainExperienceBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            tag.putInt("amount", aBonus.amount);
            SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GainExperienceBonus deserialize(FriendlyByteBuf buf) {
         float chance = buf.readFloat();
         int amount = buf.readInt();
         GainExperienceBonus bonus = new GainExperienceBonus(chance, amount);
         bonus.eventListener = NetworkHelper.readEventListener(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof GainExperienceBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            buf.writeInt(aBonus.amount);
            NetworkHelper.writeEventListener(buf, aBonus.eventListener);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new GainExperienceBonus(0.05F, 5);
      }
   }
}
