package daripher.skilltree.skill.bonus.predicate.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.init.PSTItemConditions;
import daripher.skilltree.init.PSTTags;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class EquipmentPredicate implements ItemStackPredicate {
   public EquipmentPredicate.Type type;

   public EquipmentPredicate(EquipmentPredicate.Type type) {
      this.type = type;
   }

   public boolean test(ItemStack stack) {
      return switch (this.type) {
         case ARMOR -> isArmor(stack);
         case AXE -> isAxe(stack);
         case BOOTS -> isBoots(stack);
         case BOW -> isBow(stack);
         case HOE -> isHoe(stack);
         case TOOL -> isTool(stack);
         case SWORD -> isSword(stack);
         case HELMET -> isHelmet(stack);
         case SHIELD -> isShield(stack);
         case SHOVEL -> isShovel(stack);
         case CHESTPLATE -> isChestplate(stack);
         case WEAPON -> isWeapon(stack);
         case CROSSBOW -> isCrossbow(stack);
         case PICKAXE -> isPickaxe(stack);
         case TRIDENT -> isTrident(stack);
         case LEGGINGS -> isLeggings(stack);
         case MELEE_WEAPON -> isMeleeWeapon(stack);
         case RANGED_WEAPON -> isRangedWeapon(stack);
         default -> isEquipment(stack);
      };
   }

   public static boolean isEquipment(ItemStack stack) {
      return isArmor(stack) || isWeapon(stack) || isShield(stack) || isTool(stack);
   }

   public static boolean isRangedWeapon(ItemStack stack) {
      return isCrossbow(stack) || isBow(stack) || stack.is(PSTTags.Items.RANGED_WEAPON);
   }

   public static boolean isMeleeWeapon(ItemStack stack) {
      return isSword(stack) || isAxe(stack) || isTrident(stack) || stack.is(PSTTags.Items.MELEE_WEAPON);
   }

   public static boolean isLeggings(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.LEGS || stack.is(Items.ARMORS_LEGGINGS);
   }

   public static boolean isTrident(ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return Objects.requireNonNull(id).toString().equals("tetra:modular_single")
         ? true
         : stack.getItem() instanceof TridentItem || stack.is(Items.TOOLS_TRIDENTS);
   }

   public static boolean isPickaxe(ItemStack stack) {
      return stack.getItem() instanceof PickaxeItem || stack.is(ItemTags.PICKAXES);
   }

   public static boolean isCrossbow(ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return Objects.requireNonNull(id).toString().equals("tetra:modular_crossbow")
         ? true
         : stack.getItem() instanceof CrossbowItem || stack.is(Items.TOOLS_CROSSBOWS);
   }

   public static boolean isWeapon(ItemStack stack) {
      return isMeleeWeapon(stack) || isRangedWeapon(stack);
   }

   public static boolean isPotion(ItemStack stack) {
      return stack.getItem() instanceof PotionItem;
   }

   public static boolean isChestplate(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST || stack.is(Items.ARMORS_CHESTPLATES);
   }

   public static boolean isShovel(ItemStack stack) {
      return stack.getItem() instanceof ShovelItem || stack.is(ItemTags.SHOVELS);
   }

   public static boolean isShield(ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return Objects.requireNonNull(id).toString().equals("tetra:modular_shield")
         ? true
         : stack.getItem() instanceof ShieldItem || stack.is(Items.TOOLS_SHIELDS);
   }

   public static boolean isHelmet(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.HEAD || stack.is(Items.ARMORS_HELMETS);
   }

   public static boolean isSword(ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return Objects.requireNonNull(id).toString().equals("tetra:modular_sword") ? true : stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS);
   }

   public static boolean isTool(ItemStack stack) {
      return stack.getItem() instanceof DiggerItem || stack.is(Items.TOOLS);
   }

   public static boolean isHoe(ItemStack stack) {
      return stack.getItem() instanceof HoeItem || stack.is(ItemTags.HOES);
   }

   public static boolean isBow(ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return Objects.requireNonNull(id).toString().equals("tetra:modular_bow") ? true : stack.getItem() instanceof BowItem || stack.is(Items.TOOLS_BOWS);
   }

   public static boolean isBoots(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.FEET || stack.is(Items.ARMORS_BOOTS);
   }

   public static boolean isAxe(ItemStack stack) {
      return stack.getItem() instanceof AxeItem || stack.is(ItemTags.AXES);
   }

   public static boolean isArmor(ItemStack stack) {
      return isHelmet(stack) || isBoots(stack) || isChestplate(stack) || isLeggings(stack);
   }

   @Override
   public String getDescriptionId() {
      return ItemStackPredicate.super.getDescriptionId() + "." + this.type.name().toLowerCase();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EquipmentPredicate that = (EquipmentPredicate)o;
         return Objects.equals(this.type, that.type);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type);
   }

   @Override
   public ItemStackPredicate.Serializer getSerializer() {
      return (ItemStackPredicate.Serializer)PSTItemConditions.EQUIPMENT_TYPE.get();
   }

   @Override
   public void addEditorWidgets(SkillTreeEditor editor, Consumer<ItemStackPredicate> consumer) {
      editor.addLabel(0, 0, "Type", ChatFormatting.GREEN);
      editor.increaseHeight(19);
      editor.addSelectionMenu(0, 0, 200, this.type)
         .setResponder(type -> this.selectEquipmentType(consumer, type))
         .setElementNameGetter(EquipmentPredicate.Type::getName);
      editor.increaseHeight(19);
   }

   private void selectEquipmentType(Consumer<ItemStackPredicate> consumer, EquipmentPredicate.Type type) {
      this.setType(type);
      consumer.accept(this);
   }

   public void setType(EquipmentPredicate.Type type) {
      this.type = type;
   }

   public static class Serializer implements ItemStackPredicate.Serializer {
      public ItemStackPredicate deserialize(JsonObject json) throws JsonParseException {
         EquipmentPredicate.Type type = EquipmentPredicate.Type.valueOf(json.get("equipment_type").getAsString().toUpperCase());
         return new EquipmentPredicate(type);
      }

      public void serialize(JsonObject json, ItemStackPredicate condition) {
         if (condition instanceof EquipmentPredicate aCondition) {
            json.addProperty("equipment_type", aCondition.type.name().toLowerCase());
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(CompoundTag tag) {
         EquipmentPredicate.Type type = EquipmentPredicate.Type.valueOf(tag.getString("equipment_type").toUpperCase());
         return new EquipmentPredicate(type);
      }

      public CompoundTag serialize(ItemStackPredicate condition) {
         if (condition instanceof EquipmentPredicate aCondition) {
            CompoundTag tag = new CompoundTag();
            tag.putString("equipment_type", aCondition.type.name().toLowerCase());
            return tag;
         } else {
            throw new IllegalArgumentException();
         }
      }

      public ItemStackPredicate deserialize(FriendlyByteBuf buf) {
         return new EquipmentPredicate(EquipmentPredicate.Type.values()[buf.readInt()]);
      }

      public void serialize(FriendlyByteBuf buf, ItemStackPredicate condition) {
         if (condition instanceof EquipmentPredicate aCondition) {
            buf.writeInt(aCondition.type.ordinal());
         } else {
            throw new IllegalArgumentException();
         }
      }

      @Override
      public ItemStackPredicate createDefaultInstance() {
         return new EquipmentPredicate(EquipmentPredicate.Type.ANY);
      }
   }

   public static enum Type {
      ANY,
      HELMET,
      CHESTPLATE,
      LEGGINGS,
      BOOTS,
      ARMOR,
      SHIELD,
      WEAPON,
      SWORD,
      AXE,
      TRIDENT,
      MELEE_WEAPON,
      BOW,
      CROSSBOW,
      RANGED_WEAPON,
      PICKAXE,
      HOE,
      SHOVEL,
      TOOL;

      public Component getName() {
         return Component.literal(TooltipHelper.idToName(this.name().toLowerCase()));
      }
   }
}
