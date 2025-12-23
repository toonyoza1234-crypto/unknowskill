package daripher.skilltree.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import daripher.skilltree.init.PSTRecipeSerializers;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchCraftingRecipeBuilder {
   private final ResourceLocation id;
   private final Map<Ingredient, Integer> ingredients = new HashMap<>();
   @Nullable
   private Pair<Ingredient, Integer> baseIngredient;
   private boolean requiresPassiveSkill;
   private ItemStack result;

   private WorkbenchCraftingRecipeBuilder(ResourceLocation id) {
      this.id = id;
   }

   public static WorkbenchCraftingRecipeBuilder create(ResourceLocation id) {
      return new WorkbenchCraftingRecipeBuilder(id);
   }

   public WorkbenchCraftingRecipeBuilder setBaseIngredient(Ingredient ingredient, int requiredAmount) {
      this.baseIngredient = Pair.of(ingredient, requiredAmount);
      return this;
   }

   public WorkbenchCraftingRecipeBuilder addIngredients(Ingredient ingredient, int requiredAmount) {
      this.ingredients.put(ingredient, requiredAmount);
      return this;
   }

   public WorkbenchCraftingRecipeBuilder setRequiresPassiveSkill() {
      this.requiresPassiveSkill = true;
      return this;
   }

   public WorkbenchCraftingRecipeBuilder setResult(@NotNull ItemStack result) {
      this.result = result;
      return this;
   }

   public void save(Consumer<FinishedRecipe> finishedRecipeConsumer) {
      this.validate();
      finishedRecipeConsumer.accept(
         new WorkbenchCraftingRecipeBuilder.Result(this.id, this.baseIngredient, this.ingredients, this.requiresPassiveSkill, this.result)
      );
   }

   private void validate() {
      if (this.ingredients.isEmpty()) {
         throw new IllegalStateException("No ingredients set for recipe " + this.id);
      } else if (this.ingredients.size() > 6) {
         throw new IllegalStateException("Too many ingredients set for recipe " + this.id);
      } else if (this.result == null) {
         throw new IllegalStateException("No result item set for recipe " + this.id);
      }
   }

   private static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final Map<Ingredient, Integer> ingredients;
      @Nullable
      private final Pair<Ingredient, Integer> baseIngredient;
      private final boolean requiresPassiveSkill;
      private final ItemStack result;

      private Result(
         ResourceLocation id,
         @Nullable Pair<Ingredient, Integer> baseIngredient,
         Map<Ingredient, Integer> ingredients,
         boolean requiresPassiveSkill,
         ItemStack result
      ) {
         this.id = id;
         this.baseIngredient = baseIngredient;
         this.ingredients = ingredients;
         this.requiresPassiveSkill = requiresPassiveSkill;
         this.result = result;
      }

      public void serializeRecipeData(@NotNull JsonObject jsonObject) {
         JsonArray ingredientsJson = new JsonArray();
         this.ingredients.forEach((ingredient, requiredAmount) -> {
            JsonObject ingredientJson = new JsonObject();
            ingredientJson.add("ingredient", ingredient.toJson());
            ingredientJson.addProperty("required_amount", requiredAmount);
            ingredientsJson.add(ingredientJson);
         });
         jsonObject.addProperty("requires_passive_skill", this.requiresPassiveSkill);
         jsonObject.add("ingredients", ingredientsJson);
         if (this.baseIngredient != null) {
            JsonObject baseIngredientJson = new JsonObject();
            baseIngredientJson.add("ingredient", ((Ingredient)this.baseIngredient.getLeft()).toJson());
            baseIngredientJson.addProperty("required_amount", (Number)this.baseIngredient.getRight());
            jsonObject.add("base_ingredient", baseIngredientJson);
         }

         JsonObject resultJson = new JsonObject();
         Item resultItem = this.result.getItem();
         ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(resultItem));
         resultJson.addProperty("item", itemId.toString());
         if (this.result.getCount() > 1) {
            resultJson.addProperty("count", this.result.getCount());
         }

         if (this.result.getTag() != null) {
            resultJson.addProperty("nbt", this.result.getTag().toString());
         }

         jsonObject.add("result", resultJson);
      }

      @NotNull
      public ResourceLocation getId() {
         return this.id;
      }

      @NotNull
      public RecipeSerializer<?> getType() {
         return (RecipeSerializer<?>)PSTRecipeSerializers.WORKBENCH_CRAFTING.get();
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
