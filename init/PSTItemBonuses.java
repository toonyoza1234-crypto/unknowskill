package daripher.skilltree.init;

import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.ItemBonusListItemBonus;
import daripher.skilltree.skill.bonus.item.SkillBonusItemBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PSTItemBonuses {
   public static final ResourceLocation REGISTRY_ID = new ResourceLocation("skilltree", "item_bonuses");
   public static final DeferredRegister<ItemBonus.Serializer> REGISTRY = DeferredRegister.create(REGISTRY_ID, "skilltree");
   public static final RegistryObject<ItemBonus.Serializer> SKILL_BONUS = REGISTRY.register("skill_bonus", SkillBonusItemBonus.Serializer::new);
   public static final RegistryObject<ItemBonus.Serializer> ITEM_BONUS_LIST = REGISTRY.register("item_bonus_list", ItemBonusListItemBonus.Serializer::new);
}
