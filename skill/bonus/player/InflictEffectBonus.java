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
import daripher.skilltree.skill.bonus.event.TickingEventListener;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class InflictEffectBonus implements EventListenerBonus<InflictEffectBonus> {
   private MobEffectInstance effectInstance;
   private SkillEventListener eventListener;
   private float chance;
   private int maxStacks;

   public InflictEffectBonus(float chance, MobEffectInstance effectInstance, SkillEventListener eventListener, int maxStacks) {
      this.chance = chance;
      this.effectInstance = effectInstance;
      this.eventListener = eventListener;
      this.maxStacks = maxStacks;
   }

   public InflictEffectBonus(float chance, MobEffectInstance effectInstance, int maxStacks) {
      this(chance, effectInstance, new AttackEventListener(), maxStacks);
   }

   @Override
   public void applyEffect(LivingEntity target) {
      RandomSource random = target.getRandom();
      if (random.nextFloat() < this.chance) {
         MobEffectInstance effectInstanceCopy = new MobEffectInstance(this.effectInstance);
         MobEffect effect = this.effectInstance.getEffect();
         if (this.maxStacks > 1) {
            effectInstanceCopy = this.getStackedEffectInstance(target, effect, effectInstanceCopy);
         }

         target.addEffect(effectInstanceCopy);
      }
   }

   private MobEffectInstance getStackedEffectInstance(LivingEntity target, MobEffect effect, MobEffectInstance effectInstanceCopy) {
      MobEffectInstance activeEffectInstance = target.getEffect(effect);
      if (activeEffectInstance == null) {
         return effectInstanceCopy;
      } else {
         int amplifier = activeEffectInstance.getAmplifier();
         if (amplifier >= this.maxStacks - 1) {
            return effectInstanceCopy;
         } else {
            int duration = this.effectInstance.getDuration();
            return new MobEffectInstance(effect, duration, amplifier + 1);
         }
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.INFLICT_EFFECT.get();
   }

   public InflictEffectBonus copy() {
      return new InflictEffectBonus(this.chance, this.effectInstance, this.eventListener, this.maxStacks);
   }

   public InflictEffectBonus multiply(double multiplier) {
      if (this.chance < 1.0F) {
         this.chance *= (float)multiplier;
         return this;
      } else {
         int newDuration = (int)((double)this.effectInstance.getDuration() * multiplier);
         this.effectInstance = new MobEffectInstance(this.effectInstance.getEffect(), newDuration, this.effectInstance.getAmplifier());
         return new InflictEffectBonus(this.chance, this.effectInstance, this.eventListener, this.maxStacks);
      }
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof InflictEffectBonus otherBonus) {
         return !Objects.equals(otherBonus.effectInstance.getEffect(), this.effectInstance.getEffect())
            ? false
            : Objects.equals(otherBonus.eventListener, this.eventListener);
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<EventListenerBonus<InflictEffectBonus>> merge(SkillBonus<?> other) {
      if (other instanceof InflictEffectBonus otherBonus) {
         if (this.chance < 1.0F) {
            return new InflictEffectBonus(otherBonus.chance + this.chance, this.effectInstance, this.eventListener, this.maxStacks);
         } else {
            int newDuration = this.effectInstance.getDuration() + otherBonus.effectInstance.getDuration();
            this.effectInstance = new MobEffectInstance(this.effectInstance.getEffect(), newDuration, this.effectInstance.getAmplifier());
            return new InflictEffectBonus(this.chance, this.effectInstance, this.eventListener, this.maxStacks);
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component effectDescription = TooltipHelper.getEffectTooltip(this.effectInstance);
      int duration = this.effectInstance.getDuration();
      SkillBonus.Target target = this.eventListener.getTarget();
      String targetDescription = target.getName();
      String bonusDescription = this.getDescriptionId() + "." + targetDescription;
      if (this.chance < 1.0F) {
         bonusDescription = bonusDescription + ".chance";
      }

      boolean isInstantEffect = duration == 0;
      boolean showDuration = !isInstantEffect && (!(this.getEventListener() instanceof TickingEventListener) || duration > 20);
      MutableComponent tooltip;
      if (showDuration) {
         Component durationDescription = this.getDurationDescription();
         tooltip = Component.translatable(bonusDescription, new Object[]{effectDescription, durationDescription});
      } else {
         tooltip = Component.translatable(bonusDescription, new Object[]{effectDescription, ""});
      }

      if (this.chance < 1.0F) {
         tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.chance, Operation.MULTIPLY_BASE);
      }

      tooltip = this.eventListener.getTooltip(tooltip);
      if (this.maxStacks > 1) {
         tooltip = Component.translatable(this.getDescriptionId() + ".stacks", new Object[]{tooltip, this.maxStacks});
      }

      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   private Component getDurationDescription() {
      boolean measureInSeconds = this.effectInstance.getDuration() < 1200;
      String measurement = measureInSeconds ? "seconds" : "minutes";
      float duration = measureInSeconds ? (float)this.effectInstance.getDuration() / 20.0F : (float)this.effectInstance.getDuration() / 1200.0F;
      String formattedDuration = TooltipHelper.formatNumber((double)duration);
      return Component.translatable(this.getDescriptionId() + "." + measurement, new Object[]{formattedDuration});
   }

   @Override
   public void gatherInfo(Consumer<MutableComponent> consumer) {
      TooltipHelper.consumeTranslated(this.effectInstance.getDescriptionId() + ".info", consumer);
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F
         ^ this.eventListener.getTarget() == SkillBonus.Target.PLAYER
         ^ this.effectInstance.getEffect().getCategory() != MobEffectCategory.HARMFUL;
   }

   @Override
   public SkillEventListener getEventListener() {
      return this.eventListener;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<EventListenerBonus<InflictEffectBonus>> consumer) {
      editor.addLabel(0, 0, "Effect", ChatFormatting.GOLD);
      editor.addLabel(150, 0, "Chance", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 145, this.effectInstance.getEffect()).setResponder(effect -> this.selectEffect(consumer, effect));
      editor.addNumericTextField(150, 0, 50, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Duration", ChatFormatting.GOLD);
      editor.addLabel(55, 0, "Amplifier", ChatFormatting.GOLD);
      editor.addLabel(110, 0, "Stacks", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.effectInstance.getDuration())
         .setNumericFilter(value -> value >= -1.0)
         .setNumericResponder(value -> this.selectDuration(consumer, value));
      editor.addNumericTextField(55, 0, 50, 14, (double)this.effectInstance.getAmplifier())
         .setNumericFilter(value -> value >= 0.0)
         .setNumericResponder(value -> this.selectAmplifier(consumer, value));
      editor.addNumericTextField(110, 0, 50, 14, (double)this.maxStacks)
         .setNumericFilter(value -> value >= 1.0)
         .setNumericResponder(value -> this.selectMaxStacks(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.eventListener)
         .setResponder(eventListener -> this.selectEventListener(editor, consumer, eventListener))
         .setMenuInitFunc(() -> this.addEventListenerWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void addEventListenerWidgets(SkillTreeEditor editor, Consumer<EventListenerBonus<InflictEffectBonus>> consumer) {
      this.eventListener.addEditorWidgets(editor, eventListener -> {
         this.setEventListener(eventListener);
         consumer.accept(this.copy());
      });
   }

   private void selectEventListener(SkillTreeEditor editor, Consumer<EventListenerBonus<InflictEffectBonus>> consumer, SkillEventListener eventListener) {
      this.setEventListener(eventListener);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectAmplifier(Consumer<EventListenerBonus<InflictEffectBonus>> consumer, Double value) {
      this.setAmplifier(value.intValue());
      consumer.accept(this.copy());
   }

   private void selectMaxStacks(Consumer<EventListenerBonus<InflictEffectBonus>> consumer, Double value) {
      this.setMaxStacks(value.intValue());
      consumer.accept(this.copy());
   }

   private void selectDuration(Consumer<EventListenerBonus<InflictEffectBonus>> consumer, Double value) {
      this.setDuration(value.intValue());
      consumer.accept(this.copy());
   }

   private void selectChance(Consumer<EventListenerBonus<InflictEffectBonus>> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   private void selectEffect(Consumer<EventListenerBonus<InflictEffectBonus>> consumer, MobEffect effect) {
      this.setEffectInstance(effect);
      consumer.accept(this);
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public void setEffectInstance(MobEffect effectInstance) {
      this.effectInstance = new MobEffectInstance(effectInstance, this.effectInstance.getDuration(), this.effectInstance.getAmplifier());
   }

   public void setDuration(int duration) {
      this.effectInstance = new MobEffectInstance(this.effectInstance.getEffect(), duration, this.effectInstance.getAmplifier());
   }

   public void setAmplifier(int amplifier) {
      this.effectInstance = new MobEffectInstance(this.effectInstance.getEffect(), this.effectInstance.getDuration(), amplifier);
   }

   public void setMaxStacks(int maxStacks) {
      this.maxStacks = maxStacks;
   }

   public void setEventListener(SkillEventListener eventListener) {
      this.eventListener = eventListener;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public InflictEffectBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         MobEffectInstance effect = SerializationHelper.deserializeEffectInstance(json);
         int maxStacks = json.has("max_stacks") ? json.get("max_stacks").getAsInt() : 0;
         InflictEffectBonus bonus = new InflictEffectBonus(chance, effect, maxStacks);
         bonus.eventListener = SerializationHelper.deserializeEventListener(json);
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof InflictEffectBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            json.addProperty("max_stacks", aBonus.maxStacks);
            SerializationHelper.serializeEffectInstance(json, aBonus.effectInstance);
            SerializationHelper.serializeEventListener(json, aBonus.eventListener);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public InflictEffectBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         MobEffectInstance effect = SerializationHelper.deserializeEffectInstance(tag);
         int maxStacks = tag.getInt("max_stacks");
         InflictEffectBonus bonus = new InflictEffectBonus(chance, effect, maxStacks);
         bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof InflictEffectBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            tag.putInt("max_stacks", aBonus.maxStacks);
            SerializationHelper.serializeEffectInstance(tag, aBonus.effectInstance);
            SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public InflictEffectBonus deserialize(FriendlyByteBuf buf) {
         float amount = buf.readFloat();
         int maxStacks = buf.readInt();
         MobEffectInstance effect = NetworkHelper.readEffectInstance(buf);
         InflictEffectBonus bonus = new InflictEffectBonus(amount, effect, maxStacks);
         bonus.eventListener = NetworkHelper.readEventListener(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof InflictEffectBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            buf.writeInt(aBonus.maxStacks);
            NetworkHelper.writeEffectInstance(buf, aBonus.effectInstance);
            NetworkHelper.writeEventListener(buf, aBonus.eventListener);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new InflictEffectBonus(0.05F, new MobEffectInstance(MobEffects.POISON, 100), 1);
      }
   }
}
