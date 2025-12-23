package daripher.skilltree.skill.bonus.player;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import top.theillusivec4.curios.api.CuriosApi;

public final class CurioSlotsBonus implements SkillBonus<CurioSlotsBonus> {
   private String slotName;
   private int amount;
   private final UUID modifierId;

   public CurioSlotsBonus(String slotName, int amount) {
      this.slotName = slotName;
      this.amount = amount;
      this.modifierId = UUID.randomUUID();
   }

   private CurioSlotsBonus(String slotName, int amount, UUID modifierId) {
      this.slotName = slotName;
      this.amount = amount;
      this.modifierId = modifierId;
   }

   @Override
   public void onSkillLearned(ServerPlayer player, boolean firstTime) {
      if (firstTime) {
         CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            HashMultimap<String, AttributeModifier> modifiers = HashMultimap.create();
            AttributeModifier modifier = new AttributeModifier(this.modifierId, "SkillBonus", (double)this.amount, Operation.ADDITION);
            modifiers.put(this.slotName, modifier);
            inv.addPermanentSlotModifiers(modifiers);
         });
      }
   }

   @Override
   public void onSkillRemoved(ServerPlayer player) {
      CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
         HashMultimap<String, AttributeModifier> modifiers = HashMultimap.create();
         AttributeModifier modifier = new AttributeModifier(this.modifierId, "SkillBonus", (double)this.amount, Operation.ADDITION);
         modifiers.put(this.slotName, modifier);
         inv.removeSlotModifiers(modifiers);
      });
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.CURIO_SLOTS.get();
   }

   public CurioSlotsBonus copy() {
      return new CurioSlotsBonus(this.slotName, this.amount, this.modifierId);
   }

   public CurioSlotsBonus multiply(double multiplier) {
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return false;
   }

   @Override
   public SkillBonus<CurioSlotsBonus> merge(SkillBonus<?> other) {
      throw new UnsupportedOperationException();
   }

   @Override
   public MutableComponent getTooltip() {
      Component slotDescription;
      if (Math.abs(this.amount) > 1) {
         slotDescription = TooltipHelper.getSlotTooltip(this.slotName, "plural");
      } else {
         slotDescription = TooltipHelper.getSlotTooltip(this.slotName);
      }

      MutableComponent tooltip = TooltipHelper.getSkillBonusTooltip(slotDescription, (double)this.amount, Operation.ADDITION);
      return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.amount > 0;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<CurioSlotsBonus> consumer) {
      editor.addLabel(0, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.amount).setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, CuriosApi.getSlots().keySet())
         .setValue(this.slotName)
         .setElementNameGetter(TooltipHelper::getSlotTooltip)
         .setResponder(value -> this.selectSlotName(consumer, value));
      editor.increaseHeight(19);
   }

   private void selectSlotName(Consumer<CurioSlotsBonus> consumer, String slotName) {
      this.setSlotName(slotName);
      consumer.accept(this.copy());
   }

   private void selectAmount(Consumer<CurioSlotsBonus> consumer, Double value) {
      this.setAmount(value.intValue());
      consumer.accept(this.copy());
   }

   public SkillBonus<?> setSlotName(String slotName) {
      this.slotName = slotName;
      return this;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public CurioSlotsBonus deserialize(JsonObject json) throws JsonParseException {
         String slotName = SerializationHelper.getElement(json, "slot").getAsString();
         int amount = SerializationHelper.getElement(json, "amount").getAsInt();
         String uuid = SerializationHelper.getElement(json, "modifier_id").getAsString();
         return new CurioSlotsBonus(slotName, amount, UUID.fromString(uuid));
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof CurioSlotsBonus aBonus) {
            json.addProperty("slot", aBonus.slotName);
            json.addProperty("amount", aBonus.amount);
            json.addProperty("modifier_id", aBonus.modifierId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public CurioSlotsBonus deserialize(CompoundTag tag) {
         String slotName = tag.getString("slot");
         int amount = tag.getInt("amount");
         String uuid = tag.getString("modifier_id");
         return new CurioSlotsBonus(slotName, amount, UUID.fromString(uuid));
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof CurioSlotsBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putString("slot", aBonus.slotName);
            tag.putInt("amount", aBonus.amount);
            tag.putString("modifier_id", aBonus.modifierId.toString());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public CurioSlotsBonus deserialize(FriendlyByteBuf buf) {
         String slotName = buf.readUtf();
         int amount = buf.readInt();
         String uuid = buf.readUtf();
         return new CurioSlotsBonus(slotName, amount, UUID.fromString(uuid));
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof CurioSlotsBonus aBonus) {
            buf.writeUtf(aBonus.slotName);
            buf.writeInt(aBonus.amount);
            buf.writeUtf(aBonus.modifierId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new CurioSlotsBonus("ring", 1);
      }
   }
}
