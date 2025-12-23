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
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class GainedExperienceBonus implements SkillBonus<GainedExperienceBonus> {
   private GainedExperienceBonus.ExperienceSource experienceSource;
   private float multiplier;
   private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;

   public GainedExperienceBonus(float multiplier, GainedExperienceBonus.ExperienceSource source) {
      this.multiplier = multiplier;
      this.experienceSource = source;
   }

   public GainedExperienceBonus(float multiplier, GainedExperienceBonus.ExperienceSource source, LivingMultiplier playerMultiplier) {
      this.multiplier = multiplier;
      this.experienceSource = source;
      this.playerMultiplier = playerMultiplier;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.GAINED_EXPERIENCE.get();
   }

   public GainedExperienceBonus copy() {
      return new GainedExperienceBonus(this.multiplier, this.experienceSource, this.playerMultiplier);
   }

   public GainedExperienceBonus multiply(double multiplier) {
      this.multiplier = (float)((double)this.multiplier * multiplier);
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof GainedExperienceBonus otherBonus) {
         return !Objects.equals(otherBonus.experienceSource, this.experienceSource)
            ? false
            : Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier);
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<GainedExperienceBonus> merge(SkillBonus<?> other) {
      if (other instanceof GainedExperienceBonus otherBonus) {
         return new GainedExperienceBonus(otherBonus.multiplier + this.multiplier, this.experienceSource, this.playerMultiplier);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component sourceDescription = Component.translatable(this.experienceSource.getDescriptionId());
      MutableComponent tooltip = Component.translatable(this.getDescriptionId(), new Object[]{sourceDescription});
      tooltip = TooltipHelper.getSkillBonusTooltip(tooltip, (double)this.multiplier, Operation.MULTIPLY_BASE);
      tooltip = this.playerMultiplier.getTooltip(tooltip, SkillBonus.Target.PLAYER);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.multiplier > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<GainedExperienceBonus> consumer) {
      editor.addLabel(110, 0, "Multiplier", ChatFormatting.GOLD);
      editor.addLabel(0, 0, "Source", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(110, 0, 90, 14, (double)this.multiplier).setNumericResponder(value -> this.selectMultiplier(consumer, value));
      editor.addSelection(0, 0, 80, 1, this.experienceSource)
         .setNameGetter(GainedExperienceBonus.ExperienceSource::getFormattedName)
         .setResponder(experienceSource -> this.selectExperienceSource(consumer, experienceSource));
      editor.increaseHeight(29);
      editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.playerMultiplier)
         .setResponder(multiplier -> this.selectPlayerMultiplier(editor, consumer, multiplier))
         .setMenuInitFunc(() -> this.addPlayerMultiplierWidgets(editor, consumer));
      editor.increaseHeight(19);
   }

   private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<GainedExperienceBonus> consumer, LivingMultiplier playerMultiplier) {
      this.setPlayerMultiplier(playerMultiplier);
      consumer.accept(this.copy());
      editor.rebuildWidgets();
   }

   private void selectExperienceSource(Consumer<GainedExperienceBonus> consumer, GainedExperienceBonus.ExperienceSource experienceSource) {
      this.setExpericenSource(experienceSource);
      consumer.accept(this.copy());
   }

   private void selectMultiplier(Consumer<GainedExperienceBonus> consumer, Double value) {
      this.setMultiplier(value.floatValue());
      consumer.accept(this.copy());
   }

   private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<GainedExperienceBonus> consumer) {
      this.playerMultiplier.addEditorWidgets(editor, m -> {
         this.setPlayerMultiplier(m);
         consumer.accept(this.copy());
      });
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   public void setExpericenSource(GainedExperienceBonus.ExperienceSource experienceSource) {
      this.experienceSource = experienceSource;
   }

   public SkillBonus<?> setPlayerMultiplier(LivingMultiplier playerMultiplier) {
      this.playerMultiplier = playerMultiplier;
      return this;
   }

   public float getMultiplier() {
      return this.multiplier;
   }

   public GainedExperienceBonus.ExperienceSource getSource() {
      return this.experienceSource;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GainedExperienceBonus that = (GainedExperienceBonus)o;
         return Float.compare(this.multiplier, that.multiplier) != 0 ? false : this.experienceSource == that.experienceSource;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.experienceSource, this.multiplier);
   }

   public static enum ExperienceSource {
      MOBS("mobs"),
      FISHING("fishing"),
      ORE("ore");

      final String name;

      private ExperienceSource(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public Component getFormattedName() {
         return Component.literal(this.getName().substring(0, 1).toUpperCase() + this.getName().substring(1));
      }

      public static GainedExperienceBonus.ExperienceSource byName(String name) {
         for (GainedExperienceBonus.ExperienceSource type : values()) {
            if (type.name.equals(name)) {
               return type;
            }
         }

         return MOBS;
      }

      public String getDescriptionId() {
         return "experience.source." + this.getName();
      }
   }

   public static class Serializer implements SkillBonus.Serializer {
      public GainedExperienceBonus deserialize(JsonObject json) throws JsonParseException {
         float multiplier = SerializationHelper.getElement(json, "multiplier").getAsFloat();
         GainedExperienceBonus.ExperienceSource experienceSource = GainedExperienceBonus.ExperienceSource.byName(json.get("experience_source").getAsString());
         LivingMultiplier playerMultiplier = SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
         return new GainedExperienceBonus(multiplier, experienceSource, playerMultiplier);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof GainedExperienceBonus aBonus) {
            json.addProperty("multiplier", aBonus.multiplier);
            json.addProperty("experience_source", aBonus.experienceSource.name);
            SerializationHelper.serializeLivingMultiplier(json, aBonus.playerMultiplier, "player_multiplier");
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GainedExperienceBonus deserialize(CompoundTag tag) {
         float multiplier = tag.getFloat("multiplier");
         GainedExperienceBonus.ExperienceSource experienceSource = GainedExperienceBonus.ExperienceSource.byName(tag.getString("experience_source"));
         LivingMultiplier playerMultiplier = SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
         return new GainedExperienceBonus(multiplier, experienceSource, playerMultiplier);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof GainedExperienceBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("multiplier", aBonus.multiplier);
            tag.putString("experience_source", aBonus.experienceSource.name);
            SerializationHelper.serializeLivingMultiplier(tag, aBonus.playerMultiplier, "player_multiplier");
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GainedExperienceBonus deserialize(FriendlyByteBuf buf) {
         float multiplier = buf.readFloat();
         GainedExperienceBonus.ExperienceSource experienceSource = GainedExperienceBonus.ExperienceSource.values()[buf.readInt()];
         LivingMultiplier playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
         return new GainedExperienceBonus(multiplier, experienceSource, playerMultiplier);
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof GainedExperienceBonus aBonus) {
            buf.writeFloat(aBonus.multiplier);
            buf.writeInt(aBonus.experienceSource.ordinal());
            NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new GainedExperienceBonus(0.25F, GainedExperienceBonus.ExperienceSource.MOBS);
      }
   }
}
