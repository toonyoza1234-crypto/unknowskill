package daripher.skilltree.skill.bonus.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.selection.SelectionList;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class LootDuplicationBonus implements SkillBonus<LootDuplicationBonus> {
   private LootDuplicationBonus.LootType lootType;
   private float multiplier;
   private float chance;

   public LootDuplicationBonus(float chance, float multiplier, LootDuplicationBonus.LootType lootType) {
      this.chance = chance;
      this.multiplier = multiplier;
      this.lootType = lootType;
   }

   @Override
   public SkillBonus.Serializer getSerializer() {
      return (SkillBonus.Serializer)PSTSkillBonuses.LOOT_DUPLICATION.get();
   }

   public LootDuplicationBonus copy() {
      return new LootDuplicationBonus(this.chance, this.multiplier, this.lootType);
   }

   public LootDuplicationBonus multiply(double multiplier) {
      this.chance = (float)((double)this.chance * multiplier);
      return this;
   }

   @Override
   public boolean canMerge(SkillBonus<?> other) {
      if (other instanceof LootDuplicationBonus otherBonus) {
         return otherBonus.multiplier != this.multiplier ? false : Objects.equals(otherBonus.lootType, this.lootType);
      } else {
         return false;
      }
   }

   @Override
   public SkillBonus<LootDuplicationBonus> merge(SkillBonus<?> other) {
      if (other instanceof LootDuplicationBonus otherBonus) {
         return new LootDuplicationBonus(otherBonus.chance + this.chance, this.multiplier, this.lootType);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MutableComponent getTooltip() {
      Component lootDescription = Component.translatable(this.lootType.getDescriptionId());
      String descriptionId = this.getDescriptionId();
      MutableComponent multiplierDescription;
      if (this.multiplier == 1.0F) {
         multiplierDescription = Component.translatable(descriptionId + ".double");
      } else if (this.multiplier == 2.0F) {
         multiplierDescription = Component.translatable(descriptionId + ".triple");
      } else {
         String formattedMultiplier = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)(this.multiplier * 100.0F));
         multiplierDescription = Component.translatable(descriptionId + ".multiplier", new Object[]{formattedMultiplier});
      }

      MutableComponent bonusDescription;
      if (this.chance < 1.0F) {
         bonusDescription = Component.translatable(descriptionId, new Object[]{multiplierDescription, lootDescription});
         bonusDescription = TooltipHelper.getSkillBonusTooltip(bonusDescription, (double)this.chance, Operation.MULTIPLY_BASE);
      } else {
         bonusDescription = Component.translatable(descriptionId + ".guaranteed", new Object[]{multiplierDescription, lootDescription});
      }

      return bonusDescription.withStyle(TooltipHelper.getSkillBonusStyle(this.isPositive()));
   }

   @Override
   public boolean isPositive() {
      return this.chance > 0.0F;
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, int row, Consumer<LootDuplicationBonus> consumer) {
      editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
      editor.addLabel(110, 0, "Multiplier", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor.addNumericTextField(0, 0, 90, 14, (double)this.chance).setNumericResponder(value -> this.selectChance(consumer, value));
      editor.addNumericTextField(110, 0, 90, 14, (double)this.multiplier).setNumericResponder(value -> this.selectMultiplier(consumer, value));
      editor.increaseHeight(19);
      editor.addLabel(0, 0, "Loot Type", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      SelectionList<LootDuplicationBonus.LootType> lootTypeSelection = editor.addSelection(0, 0, 190, 6, this.lootType)
         .setNameGetter(LootDuplicationBonus.LootType::getFormattedName)
         .setResponder(lootType -> this.selectLootType(consumer, lootType));
      editor.increaseHeight(lootTypeSelection.getHeight() + 10);
   }

   private void selectLootType(Consumer<LootDuplicationBonus> consumer, LootDuplicationBonus.LootType lootType) {
      this.setLootType(lootType);
      consumer.accept(this.copy());
   }

   private void selectMultiplier(Consumer<LootDuplicationBonus> consumer, Double value) {
      this.setMultiplier(value.floatValue());
      consumer.accept(this.copy());
   }

   private void selectChance(Consumer<LootDuplicationBonus> consumer, Double value) {
      this.setChance(value.floatValue());
      consumer.accept(this.copy());
   }

   public void setChance(float chance) {
      this.chance = chance;
   }

   public void setMultiplier(float multiplier) {
      this.multiplier = multiplier;
   }

   public void setLootType(LootDuplicationBonus.LootType lootType) {
      this.lootType = lootType;
   }

   public float getChance() {
      return this.chance;
   }

   public float getMultiplier() {
      return this.multiplier;
   }

   public LootDuplicationBonus.LootType getLootType() {
      return this.lootType;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LootDuplicationBonus that = (LootDuplicationBonus)o;
         if (Float.compare(this.multiplier, that.multiplier) != 0) {
            return false;
         } else {
            return Float.compare(this.chance, that.chance) != 0 ? false : this.lootType == that.lootType;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.lootType, this.multiplier, this.chance);
   }

   public static enum LootType {
      MOBS("mobs"),
      FISHING("fishing"),
      GEMS("gems"),
      CHESTS("chests"),
      ORE("ore"),
      ARCHAEOLOGY("archaeology");

      final String name;

      public boolean canAffect(LootContext lootContext) {
         LootContextParam<Entity> playerLootContextParam = this.getPlayerLootContextParam();
         if (!lootContext.hasParam(playerLootContextParam)) {
            return false;
         } else if (!(lootContext.getParam(playerLootContextParam) instanceof Player)) {
            return false;
         } else {
            ResourceLocation lootTableId = lootContext.getQueriedLootTableId();
            String lootTableName = lootTableId.toString();

            return switch (this) {
               case MOBS -> lootTableName.contains("entities/");
               case FISHING -> lootTableName.contains("fishing");
               case GEMS -> lootTableName.contains("gems");
               case CHESTS -> lootTableName.contains("chests/");
               case ORE -> lootTableName.contains("blocks/") && lootTableName.contains("_ore");
               case ARCHAEOLOGY -> lootTableName.contains("archaeology/");
            };
         }
      }

      public LootContextParam<Entity> getPlayerLootContextParam() {
         return switch (this) {
            case MOBS, FISHING -> LootContextParams.KILLER_ENTITY;
            case GEMS, CHESTS, ORE, ARCHAEOLOGY -> LootContextParams.THIS_ENTITY;
         };
      }

      private LootType(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public Component getFormattedName() {
         String firstLetter = this.getName().substring(0, 1);
         return Component.literal(firstLetter.toUpperCase() + this.getName().substring(1));
      }

      public static LootDuplicationBonus.LootType byName(String name) {
         for (LootDuplicationBonus.LootType type : values()) {
            if (type.name.equals(name)) {
               return type;
            }
         }

         return MOBS;
      }

      public String getDescriptionId() {
         return "loot.type." + this.getName();
      }
   }

   public static class Serializer implements SkillBonus.Serializer {
      public LootDuplicationBonus deserialize(JsonObject json) throws JsonParseException {
         float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
         float multiplier = SerializationHelper.getElement(json, "multiplier").getAsFloat();
         LootDuplicationBonus.LootType lootType = LootDuplicationBonus.LootType.byName(json.get("loot_type").getAsString());
         return new LootDuplicationBonus(chance, multiplier, lootType);
      }

      public void serialize(JsonObject json, SkillBonus<?> bonus) {
         if (bonus instanceof LootDuplicationBonus aBonus) {
            json.addProperty("chance", aBonus.chance);
            json.addProperty("multiplier", aBonus.multiplier);
            json.addProperty("loot_type", aBonus.lootType.name);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LootDuplicationBonus deserialize(CompoundTag tag) {
         float chance = tag.getFloat("chance");
         float multiplier = tag.getFloat("multiplier");
         LootDuplicationBonus.LootType lootType = LootDuplicationBonus.LootType.byName(tag.getString("loot_type"));
         return new LootDuplicationBonus(chance, multiplier, lootType);
      }

      public CompoundTag serialize(SkillBonus<?> bonus) {
         if (bonus instanceof LootDuplicationBonus aBonus) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("chance", aBonus.chance);
            tag.putFloat("multiplier", aBonus.multiplier);
            tag.putString("loot_type", aBonus.lootType.name);
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public LootDuplicationBonus deserialize(FriendlyByteBuf buf) {
         return new LootDuplicationBonus(buf.readFloat(), buf.readFloat(), LootDuplicationBonus.LootType.byName(buf.readUtf()));
      }

      public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
         if (bonus instanceof LootDuplicationBonus aBonus) {
            buf.writeFloat(aBonus.chance);
            buf.writeFloat(aBonus.multiplier);
            buf.writeUtf(aBonus.lootType.name);
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public SkillBonus<?> createDefaultInstance() {
         return new LootDuplicationBonus(0.05F, 1.0F, LootDuplicationBonus.LootType.MOBS);
      }
   }
}
