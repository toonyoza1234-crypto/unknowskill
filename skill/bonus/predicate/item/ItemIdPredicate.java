package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTItemConditions;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemIdPredicate implements ItemStackPredicate {
   private ResourceLocation id;

   public ItemIdPredicate(ResourceLocation id) {
      this.id = id;
   }

   public boolean test(ItemStack stack) {
      return ForgeRegistries.ITEMS.getValue(this.id) == stack.getItem();
   }

   @Override
   public String getDescriptionId() {
      Item item = (Item)ForgeRegistries.ITEMS.getValue(this.id);
      return item != null ? item.getDescriptionId() : ItemStackPredicate.super.getDescriptionId();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemIdPredicate that = (ItemIdPredicate)o;
         return this.id.equals(that.id);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.ITEM_ID.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      editor.addLabel(0, 0, "Item Id", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addTextField(0, 0, 200, 14, this.id.toString()).setSoftFilter(ItemIdPredicate::isItemId).setResponder(text -> this.selectItemId(consumer, text));
      editor.increaseHeight(19);
   }

   private void selectItemId(Consumer<ItemStackPredicate> consumer, String text) {
      this.setId(new ResourceLocation(text));
      consumer.accept(this);
   }

   private static boolean isItemId(String text) {
      return !ResourceLocation.isValidResourceLocation(text) ? false : ForgeRegistries.ITEMS.containsKey(new ResourceLocation(text));
   }

   public void setId(ResourceLocation id) {
      this.id = id;
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
         return new ItemIdPredicate(id);
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition instanceof ItemIdPredicate aCondition) {
            json.addProperty("id", aCondition.id.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         Tag idTag = tag.get("id");
         Objects.requireNonNull(idTag);
         ResourceLocation id = new ResourceLocation(idTag.getAsString());
         return new ItemIdPredicate(id);
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition instanceof ItemIdPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", aCondition.id.toString());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return new ItemIdPredicate(new ResourceLocation(buf.readUtf()));
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition instanceof ItemIdPredicate aCondition) {
            buf.writeUtf(aCondition.id.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new ItemIdPredicate(new ResourceLocation("minecraft:shield"));
      }
   }
}
