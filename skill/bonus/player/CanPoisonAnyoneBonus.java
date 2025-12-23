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

public final class CanPoisonAnyoneBonus implements SkillBonus<CanPoisonAnyoneBonus> {
   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.CAN_POISON_ANYONE.get();
   }

   public CanPoisonAnyoneBonus copy() {
      return new CanPoisonAnyoneBonus();
   }

   public CanPoisonAnyoneBonus multiply(double multiplier) {
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof CanPoisonAnyoneBonus;
   }

   @Override
   public SkillBonus<CanPoisonAnyoneBonus> merge(SkillBonus<?> other) {
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
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<CanPoisonAnyoneBonus> consumer) {
   }

   public static class Serializer implements SkillBonus.Serializer {
      public CanPoisonAnyoneBonus deserialize(JsonObject json) throws JsonParseException {
         return new CanPoisonAnyoneBonus();
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (!(bonus instanceof CanPoisonAnyoneBonus)) {
            throw new IllegalArgumentException();
         }
      }

      public CanPoisonAnyoneBonus deserialize(CompoundTag tag) {
         return new CanPoisonAnyoneBonus();
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (!(bonus instanceof CanPoisonAnyoneBonus)) {
            throw new IllegalArgumentException();
         } else {
            return new CompoundTag();
         }
      }

      public CanPoisonAnyoneBonus deserialize(FriendlyByteBuf buf) {
         return new CanPoisonAnyoneBonus();
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (!(bonus instanceof CanPoisonAnyoneBonus)) {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new CanPoisonAnyoneBonus();
      }
   }
}
