package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class ArrowRetrievalBonus implements SkillBonus<ArrowRetrievalBonus> {
   private float chance;

   public ArrowRetrievalBonus(float chance) {
      this.chance = chance;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.ARROW_RETRIEVAL.get();
   }

   public ArrowRetrievalBonus copy() {
      return new ArrowRetrievalBonus(this.chance);
   }

   public ArrowRetrievalBonus multiply(double multiplier) {
      return new ArrowRetrievalBonus((float)((double)this.getChance() * multiplier));
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof ArrowRetrievalBonus;
   }

   @Override
   public SkillBonus<ArrowRetrievalBonus> merge(SkillBonus<?> other) {
      if (other instanceof ArrowRetrievalBonus otherBonus) {
         return new ArrowRetrievalBonus(otherBonus.chance + this.chance);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      return TooltipHelper.getSkillBonusTooltip(this.getDescriptionId(), (double)this.chance, Operation.MULTIPLY_BASE)
         .withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<ArrowRetrievalBonus> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.increaseHeight(19);
   }

   private void selectChance(Consumer<ArrowRetrievalBonus> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public float getChance() {
      return this.chance;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         ArrowRetrievalBonus that = (ArrowRetrievalBonus)obj;
         return Float.floatToIntBits(this.chance) == Float.floatToIntBits(that.chance);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chance);
   }

   public static class Serializer implements SkillBonus.Serializer {
      public ArrowRetrievalBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         return new ArrowRetrievalBonus(chance);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof ArrowRetrievalBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ArrowRetrievalBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         return new ArrowRetrievalBonus(chance);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof ArrowRetrievalBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ArrowRetrievalBonus deserialize(FriendlyByteBuf buf) {
         return new ArrowRetrievalBonus(buf.readFloat());
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof ArrowRetrievalBonus aBonus) {
            buf.writeFloat(aBonus.chance);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new ArrowRetrievalBonus(0.05F);
      }
   }
}
