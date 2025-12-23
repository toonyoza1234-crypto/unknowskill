package daripher.skilltree.skill.bonus.predicate.living;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public final class DualWieldingEntityPredicate implements LivingEntityPredicate {
   @Nonnull
   private ItemStackPredicate weaponCondition;

   public DualWieldingEntityPredicate(@Nonnull ItemStackPredicate weaponCondition) {
      this.weaponCondition = weaponCondition;
   }

   public boolean test(LivingEntity living) {
      return PlayerHelper.getItemsInHands(living).allMatch(this.weaponCondition);
   }

   @Override
   public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
      String key = this.getDescriptionId();
      Component targetDescription = Component.translatable("%s.target.%s".formatted(key, target.getName()));
      Component itemDescription = this.weaponCondition.getTooltip();
      return Component.translatable(key, new Object[]{bonusTooltip, targetDescription, itemDescription});
   }

   @Override
   public LivingEntityPredicate.Serializer getSerializer() {
      return (LivingEntityPredicate.Serializer)PSTLivingConditions.DUAL_WIELDING.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingEntityPredicate> consumer) {
      this.weaponCondition.addEditorWidgets(editor, c -> {
         this.setWeaponCondition(c);
         consumer.accept(this);
      });
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DualWieldingEntityPredicate that = (DualWieldingEntityPredicate)o;
         return Objects.equals(this.weaponCondition, that.weaponCondition);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.weaponCondition);
   }

   public void setWeaponCondition(@Nonnull ItemStackPredicate weaponCondition) {
      this.weaponCondition = weaponCondition;
   }

   public static class Serializer implements LivingEntityPredicate.Serializer {
      public LivingEntityPredicate deserialize(JsonObject json) throws JsonParseException {
         return new DualWieldingEntityPredicate(SerializationHelper.deserializeItemCondition(json));
      }

      public void serialize(JsonObject json, LivingEntityPredicate condition) {
         if (condition instanceof DualWieldingEntityPredicate aCondition) {
            SerializationHelper.serializeItemCondition(json, aCondition.weaponCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(CompoundTag tag) {
         return new DualWieldingEntityPredicate(SerializationHelper.deserializeItemCondition(tag));
      }

      public CompoundTag serialize(LivingEntityPredicate condition) {
         if (condition instanceof DualWieldingEntityPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeItemCondition(tag, aCondition.weaponCondition);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LivingEntityPredicate deserialize(FriendlyByteBuf buf) {
         return new DualWieldingEntityPredicate(NetworkHelper.readItemCondition(buf));
      }

      public void serialize(FriendlyByteBuf buf, LivingEntityPredicate condition) {
         if (condition instanceof DualWieldingEntityPredicate aCondition) {
            NetworkHelper.writeItemCondition(buf, aCondition.weaponCondition);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public LivingEntityPredicate createDefaultInstance() {
         return new DualWieldingEntityPredicate(new EquipmentPredicate(EquipmentPredicate.Type.WEAPON));
      }
   }
}
