package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTItemConditions;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags.Items;

public class ItemTagPredicate implements ItemStackPredicate {
   private ResourceLocation tagId;

   public ItemTagPredicate(ResourceLocation tagId) {
      this.tagId = tagId;
   }

   public boolean test(ItemStack stack) {
      return stack.is(ItemTags.create(this.tagId));
   }

   @Override
   public String getDescriptionId() {
      return "item_tag.%s".formatted(this.tagId.toString());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemTagPredicate that = (ItemTagPredicate)o;
         return Objects.equals(this.tagId, that.tagId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.tagId);
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.TAG.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      editor.addLabel(0, 0, "Tag", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addTextField(0, 0, 200, 14, this.tagId.toString())
         .setSoftFilter(ResourceLocation::isValidResourceLocation)
         .setResponder(text -> this.selectTagId(consumer, text));
      editor.increaseHeight(19);
   }

   private void selectTagId(Consumer<ItemStackPredicate> consumer, String text) {
      this.setTagId(new ResourceLocation(text));
      consumer.accept(this);
   }

   public void setTagId(ResourceLocation tagId) {
      this.tagId = tagId;
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation tagId = new ResourceLocation(json.get("tag_id").getAsString());
         return new ItemTagPredicate(tagId);
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition instanceof ItemTagPredicate aCondition) {
            json.addProperty("tag_id", aCondition.tagId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         ResourceLocation tagId = new ResourceLocation(tag.getString("tag_id"));
         return new ItemTagPredicate(tagId);
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition instanceof ItemTagPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            tag.putString("tag_id", aCondition.tagId.toString());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         ResourceLocation tagId = new ResourceLocation(buf.readUtf());
         return new ItemTagPredicate(tagId);
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition instanceof ItemTagPredicate aCondition) {
            buf.writeUtf(aCondition.tagId.toString());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new ItemTagPredicate(Items.ARMORS.location());
      }
   }
}
