package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class LethalPoisonBonus implements SkillBonus<LethalPoisonBonus> {
   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.LETHAL_POISON.get();
   }

   public LethalPoisonBonus copy() {
      return new LethalPoisonBonus();
   }

   public LethalPoisonBonus multiply(double multiplier) {
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof LethalPoisonBonus;
   }

   @Override
   public SkillBonus<LethalPoisonBonus> merge(SkillBonus<?> other) {
      return this;
   }

   @Override
   public MutableComponent getTooltip() {
      return Component.translatable(this.getDescriptionId()).withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return true;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<LethalPoisonBonus> consumer) {
   }

   public static class Serializer implements SkillBonus.Serializer {
      public LethalPoisonBonus deserialize(JsonObject json) throws JsonParseException {
         return new LethalPoisonBonus();
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (!(bonus instanceof LethalPoisonBonus)) {
            throw new IllegalArgumentException();
         }
      }

      public LethalPoisonBonus deserialize(CompoundTag tag) {
         return new LethalPoisonBonus();
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (!(bonus instanceof LethalPoisonBonus)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public LethalPoisonBonus deserialize(FriendlyByteBuf buf) {
         return new LethalPoisonBonus();
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (!(bonus instanceof LethalPoisonBonus)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new LethalPoisonBonus();
      }
   }
}
