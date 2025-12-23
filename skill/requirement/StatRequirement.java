package daripher.skilltree.skill.requirement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTSkillRequirements;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public final class StatRequirement implements SkillRequirement<StatRequirement> {
   private ResourceLocation statTypeId;
   private ResourceLocation statId;
   private int minValue;

   public StatRequirement(ResourceLocation statTypeId, ResourceLocation statId, int minValue) {
      this.statTypeId = statTypeId;
      this.statId = statId;
      this.minValue = minValue;
   }

   public boolean test(Player player) {
      StatType<?> statType = (StatType<?>)ForgeRegistries.STAT_TYPES.getValue(this.statTypeId);
      Objects.requireNonNull(statType);
      int statValue = this.getStatValue(player, statType);
      return statValue >= this.minValue;
   }

   @Override
   public MutableComponent getTooltip() {
      StatType<?> statType = (StatType<?>)ForgeRegistries.STAT_TYPES.getValue(this.statTypeId);
      if (statType == null) {
         return Component.literal("Unknown stat type: " + this.statTypeId).withStyle(ChatFormatting.RED);
      } else if (statType == Stats.CUSTOM) {
         ResourceLocation originalStatId = (ResourceLocation)Stats.CUSTOM.getRegistry().get(this.statId);
         if (originalStatId == null) {
            return Component.literal("Unknown stat: " + this.statId).withStyle(ChatFormatting.RED);
         } else {
            String statIdString = originalStatId.toString().replace(':', '.');
            Component statName = Component.translatable("stat." + statIdString);
            Stat<ResourceLocation> stat = Stats.CUSTOM.get(originalStatId);
            String formattedMinValue = stat.format(this.minValue).replace(".00", "");
            return Component.literal(statName.getString() + ": " + formattedMinValue);
         }
      } else if (statType == Stats.ENTITY_KILLED) {
         EntityType<?> entityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(this.statId);
         if (entityType == null) {
            return Component.literal("Unknown entity: " + this.statId).withStyle(ChatFormatting.RED);
         } else {
            Component entityName = entityType.getDescription();
            return Component.translatable(statType.getTranslationKey(), new Object[]{this.minValue, entityName});
         }
      } else if (statType == Stats.ENTITY_KILLED_BY) {
         EntityType<?> entityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(this.statId);
         if (entityType == null) {
            return Component.literal("Unknown entity: " + this.statId).withStyle(ChatFormatting.RED);
         } else {
            Component entityName = entityType.getDescription();
            return Component.translatable(statType.getTranslationKey(), new Object[]{entityName, this.minValue});
         }
      } else {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(this.statId);
         if (item == null) {
            return Component.literal("Unknown item: " + this.statId).withStyle(ChatFormatting.RED);
         } else {
            Component itemName = item.getDescription();
            return Component.literal(statType.getDisplayName().getString() + " " + itemName.getString() + ": " + this.minValue);
         }
      }
   }

   private <T> int getStatValue(Player player, @Nonnull StatType<T> statType) {
      StatsCounter playerStats = this.getPlayerStats(player);
      int statValue;
      if (statType == Stats.CUSTOM) {
         ResourceLocation originalStatId = (ResourceLocation)Stats.CUSTOM.getRegistry().get(this.statId);
         if (originalStatId == null) {
            return 0;
         }

         statValue = playerStats.getValue(Stats.CUSTOM, originalStatId);
      } else {
         T stat = (T)statType.getRegistry().get(this.statId);
         Objects.requireNonNull(stat);
         statValue = playerStats.getValue(statType, stat);
      }

      return statValue;
   }

   private StatsCounter getPlayerStats(Player player) {
      return (StatsCounter)(player.level().isClientSide ? getClientPlayerStats(player) : ((ServerPlayer)player).getStats());
   }

   @OnlyIn(Dist.CLIENT)
   private static StatsCounter getClientPlayerStats(Player player) {
      return ((LocalPlayer)player).getStats();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<StatRequirement> consumer) {
      editor.addLabel(0, 0, "Stat Type", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      Set<ResourceLocation> statTypeIds = ForgeRegistries.STAT_TYPES.getKeys();
      editor.addSelectionMenu(0, 0, 200, statTypeIds)
         .setValue(this.getStatTypeId())
         .setElementNameGetter(v -> Component.literal(v.toString()))
         .setResponder(v -> this.selectStatType(consumer, v));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Stat", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      StatType<?> statType = (StatType<?>)ForgeRegistries.STAT_TYPES.getValue(this.getStatTypeId());
      Objects.requireNonNull(statType);
      Set<ResourceLocation> statIds = statType.getRegistry().keySet();
      editor.addSelectionMenu(0, 0, 200, statIds)
         .setValue(this.getStatId())
         .setElementNameGetter(v -> Component.literal(v.toString()))
         .setResponder(v -> this.selectStat(consumer, v));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Min Value", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 50, 14, (double)this.minValue)
         .setNumericFilter(value -> value == (double)value.intValue())
         .setNumericResponder(value -> this.selectMinValue(consumer, value));
      editor.increaseHeight(19);
   }

   private void selectMinValue(Consumer<StatRequirement> consumer, Double value) {
      this.setMinValue(value.intValue());
      consumer.accept(this);
   }

   private void selectStat(Consumer<StatRequirement> consumer, ResourceLocation statId) {
      this.setStatId(statId);
      consumer.accept(this);
   }

   private void selectStatType(Consumer<StatRequirement> consumer, ResourceLocation statTypeId) {
      this.setStatTypeId(statTypeId);
      StatType<?> statType = (StatType<?>)ForgeRegistries.STAT_TYPES.getValue(this.getStatTypeId());
      Objects.requireNonNull(statType);
      Set<ResourceLocation> statIds = statType.getRegistry().keySet();
      statIds.stream().findFirst().ifPresent(this::setStatId);
      consumer.accept(this);
   }

   public void setStatId(ResourceLocation statId) {
      this.statId = statId;
   }

   public void setStatTypeId(ResourceLocation statTypeId) {
      this.statTypeId = statTypeId;
   }

   public void setMinValue(int minValue) {
      this.minValue = minValue;
   }

   public StatRequirement copy() {
      return new StatRequirement(this.statTypeId, this.statId, this.minValue);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         StatRequirement that = (StatRequirement)o;
         return this.minValue == that.minValue && Objects.equals(this.statTypeId, that.statTypeId) && Objects.equals(this.statId, that.statId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.statTypeId, this.statId, this.minValue);
   }

   public ResourceLocation getStatTypeId() {
      return this.statTypeId;
   }

   public ResourceLocation getStatId() {
      return this.statId;
   }

   @Override
   public SkillRequirement.Serializer getSerializer() {
      return (SkillRequirement.Serializer)PSTSkillRequirements.STAT_VALUE.get();
   }

   public static class Serializer implements SkillRequirement.Serializer {
      public SkillRequirement<?> deserialize(JsonObject json) throws JsonParseException {
         ResourceLocation statTypeId = new ResourceLocation(json.get("statTypeId").getAsString());
         ResourceLocation statId = new ResourceLocation(json.get("statId").getAsString());
         int minValue = json.get("minValue").getAsInt();
         return new StatRequirement(statTypeId, statId, minValue);
      }

      public void serialize(JsonObject json, SkillRequirement<?> requirement) {
         if (requirement instanceof StatRequirement aRequirement) {
            json.addProperty("statTypeId", aRequirement.statTypeId.toString());
            json.addProperty("statId", aRequirement.statId.toString());
            json.addProperty("minValue", aRequirement.minValue);
         }
      }

      public SkillRequirement<?> deserialize(CompoundTag tag) {
         ResourceLocation statTypeId = new ResourceLocation(tag.getString("statTypeId"));
         ResourceLocation statId = new ResourceLocation(tag.getString("statId"));
         int minValue = tag.getInt("minValue");
         return new StatRequirement(statTypeId, statId, minValue);
      }

      public CompoundTag serialize(SkillRequirement<?> requirement) {
         CompoundTag tag = new CompoundTag();
         if (requirement instanceof StatRequirement aRequirement) {
            tag.putString("statTypeId", aRequirement.statTypeId.toString());
            tag.putString("statId", aRequirement.statId.toString());
            tag.putInt("minValue", aRequirement.minValue);
         }

         return tag;
      }

      public SkillRequirement<?> deserialize(FriendlyByteBuf buf) {
         ResourceLocation statTypeId = new ResourceLocation(buf.readUtf());
         ResourceLocation statId = new ResourceLocation(buf.readUtf());
         int minValue = buf.readInt();
         return new StatRequirement(statTypeId, statId, minValue);
      }

      public void serialize(FriendlyByteBuf buf, SkillRequirement<?> requirement) {
         if (requirement instanceof StatRequirement aRequirement) {
            buf.writeUtf(aRequirement.statTypeId.toString());
            buf.writeUtf(aRequirement.statId.toString());
            buf.writeInt(aRequirement.minValue);
         }
      }

      @Override
      public SkillRequirement<?> createDefaultInstance() {
         return new StatRequirement(ForgeRegistries.STAT_TYPES.getKey(Stats.CUSTOM), Stats.DEATHS, 1);
      }
   }
}
