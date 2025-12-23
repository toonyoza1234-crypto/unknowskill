package daripher.skilltree.data.generation;

import daripher.skilltree.init.PSTItems;
import daripher.skilltree.recipe.builder.WorkbenchCraftingRecipeBuilder;
import daripher.skilltree.recipe.builder.WorkbenchItemBonusRecipeBuilder;
import daripher.skilltree.skill.bonus.player.AttributeBonus;
import daripher.skilltree.skill.bonus.player.DamageBonus;
import daripher.skilltree.skill.bonus.predicate.damage.ThornsDamageCondition;
import daripher.skilltree.skill.bonus.predicate.item.EquipmentPredicate;
import java.util.function.Consumer;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags.Items;
import org.jetbrains.annotations.NotNull;

public class PSTRecipesProvider extends RecipeProvider {
   public PSTRecipesProvider(DataGenerator dataGenerator) {
      super(dataGenerator.getPackOutput());
   }

   protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
      addCraftingTableRecipes(consumer);
      addUpgradeRecipes(consumer);
      addWorkbenchCraftingRecipes(consumer);
   }

   private static void addCraftingTableRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, (ItemLike)PSTItems.WORKBENCH.get())
         .define('I', Items.INGOTS_IRON)
         .define('G', Items.INGOTS_GOLD)
         .define('C', Items.INGOTS_COPPER)
         .define('#', net.minecraft.world.item.Items.SMITHING_TABLE)
         .pattern("III")
         .pattern("G#G")
         .pattern("CCC")
         .unlockedBy(getHasName(net.minecraft.world.item.Items.SMITHING_TABLE), has(net.minecraft.world.item.Items.SMITHING_TABLE))
         .save(consumer);
   }

   private static void addUpgradeRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("shields_thorns_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.SHIELD))
         .addIngredients(Ingredient.of(Items.INGOTS_IRON), 5)
         .addIngredients(Ingredient.of(Items.NUGGETS_IRON), 9)
         .setItemBonus(new DamageBonus(0.25F, Operation.MULTIPLY_BASE).setDamageCondition(new ThornsDamageCondition()))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("armor_defence_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.ARMOR))
         .addIngredients(Ingredient.of(Items.INGOTS_COPPER), 2)
         .setItemBonus(new AttributeBonus(Attributes.ARMOR, new AttributeModifier("Workbench Upgrade", 1.0, Operation.ADDITION)))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("shields_defence_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.SHIELD))
         .addIngredients(Ingredient.of(Items.INGOTS_COPPER), 2)
         .setItemBonus(new AttributeBonus(Attributes.ARMOR, new AttributeModifier("Workbench Upgrade", 2.0, Operation.ADDITION)))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("melee_weapon_attack_speed_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.MELEE_WEAPON))
         .addIngredients(Ingredient.of(Items.LEATHER), 3)
         .addIngredients(Ingredient.of(Items.NUGGETS_IRON), 5)
         .setItemBonus(new AttributeBonus(Attributes.ATTACK_SPEED, new AttributeModifier("Workbench Upgrade", 0.1, Operation.MULTIPLY_BASE)))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("boots_movement_speed_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.BOOTS))
         .addIngredients(Ingredient.of(Items.LEATHER), 4)
         .addIngredients(Ingredient.of(Items.NUGGETS_IRON), 3)
         .setItemBonus(new AttributeBonus(Attributes.MOVEMENT_SPEED, new AttributeModifier("Workbench Upgrade", 0.15, Operation.MULTIPLY_BASE)))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("melee_weapon_attack_damage_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.MELEE_WEAPON))
         .addIngredients(Ingredient.of(Items.GEMS_DIAMOND), 1)
         .setItemBonus(new AttributeBonus(Attributes.ATTACK_DAMAGE, new AttributeModifier("Workbench Upgrade", 1.0, Operation.ADDITION)))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchItemBonusRecipeBuilder.create(modRecipeId("chestplates_toughness_bonus"))
         .setBaseItemCondition(new EquipmentPredicate(EquipmentPredicate.Type.CHESTPLATE))
         .addIngredients(Ingredient.of(Items.ORES_NETHERITE_SCRAP), 2)
         .addIngredients(Ingredient.of(Items.GEMS_DIAMOND), 3)
         .setItemBonus(new AttributeBonus(Attributes.ARMOR_TOUGHNESS, new AttributeModifier("Workbench Upgrade", 1.0, Operation.ADDITION)))
         .setRequiresPassiveSkill()
         .save(consumer);
   }

   private static void addWorkbenchCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
      WorkbenchCraftingRecipeBuilder.create(modRecipeId("saddle_crafting"))
         .addIngredients(Ingredient.of(Items.LEATHER), 8)
         .addIngredients(Ingredient.of(Items.NUGGETS_IRON), 4)
         .addIngredients(Ingredient.of(Items.INGOTS_IRON), 2)
         .setResult(new ItemStack(net.minecraft.world.item.Items.SADDLE))
         .setRequiresPassiveSkill()
         .save(consumer);
      WorkbenchCraftingRecipeBuilder.create(modRecipeId("name_tag_crafting"))
         .addIngredients(Ingredient.of(new ItemLike[]{net.minecraft.world.item.Items.PAPER}), 8)
         .addIngredients(Ingredient.of(Items.NUGGETS_IRON), 2)
         .addIngredients(Ingredient.of(Items.INGOTS_IRON), 1)
         .setResult(new ItemStack(net.minecraft.world.item.Items.NAME_TAG))
         .setRequiresPassiveSkill()
         .save(consumer);
   }

   private static ResourceLocation modRecipeId(String path) {
      return new ResourceLocation("skilltree", path);
   }
}
