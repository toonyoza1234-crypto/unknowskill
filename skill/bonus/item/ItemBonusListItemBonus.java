package daripher.skilltree.skill.bonus.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.player.AttributeBonus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public record ItemBonusListItemBonus(List<? extends ItemBonus<?>> innerBonuses) implements ItemBonus<ItemBonusListItemBonus> {
   @Override
   public boolean canMerge(ItemBonus<?> other) {
      if (other instanceof ItemBonusListItemBonus otherBonus) {
         if (otherBonus.innerBonuses.size() != this.innerBonuses.size()) {
            return false;
         } else {
            for (int i = 0; i < this.innerBonuses.size(); i++) {
               if (!this.innerBonuses.get(i).canMerge((ItemBonus<?>)otherBonus.innerBonuses.get(i))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public ItemBonusListItemBonus merge(ItemBonus<?> other) {
      if (other instanceof ItemBonusListItemBonus otherBonus) {
         if (otherBonus.innerBonuses.size() != this.innerBonuses.size()) {
            throw new IllegalArgumentException();
         } else {
            List<ItemBonus<?>> mergedSkillBonuses = new ArrayList<>();

            for (int i = 0; i < this.innerBonuses.size(); i++) {
               if (!this.innerBonuses.get(i).canMerge((ItemBonus<?>)otherBonus.innerBonuses.get(i))) {
                  throw new IllegalArgumentException();
               }

               mergedSkillBonuses.add(this.innerBonuses.get(i).merge((ItemBonus<?>)otherBonus.innerBonuses.get(i)));
            }

            return new ItemBonusListItemBonus(mergedSkillBonuses);
         }
      } else {
         throw new IllegalArgumentException();
      }
   }

   public ItemBonusListItemBonus copy() {
      return new ItemBonusListItemBonus(this.innerBonuses.stream().map(ItemBonus::copy).toList());
   }

   public ItemBonusListItemBonus multiply(double multiplier) {
      this.innerBonuses.forEach(bonus -> bonus.multiply(multiplier));
      return this;
   }

   @Override
   public ItemBonus.Serializer getSerializer() {
      return (ItemBonus.Serializer)PSTItemBonuses.ITEM_BONUS_LIST.get();
   }

   @Override
   public void addTooltip(Consumer<MutableComponent> consumer) {
      for (ItemBonus<?> itemBonus : this.innerBonuses) {
         itemBonus.addTooltip(consumer);
      }
   }

   @Override
   public boolean isPositive() {
      return this.innerBonuses.stream().anyMatch(ItemBonus::isPositive);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemBonusListItemBonus that = (ItemBonusListItemBonus)o;
         return Objects.equals(this.innerBonuses, that.innerBonuses);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.innerBonuses);
   }

   public static class Serializer implements ItemBonus.Serializer {
      public ItemBonus<?> deserialize(JsonObject json) throws JsonParseException {
         JsonArray innerBonusesJson = json.get("inner_bonuses").getAsJsonArray();
         List<ItemBonus<?>> innerBonuses = new ArrayList<>();

         for (int i = 0; i < innerBonusesJson.size(); i++) {
            JsonObject innerBonusTag = innerBonusesJson.get(i).getAsJsonObject();
            String serializerIdString = innerBonusTag.get("type").getAsString();
            ResourceLocation serializerId = new ResourceLocation(serializerIdString);
            ItemBonus.Serializer serializer = (ItemBonus.Serializer)PSTRegistries.ITEM_BONUSES.get().getValue(serializerId);
            Objects.requireNonNull(serializer, "Unknown item bonus: " + serializerId);
            ItemBonus<?> innerBonus = (ItemBonus<?>)serializer.deserialize(innerBonusTag);
            innerBonuses.add(innerBonus);
         }

         return new ItemBonusListItemBonus(innerBonuses);
      }

      public void serialize(JsonObject json, ItemBonus<?> bonus) {
         if (!(bonus instanceof ItemBonusListItemBonus aBonus)) {
            throw new IllegalArgumentException();
         } else {
            JsonArray innerBonusesJson = new JsonArray();

            for (int i = 0; i < aBonus.innerBonuses.size(); i++) {
               ItemBonus<?> innerBonus = (ItemBonus<?>)aBonus.innerBonuses.get(i);
               ItemBonus.Serializer serializer = innerBonus.getSerializer();
               ResourceLocation serializerId = PSTRegistries.ITEM_BONUSES.get().getKey(serializer);
               Objects.requireNonNull(serializerId);
               JsonObject innerBonusJson = new JsonObject();
               innerBonusJson.addProperty("type", serializerId.toString());
               serializer.serialize(innerBonusJson, innerBonus);
               innerBonusesJson.add(innerBonusJson);
            }

            json.add("inner_bonuses", innerBonusesJson);
         }
      }

      public ItemBonus<?> deserialize(CompoundTag tag) {
         List<ItemBonus<?>> innerBonuses = new ArrayList<>();

         for (Tag value : tag.getList("inner_bonuses", 10)) {
            CompoundTag innerBonusTag = (CompoundTag)value;
            String type = innerBonusTag.getString("type");
            ResourceLocation serializerId = new ResourceLocation(type);
            ItemBonus.Serializer serializer = (ItemBonus.Serializer)PSTRegistries.ITEM_BONUSES.get().getValue(serializerId);
            Objects.requireNonNull(serializer, "Unknown item bonus: " + serializerId);
            innerBonuses.add((ItemBonus<?>)serializer.deserialize(innerBonusTag));
         }

         return new ItemBonusListItemBonus(innerBonuses);
      }

      public CompoundTag serialize(ItemBonus<?> bonus) {
         if (!(bonus instanceof ItemBonusListItemBonus aBonus)) {
            throw new IllegalArgumentException();
         } else {
            CompoundTag tag = new CompoundTag();
            ListTag innerBonusesTag = new ListTag();

            for (int i = 0; i < aBonus.innerBonuses.size(); i++) {
               ItemBonus<?> innerBonus = (ItemBonus<?>)aBonus.innerBonuses.get(i);
               ItemBonus.Serializer serializer = innerBonus.getSerializer();
               ResourceLocation serializerId = PSTRegistries.ITEM_BONUSES.get().getKey(serializer);
               Objects.requireNonNull(serializerId);
               CompoundTag innerBonusTag = serializer.serialize(innerBonus);
               innerBonusTag.putString("type", serializerId.toString());
               innerBonusesTag.add(innerBonusTag);
            }

            tag.put("inner_bonuses", innerBonusesTag);
            return tag;
         }
      }

      public ItemBonus<?> deserialize(FriendlyByteBuf buf) {
         List<ItemBonus<?>> innerBonuses = new ArrayList<>();
         int size = buf.readInt();

         for (int i = 0; i < size; i++) {
            innerBonuses.add(NetworkHelper.readItemBonus(buf));
         }

         return new ItemBonusListItemBonus(innerBonuses);
      }

      public void serialize(FriendlyByteBuf buf, ItemBonus<?> bonus) {
         if (!(bonus instanceof ItemBonusListItemBonus aBonus)) {
            throw new IllegalArgumentException();
         } else {
            buf.writeInt(aBonus.innerBonuses.size());

            for (int i = 0; i < aBonus.innerBonuses.size(); i++) {
               NetworkHelper.writeItemBonus(buf, (ItemBonus<?>)aBonus.innerBonuses.get(i));
            }
         }
      }

      @Override
      public ItemBonus<?> createDefaultInstance() {
         AttributeModifier defaultModifier = new AttributeModifier("Default Modifier", 1.0, Operation.ADDITION);
         ItemBonus<?> bonus1 = new SkillBonusItemBonus(new AttributeBonus(Attributes.ARMOR, defaultModifier));
         ItemBonus<?> bonus2 = new SkillBonusItemBonus(new AttributeBonus(Attributes.ARMOR_TOUGHNESS, defaultModifier));
         return new ItemBonusListItemBonus(List.of(bonus1, bonus2));
      }
   }
}
