package daripher.skilltree.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.SkillBonusItemBonus;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchItemBonusRecipeBuilder {
   private final ResourceLocation id;
   private ItemStackPredicate baseItemStackPredicate;
   private final Map<Ingredient, Integer> ingredients = new HashMap<>();
   private boolean requiresPassiveSkill;
   private ItemBonus<?> itemBonus;

   private WorkbenchItemBonusRecipeBuilder(ResourceLocation id) {
      this.id = id;
   }

   public static WorkbenchItemBonusRecipeBuilder create(ResourceLocation id) {
      return new WorkbenchItemBonusRecipeBuilder(id);
   }

   public WorkbenchItemBonusRecipeBuilder setBaseItemCondition(ItemStackPredicate baseItemStackPredicate) {
      this.baseItemStackPredicate = baseItemStackPredicate;
      return this;
   }

   public WorkbenchItemBonusRecipeBuilder addIngredients(Ingredient ingredient, int requiredAmount) {
      this.ingredients.put(ingredient, requiredAmount);
      return this;
   }

   public WorkbenchItemBonusRecipeBuilder setRequiresPassiveSkill() {
      this.requiresPassiveSkill = true;
      return this;
   }

   public WorkbenchItemBonusRecipeBuilder setItemBonus(ItemBonus<?> itemBonus) {
      this.itemBonus = itemBonus;
      return this;
   }

   public WorkbenchItemBonusRecipeBuilder setItemBonus(SkillBonus<?> skillBonus) {
      this.itemBonus = new SkillBonusItemBonus(skillBonus);
      return this;
   }

   public void save(Consumer<FinishedRecipe> finishedRecipeConsumer) {
      this.validate();
      finishedRecipeConsumer.accept(
         new WorkbenchItemBonusRecipeBuilder.Result(this.id, this.baseItemStackPredicate, this.ingredients, this.requiresPassiveSkill, this.itemBonus)
      );
   }

   private void validate() {
      if (this.baseItemStackPredicate == null) {
         throw new IllegalStateException("No base item condition set for recipe " + this.id);
      } else if (this.ingredients.isEmpty()) {
         throw new IllegalStateException("No ingredients set for recipe " + this.id);
      } else if (this.ingredients.size() > 6) {
         throw new IllegalStateException("Too many ingredients set for recipe " + this.id);
      } else if (this.itemBonus == null) {
         throw new IllegalStateException("No item bonus set for recipe " + this.id);
      }
   }

   private static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final ItemStackPredicate baseItemStackPredicate;
      private final Map<Ingredient, Integer> ingredients;
      private final boolean requiresPassiveSkill;
      private final ItemBonus<?> itemBonus;

      private Result(
         ResourceLocation id,
         ItemStackPredicate baseItemStackPredicate,
         Map<Ingredient, Integer> ingredients,
         boolean requiresPassiveSkill,
         ItemBonus<?> itemBonus
      ) {
         this.id = id;
         this.baseItemStackPredicate = baseItemStackPredicate;
         this.ingredients = ingredients;
         this.requiresPassiveSkill = requiresPassiveSkill;
         this.itemBonus = itemBonus;
      }

      public void serializeRecipeData(@NotNull JsonObject jsonObject) {
         JsonArray ingredientsJson = new JsonArray();
         this.ingredients.forEach((ingredient, requiredAmount) -> {
            JsonObject ingredientJson = new JsonObject();
            ingredientJson.add("ingredient", ingredient.toJson());
            ingredientJson.addProperty("required_amount", requiredAmount);
            ingredientsJson.add(ingredientJson);
         });
         SerializationHelper.serializeItemCondition(jsonObject, this.baseItemStackPredicate, "base_item_condition");
         SerializationHelper.serializeItemBonus(jsonObject, this.itemBonus);
         jsonObject.addProperty("requires_passive_skill", this.requiresPassiveSkill);
         jsonObject.add("ingredients", ingredientsJson);
      }

      @NotNull
      public ResourceLocation getId() {
         return this.id;
      }

      @NotNull
      public RecipeSerializer<?> getType() {
         return (RecipeSerializer<?>)PSTRecipeSerializers.WORKBENCH_ITEM_BONUS.get();
      }

      @Nullable
      public JsonObject serializeAdvancement() {
         return null;
      }

      @Nullable
      public ResourceLocation getAdvancementId() {
         return null;
      }
   }
}
