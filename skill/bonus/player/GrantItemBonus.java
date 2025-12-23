package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class GrantItemBonus implements SkillBonus<GrantItemBonus> {
   private ResourceLocation itemId;
   private int amount;

   public GrantItemBonus(ResourceLocation itemId, int amount) {
      this.itemId = itemId;
      this.amount = amount;
   }

   @Override
   public void onSkillLearned(ServerPlayer player, boolean firstTime) {
      if (firstTime) {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(this.itemId);
         if (item == null) {
            SkillTreeEditorData.printMessage("Unknown item: " + this.itemId, ChatFormatting.DARK_RED);
            return;
         }

         int amountLeft = this.amount;

         while (amountLeft > 64) {
            amountLeft -= 64;
            player.addItem(new ItemStack(item, 64));
         }

         if (amountLeft > 0) {
            player.addItem(new ItemStack(item, amountLeft));
         }
      }
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.GRANT_ITEM.get();
   }

   public GrantItemBonus copy() {
      return new GrantItemBonus(this.itemId, this.amount);
   }

   public GrantItemBonus multiply(double multiplier) {
      this.amount = (int)((double)this.amount * multiplier);
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      return other instanceof GrantItemBonus otherBonus ? Objects.equals(otherBonus.itemId, this.itemId) : false;
   }

   public GrantItemBonus merge(SkillBonus<?> other) {
      if (other instanceof GrantItemBonus otherBonus) {
         return new GrantItemBonus(this.itemId, this.amount + otherBonus.amount);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Item item = (Item)ForgeRegistries.ITEMS.getValue(this.itemId);
      if (item == null) {
         return Component.literal("Unknown item: " + this.itemId).withStyle(ChatFormatting.DARK_RED);
      } else {
         Style style = TooltipHelper.getSkillBonusStyle(this.isPositive());
         Component itemDescription = item.getDescription();
         if (this.amount > 1) {
            String amountDescription = TooltipHelper.formatNumber((double)this.amount);
            return Component.translatable(this.getDescriptionId() + ".amount", new Object[]{amountDescription, itemDescription}).withStyle(style);
         } else {
            return Component.translatable(this.getDescriptionId(), new Object[]{itemDescription}).withStyle(style);
         }
      }
   }

   @Override
   public boolean isPositive() {
      return true;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<GrantItemBonus> consumer) {
      editor.addLabel(0, 0, "Amount", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.amount)
         .setNumericFilter(v -> v > 0.0 && v % 1.0 == 0.0)
         .setNumericResponder(value -> this.selectAmount(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Item", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      List<ResourceLocation> items = ForgeRegistries.ITEMS.getEntries().stream().map(Entry::getKey).<ResourceLocation>map(ResourceKey::location).toList();
      editor.addSelectionMenu(0, 0, 200, items)
         .setValue(this.itemId)
         .setElementNameGetter(id -> Component.literal(id.toString()))
         .setResponder(id -> this.selectItemId(id, consumer));
      editor.increaseHeight(19);
   }

   private void selectItemId(ResourceLocation id, Consumer<GrantItemBonus> consumer) {
      this.setItemId(id);
      consumer.accept(this.copy());
   }

   public void setItemId(ResourceLocation itemId) {
      this.itemId = itemId;
   }

   private void selectAmount(Consumer<GrantItemBonus> consumer, Double value) {
      this.setAmount(value.intValue());
      consumer.accept(this.copy());
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public static class Serializer implements SkillBonus.Serializer {
      public GrantItemBonus deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation itemId = new ResourceLocation(json.get("item_id").getAsString());
         int amount = SerializationHelper.getElement(json, "amount").getAsInt();
         return new GrantItemBonus(itemId, amount);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof GrantItemBonus aBonus) {
            json.addProperty("item_id", aBonus.itemId.toString());
            json.addProperty("amount", aBonus.amount);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GrantItemBonus deserialize(CompoundTag tag) {
         ResourceLocation itemId = new ResourceLocation(tag.getString("item_id"));
         int amount = tag.getInt("amount");
         return new GrantItemBonus(itemId, amount);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof GrantItemBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putString("item_id", aBonus.itemId.toString());
            tag.putInt("amount", aBonus.amount);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public GrantItemBonus deserialize(FriendlyByteBuf buf) {
         ResourceLocation itemId = buf.readResourceLocation();
         int duration = buf.readInt();
         return new GrantItemBonus(itemId, duration);
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof GrantItemBonus aBonus) {
            buf.writeResourceLocation(aBonus.itemId);
            buf.writeInt(aBonus.amount);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new GrantItemBonus(ForgeRegistries.ITEMS.getKey(Items.DIAMOND), 64);
      }
   }
}
