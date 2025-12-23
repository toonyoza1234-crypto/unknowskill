package daripher.skilltree.skill.bonus.item;

import com.google.common.collect.ImmutableList;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.player.AttributeBonus;
import daripher.skilltree.skill.bonus.player.MoreItemBonusesBonus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "skilltree",
   bus = Bus.FORGE
)
public class ItemBonusHandler {
   @SubscribeEvent
   public static void addItemBonusTooltips(ItemTooltipEvent event) {
      List<Component> components = event.getToolTip();
      List<ItemBonus<?>> itemBonuses = getItemBonuses(event.getItemStack());
      if (!itemBonuses.isEmpty()) {
         components.add(Component.empty());
      }

      for (ItemBonus<?> itemBonus : itemBonuses) {
         Style style = TooltipHelper.getItemBonusStyle();
         itemBonus.addTooltip(tooltip -> components.add(tooltip.withStyle(style)));
      }
   }

   @SubscribeEvent
   public static void addCraftedItemAttributeBonuses(LivingEquipmentChangeEvent event) {
      LivingEntity entity = event.getEntity();
      if (entity instanceof Player) {
         for (ItemBonus<?> itemBonus : getItemBonuses(event.getFrom(), SkillBonusItemBonus.class)) {
            SkillBonusItemBonus bonus = (SkillBonusItemBonus)itemBonus;
            SkillBonus attributeInstance = bonus.skillBonus();
            if (attributeInstance instanceof AttributeBonus) {
               AttributeBonus attributeBonus = (AttributeBonus)attributeInstance;
               AttributeInstance attributeInstancex = entity.getAttribute(attributeBonus.getAttribute());
               if (attributeInstancex != null) {
                  attributeInstancex.removeModifier(attributeBonus.getModifier().getId());
               }
            }
         }

         for (ItemBonus<?> itemBonusx : getItemBonuses(event.getTo(), SkillBonusItemBonus.class)) {
            SkillBonusItemBonus bonus = (SkillBonusItemBonus)itemBonusx;
            SkillBonus var12 = bonus.skillBonus();
            if (var12 instanceof AttributeBonus) {
               AttributeBonus attributeBonus = (AttributeBonus)var12;
               if (!attributeBonus.isDynamic()) {
                  AttributeInstance attributeInstance = entity.getAttribute(attributeBonus.getAttribute());
                  if (attributeInstance != null && !attributeInstance.hasModifier(attributeBonus.getModifier())) {
                     attributeInstance.addTransientModifier(attributeBonus.getModifier());
                  }
               }
            }
         }
      }
   }

   public static List<ItemBonus<?>> getItemBonuses(ItemStack stack) {
      if (!stack.hasTag()) {
         return ImmutableList.of();
      } else {
         List<ItemBonus<?>> list = new ArrayList<>();
         CompoundTag stackTag = stack.getOrCreateTag();
         CompoundTag bonusesTag = stackTag.getCompound("SkillBonuses");

         for (int i = 0; bonusesTag.contains(i + ""); i++) {
            CompoundTag itemBonusTag = bonusesTag.getCompound(i + "");
            list.add(deserializeBonus(itemBonusTag));
         }

         return list;
      }
   }

   public static List<ItemBonus<?>> getItemBonuses(ItemStack stack, Class<?> type) {
      List<ItemBonus<?>> bonuses = new ArrayList<>();

      for (ItemBonus<?> bonus : getItemBonuses(stack)) {
         if (bonus instanceof ItemBonusListItemBonus listBonus) {
            bonuses.addAll(listBonus.innerBonuses());
         } else {
            bonuses.add(bonus);
         }
      }

      return bonuses.stream().filter(type::isInstance).toList();
   }

   public static void setItemBonuses(ItemStack stack, List<ItemBonus<?>> bonuses) {
      CompoundTag bonusesTag = new CompoundTag();
      int i = 0;

      for (ItemBonus<?> itemBonus : bonuses) {
         CompoundTag bonusTag = serializeBonus((ItemBonus<? extends ItemBonus<?>>)itemBonus);
         bonusesTag.put(i + "", bonusTag);
         i++;
      }

      stack.getOrCreateTag().put("SkillBonuses", bonusesTag);
   }

   public static void removeItemBonuses(ItemStack stack) {
      if (stack.hasTag()) {
         stack.getOrCreateTag().remove("SkillBonuses");
      }
   }

   private static CompoundTag serializeBonus(ItemBonus<? extends ItemBonus<?>> bonus) {
      ItemBonus.Serializer serializer = bonus.getSerializer();
      CompoundTag bonusTag = serializer.serialize(bonus);
      ResourceLocation id = PSTRegistries.ITEM_BONUSES.get().getKey(serializer);
      bonusTag.putString("type", Objects.requireNonNull(id).toString());
      return bonusTag;
   }

   private static ItemBonus<?> deserializeBonus(CompoundTag tag) {
      if (!tag.contains("type")) {
         return null;
      } else {
         ResourceLocation id = new ResourceLocation(tag.getString("type"));
         ItemBonus.Serializer serializer = (ItemBonus.Serializer)PSTRegistries.ITEM_BONUSES.get().getValue(id);
         if (serializer == null) {
            return null;
         } else {
            try {
               return (ItemBonus<?>)serializer.deserialize(tag);
            } catch (Exception var4) {
               var4.printStackTrace();
               return null;
            }
         }
      }
   }

   public static int getCraftedBonusLimit(ItemStack itemStack, @Nullable Player player) {
      int limit = 1;
      if (player != null) {
         limit += SkillBonusHandler.getSkillBonuses(player, MoreItemBonusesBonus.class)
            .stream()
            .filter(bonus -> bonus.getItemCondition().test(itemStack))
            .map(MoreItemBonusesBonus::getAmount)
            .reduce(Integer::sum)
            .orElse(0);
      }

      return limit;
   }
}
