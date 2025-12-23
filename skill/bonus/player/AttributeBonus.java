package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.TickingSkillBonus;
import daripher.skilltree.skill.bonus.function.AttributeValueFunction;
import daripher.skilltree.skill.bonus.multiplier.FloatFunctionMultiplier;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class AttributeBonus implements SkillBonus<AttributeBonus>, TickingSkillBonus {
   private Attribute attribute;
   private AttributeModifier modifier;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;

   public AttributeBonus(Attribute attribute, AttributeModifier modifier) {
      this.attribute = attribute;
      this.modifier = modifier;
   }

   @Override
   public void onSkillLearned(ServerPlayer player, boolean firstTime) {
      if (this.playerCondition == NoneLivingEntityPredicate.INSTANCE && this.playerMultiplier == NoneLivingMultiplier.INSTANCE) {
         AttributeInstance instance = player.getAttribute(this.attribute);
         if (instance == null) {
            SkillTreeMod.LOGGER.error("Attempting to add attribute modifier to attribute {}, which is not present for player", this.attribute);
         } else {
            if (!instance.hasModifier(this.modifier)) {
               this.applyAttributeModifier(instance, this.modifier, player);
            }
         }
      }
   }

   @Override
   public void onSkillRemoved(ServerPlayer player) {
      AttributeInstance instance = player.getAttribute(this.attribute);
      if (instance == null) {
         SkillTreeMod.LOGGER.error("Attempting to remove attribute modifier from attribute {}, which is not present for player", this.attribute);
      } else {
         instance.removeModifier(this.modifier.getId());
      }
   }

   @Override
   public void tick(ServerPlayer player) {
      if (this.isDynamic()) {
         if (this.playerCondition != NoneLivingEntityPredicate.INSTANCE && !this.playerCondition.test(player)) {
            this.onSkillRemoved(player);
         } else if (this.playerMultiplier != NoneLivingMultiplier.INSTANCE && this.playerMultiplier.getValue(player) == 0.0F) {
            this.onSkillRemoved(player);
         } else {
            this.applyDynamicAttributeBonus(player);
         }
      }
   }

   public boolean isDynamic() {
      return this.playerCondition != NoneLivingEntityPredicate.INSTANCE || this.playerMultiplier != NoneLivingMultiplier.INSTANCE;
   }

   private void applyDynamicAttributeBonus(ServerPlayer player) {
      AttributeInstance instance = player.getAttribute(this.attribute);
      if (instance != null) {
         AttributeModifier oldModifier = instance.getModifier(this.modifier.getId());
         double value = this.modifier.getAmount();
         value *= (double)this.playerMultiplier.getValue(player);
         if (oldModifier == null || oldModifier.getAmount() != value) {
            AttributeModifier dynamicModifier = new AttributeModifier(this.modifier.getId(), "DynamicBonus", value, this.modifier.getOperation());
            this.applyAttributeModifier(instance, dynamicModifier, player);
         }
      }
   }

   private void applyAttributeModifier(AttributeInstance instance, AttributeModifier modifier, Player player) {
      float healthPercentage = player.getHealth() / player.getMaxHealth();
      if (instance.getModifier(modifier.getId()) != null) {
         instance.removeModifier(modifier.getId());
      }

      instance.addTransientModifier(modifier);
      if (this.attribute == Attributes.MAX_HEALTH) {
         player.setHealth(player.getMaxHealth() * healthPercentage);
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.ATTRIBUTE.get();
   }

   public AttributeBonus copy() {
      AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), this.modifier.getName(), this.modifier.getAmount(), this.modifier.getOperation());
      AttributeBonus bonus = new AttributeBonus(this.attribute, modifier);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      return bonus;
   }

   public AttributeBonus multiply(double multiplier) {
      this.modifier = new AttributeModifier(
         this.modifier.getId(), this.modifier.getName(), this.modifier.getAmount() * multiplier, this.modifier.getOperation()
      );
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof AttributeBonus otherBonus) {
         if (otherBonus.attribute != this.attribute) {
            return false;
         } else if (!Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier)) {
            return false;
         } else {
            return !Objects.equals(otherBonus.playerCondition, this.playerCondition)
               ? false
               : otherBonus.modifier.getOperation() == this.modifier.getOperation();
         }
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<AttributeBonus> merge(SkillBonus<?> other) {
      if (other instanceof AttributeBonus otherBonus) {
         AttributeModifier mergedModifier = new AttributeModifier(
            this.modifier.getId(), "Merged", this.modifier.getAmount() + otherBonus.modifier.getAmount(), this.modifier.getOperation()
         );
         AttributeBonus mergedBonus = new AttributeBonus(this.attribute, mergedModifier);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      float visibleAmount = (float)this.modifier.getAmount();
      String descriptionId = this.attribute.getDescriptionId();
      MutableComponent tooltip;
      if (this.isPercentageRegeneration()) {
         visibleAmount *= 100.0F;
         String amountDescription = TooltipHelper.formatNumber((double)visibleAmount);
         descriptionId = this.getDescriptionId() + ".percentage_regeneration";
         tooltip = Component.translatable(descriptionId, new Object[]{amountDescription});
      } else {
         if (this.isKnockbackResistanceAddition()) {
            visibleAmount *= 10.0F;
         }

         Operation operation = this.modifier.getOperation();
         tooltip = TooltipHelper.getSkillBonusTooltip(descriptionId, (double)visibleAmount, operation);
         tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      }

      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   private boolean isKnockbackResistanceAddition() {
      return this.modifier.getOperation() == Operation.ADDITION && this.attribute.equals(Attributes.KNOCKBACK_RESISTANCE);
   }

   private boolean isPercentageRegeneration() {
      return this.modifier.getOperation() == Operation.ADDITION
         && this.playerMultiplier instanceof FloatFunctionMultiplier floatFunctionMultiplier
         && floatFunctionMultiplier.getFloatFunction() instanceof AttributeValueFunction attributeValueFunction
         && attributeValueFunction.getAttribute() == Attributes.MAX_HEALTH
         && floatFunctionMultiplier.getDivisor() == 1.0F;
   }

   @Override
   public void gatherInfo(Consumer<MutableComponent> consumer) {
      SkillBonus.super.gatherInfo(consumer);
      TooltipHelper.consumeTranslated(this.attribute.getDescriptionId() + ".info", consumer);
   }

   @Override
   public boolean isPositive() {
      return this.modifier.getAmount() > 0.0;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int index, Consumer<AttributeBonus> consumer) {
      editor.addLabel(0, 0, "Attribute", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.attribute).setResponder(attribute -> this.selectAttribute(consumer, attribute));
      editor.increaseHeight(19);
      editor.addLabel(110, 0, "Amount", ChatFormatting.GOLD);
      editor.addLabel(0, 0, "Operation", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(110, 0, 50, 14, this.modifier.getAmount()).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.addOperationSelection(0, 0, 80, this.modifier.getOperation()).setResponder(operation -> this.selectOperation(consumer, operation));
      editor.increaseHeight(29);
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

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<AttributeBonus> consumer, LivingMultiplier multiplier) {
      this.setMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<AttributeBonus> consumer, LivingEntityPredicate condition) {
      this.setCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectOperation(Consumer<AttributeBonus> consumer, Operation operation) {
      this.setOperation(operation);
      consumer.accept(this.copy());
   }

   private void selectAmount(Consumer<AttributeBonus> consumer, Double value) {
      this.setAmount(value);
      consumer.accept(this.copy());
   }

   private void selectAttribute(Consumer<AttributeBonus> consumer, Attribute attribute) {
      this.setAttribute(attribute);
      consumer.accept(this.copy());
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<AttributeBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<AttributeBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, m -> {
         this.setMultiplier(m);
         consumer.accept(this.copy());
      });
   }

   public Attribute getAttribute() {
      return this.attribute;
   }

   public AttributeModifier getModifier() {
      return this.modifier;
   }

   public void setAttribute(Attribute attribute) {
      this.attribute = attribute;
   }

   public void setAmount(double amount) {
      this.modifier = new AttributeModifier(this.modifier.getId(), this.modifier.getName(), amount, this.modifier.getOperation());
   }

   public void setOperation(Operation operation) {
      this.modifier = new AttributeModifier(this.modifier.getId(), this.modifier.getName(), this.modifier.getAmount(), operation);
   }

   public SkillBonus<?> setCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public AttributeBonus deserialize(JsonObject json) throws JsonParseException {
         Attribute attribute = SerializationHelper.deserializeAttribute(json);
         AttributeModifier modifier = SerializationHelper.deserializeAttributeModifier(json);
         AttributeBonus bonus = new AttributeBonus(attribute, modifier);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof AttributeBonus aBonus) {
            SerializationHelper.serializeAttribute(json, aBonus.attribute);
            SerializationHelper.serializeAttributeModifier(json, aBonus.modifier);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public AttributeBonus deserialize(CompoundTag tag) {
         Attribute attribute = SerializationHelper.deserializeAttribute(tag);
         AttributeModifier modifier = SerializationHelper.deserializeAttributeModifier(tag);
         AttributeBonus bonus = new AttributeBonus(attribute, modifier);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof AttributeBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeAttribute(tag, aBonus.attribute);
            SerializationHelper.serializeAttributeModifier(tag, aBonus.modifier);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public AttributeBonus deserialize(FriendlyByteBuf buf) {
         Attribute attribute = NetworkHelper.readAttribute(buf);
         AttributeModifier modifier = NetworkHelper.readAttributeModifier(buf);
         AttributeBonus bonus = new AttributeBonus(attribute, modifier);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof AttributeBonus aBonus) {
            NetworkHelper.writeAttribute(buf, aBonus.attribute);
            NetworkHelper.writeAttributeModifier(buf, aBonus.modifier);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new AttributeBonus(Attributes.ARMOR, new AttributeModifier(UUID.randomUUID(), "Skill", 1.0, Operation.ADDITION));
      }
   }
}
