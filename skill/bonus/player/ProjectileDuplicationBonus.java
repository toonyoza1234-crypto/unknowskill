package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import daripher.skilltree.skill.bonus.predicate.living.HasItemEquippedEntityPredicate;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;

public final class ProjectileDuplicationBonus implements SkillBonus<ProjectileDuplicationBonus> {
   private float chance;
   @Nonnull
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
   @Nonnull
   private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;

   public ProjectileDuplicationBonus(float chance) {
      this.chance = chance;
   }

   public float getChance(Player player) {
      return !this.playerCondition.test(player) ? 0.0F : this.chance * this.playerMultiplier.getValue(player);
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.PROJECTILE_DUPLICATION.get();
   }

   public ProjectileDuplicationBonus copy() {
      ProjectileDuplicationBonus bonus = new ProjectileDuplicationBonus(this.chance);
      bonus.playerMultiplier = this.playerMultiplier;
      bonus.playerCondition = this.playerCondition;
      return bonus;
   }

   public ProjectileDuplicationBonus multiply(double multiplier) {
      this.chance *= (float)multiplier;
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof ProjectileDuplicationBonus otherBonus) {
         return !Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier) ? false : Objects.equals(otherBonus.playerCondition, this.playerCondition);
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<ProjectileDuplicationBonus> merge(SkillBonus<?> other) {
      if (other instanceof ProjectileDuplicationBonus otherBonus) {
         float mergedChance = otherBonus.chance + this.chance;
         ProjectileDuplicationBonus mergedBonus = new ProjectileDuplicationBonus(mergedChance);
         mergedBonus.playerMultiplier = this.playerMultiplier;
         mergedBonus.playerCondition = this.playerCondition;
         return mergedBonus;
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      MutableComponent tooltip;
      if (this.chance < 1.0F || this.chance % 1.0F != 0.0F) {
         tooltip = TooltipHelper.getSkillBonusTooltip(this.getDescriptionId() + ".chance", (double)this.chance, Operation.MULTIPLY_BASE);
      } else if (this.chance == 1.0F) {
         tooltip = Component.translatable(this.getDescriptionId());
      } else {
         tooltip = Component.translatable(this.getDescriptionId() + ".amount", new Object[]{(int)this.chance});
      }

      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      tooltip = this.playerCondition.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<ProjectileDuplicationBonus> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.increaseHeight(19);
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

   private void selectChance(Consumer<ProjectileDuplicationBonus> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<ProjectileDuplicationBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, multiplier -> {
         this.setPlayerMultiplier(multiplier);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<ProjectileDuplicationBonus> consumer, LivingMultiplier multiplier) {
      this.setPlayerMultiplier(multiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<ProjectileDuplicationBonus> consumer) {
      this.playerCondition.addEditorWidgets(editor, c -> {
         this.setPlayerCondition(c);
         consumer.accept(this.copy());
      });
   }

   private void selectPlayerCondition(SkillTreeEditor editor, Consumer<ProjectileDuplicationBonus> consumer, LivingEntityPredicate condition) {
      this.setPlayerCondition(condition);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   public SkillBonus<?> setPlayerCondition(LivingEntityPredicate condition) {
      this.playerCondition = condition;
      return this;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier multiplier) {
      this.playerMultiplier = multiplier;
      return this;
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public ProjectileDuplicationBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         ProjectileDuplicationBonus bonus = new ProjectileDuplicationBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(json, "player_condition");
         return bonus;
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof ProjectileDuplicationBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(json, aBonus.playerCondition, "player_condition");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ProjectileDuplicationBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         ProjectileDuplicationBonus bonus = new ProjectileDuplicationBonus(chance);
         bonus.playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         bonus.playerCondition = SerializationHelper.deserializeLivingCondition(tag, "player_condition");
         return bonus;
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof ProjectileDuplicationBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ProjectileDuplicationBonus deserialize(FriendlyByteBuf buf) {
         float chance = buf.readFloat();
         ProjectileDuplicationBonus bonus = new ProjectileDuplicationBonus(chance);
         bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
         return bonus;
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof ProjectileDuplicationBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
            NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new ProjectileDuplicationBonus(0.1F)
            .setPlayerCondition(new HasItemEquippedEntityPredicate(new EquipmentPredicate(EquipmentPredicate.Type.RANGED_WEAPON)));
      }
   }
}
