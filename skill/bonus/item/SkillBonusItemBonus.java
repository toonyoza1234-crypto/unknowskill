package daripher.skilltree.skill.bonus.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.player.DamageBonus;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public record SkillBonusItemBonus(SkillBonus<?> skillBonus) implements ItemBonus<SkillBonusItemBonus> {
   @Override
   public boolean canMerge(ItemBonus<?> other) {
      return other instanceof SkillBonusItemBonus otherBonus ? otherBonus.skillBonus.canMerge(this.skillBonus) : false;
   }

   public SkillBonusItemBonus merge(ItemBonus<?> other) {
      if (other instanceof SkillBonusItemBonus otherBonus) {
         return new SkillBonusItemBonus(otherBonus.skillBonus.merge(this.skillBonus));
      } else {
         throw new IllegalArgumentException();
      }
   }

   public SkillBonusItemBonus copy() {
      return new SkillBonusItemBonus(this.skillBonus.copy());
   }

   public SkillBonusItemBonus multiply(double multiplier) {
      this.skillBonus.multiply(multiplier);
      return this;
   }

   @Override
   public ItemBonus.Serializer getSerializer() {
      return (ItemBonus.Serializer)PSTItemBonuses.SKILL_BONUS.get();
   }

   @Override
   public void addTooltip(Consumer<MutableComponent> consumer) {
      consumer.accept(this.skillBonus.getTooltip());
   }

   @Override
   public boolean isPositive() {
      return this.skillBonus.isPositive();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         SkillBonusItemBonus that = (SkillBonusItemBonus)obj;
         return Objects.equals(this.skillBonus, that.skillBonus);
      } else {
         return false;
      }
   }

   public static class Serializer implements ItemBonus.Serializer {
      public ItemBonus<?> deserialize(JsonObject json) throws JsonParseException {
         return new SkillBonusItemBonus((SkillBonus<?>)SkillsReloader.GSON.fromJson(json.get("skill_bonus"), SkillBonus.class));
      }

      public void serialize(JsonObject json, ItemBonus<?> bonus) {
         if (bonus instanceof SkillBonusItemBonus aBonus) {
            JsonObject skillBonusJson = new JsonObject();
            SkillBonus skillBonus = aBonus.skillBonus;
            ResourceLocation serializerId = PSTRegistries.SKILL_BONUSES.get().getKey(skillBonus.getSerializer());
            Objects.requireNonNull(serializerId);
            skillBonusJson.addProperty("type", serializerId.toString());
            skillBonus.getSerializer().serialize(skillBonusJson, skillBonus);
            json.add("skill_bonus", skillBonusJson);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemBonus<?> deserialize(CompoundTag tag) {
         CompoundTag skillBonusTag = tag.getCompound("skill_bonus");
         String type = skillBonusTag.getString("type");
         ResourceLocation serializerId = new ResourceLocation(type);
         SkillBonus.Serializer serializer = (SkillBonus.Serializer)PSTRegistries.SKILL_BONUSES.get().getValue(serializerId);
         Objects.requireNonNull(serializer, "Unknown skill bonus: " + serializerId);
         SkillBonus<?> skillBonus = (SkillBonus<?>)serializer.deserialize(skillBonusTag);
         return new SkillBonusItemBonus(skillBonus);
      }

      public CompoundTag serialize(ItemBonus<?> bonus) {
         if (bonus instanceof SkillBonusItemBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            SkillBonus<?> skillBonus = aBonus.skillBonus();
            SkillBonus.Serializer serializer = skillBonus.getSerializer();
            ResourceLocation serializerId = PSTRegistries.SKILL_BONUSES.get().getKey(serializer);
            Objects.requireNonNull(serializerId);
            CompoundTag skillBonusTag = serializer.serialize(skillBonus);
            skillBonusTag.putString("type", serializerId.toString());
            tag.put("skill_bonus", skillBonusTag);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemBonus<?> deserialize(FriendlyByteBuf buf) {
         return new SkillBonusItemBonus(NetworkHelper.readSkillBonus(buf));
      }

      public void serialize(FriendlyByteBuf buf, ItemBonus<?> bonus) {
         if (bonus instanceof SkillBonusItemBonus aBonus) {
            NetworkHelper.writeSkillBonus(buf, aBonus.skillBonus);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemBonus<?> createDefaultInstance() {
         return new SkillBonusItemBonus(new DamageBonus(0.1F, Operation.MULTIPLY_BASE));
      }
   }
}
