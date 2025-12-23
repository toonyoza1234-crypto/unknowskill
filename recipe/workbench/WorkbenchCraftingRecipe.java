package daripher.skilltree.recipe.workbench;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchCraftingRecipe extends AbstractWorkbenchRecipe {
   @Nullable
   private final Pair<Ingredient, Integer> baseIngredient;
   private final ItemStack result;

   public WorkbenchCraftingRecipe(
      ResourceLocation id,
      @Nullable Pair<Ingredient, Integer> baseIngredient,
      Map<Ingredient, Integer> ingredients,
      boolean requiresPassiveSkill,
      ItemStack result
   ) {
      super(id, ingredients, requiresPassiveSkill);
      this.result = result;
      this.baseIngredient = baseIngredient;
   }

   @NotNull
   public ItemStack assemble(@NotNull WorkbenchContainer container, @NotNull RegistryAccess registryAccess) {
      return this.getResult(container);
   }

   @Override
   public boolean isValidBaseItem(ItemStack itemStack) {
      return this.baseIngredient == null
         ? itemStack.isEmpty()
         : ((Ingredient)this.baseIngredient.getLeft()).test(itemStack) && itemStack.getCount() >= (Integer)this.baseIngredient.getRight();
   }

   @Override
   public Component getShortDescription() {
      return this.result.getHoverName();
   }

   @NotNull
   @Override
   public ItemStack getResult(WorkbenchContainer workbenchContainer) {
      return this.result.copy();
   }

   @Override
   public int requiredBaseItemAmount() {
      return this.baseIngredient == null ? 0 : (Integer)this.baseIngredient.getRight();
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)PSTRecipeSerializers.WORKBENCH_CRAFTING.get();
   }

   public static class Serializer implements RecipeSerializer<WorkbenchCraftingRecipe> {
      @NotNull
      public WorkbenchCraftingRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject jsonObject) {
         boolean requiresPassiveSkill = jsonObject.get("requires_passive_skill").getAsBoolean();
         Map<Ingredient, Integer> ingredients = new HashMap<>();

         for (JsonElement jsonElement : jsonObject.getAsJsonArray("ingredients")) {
            JsonObject ingredientJson = jsonElement.getAsJsonObject();
            Ingredient ingredient = Ingredient.fromJson(ingredientJson.get("ingredient"));
            int requiredAmount = ingredientJson.get("required_amount").getAsInt();
            ingredients.put(ingredient, requiredAmount);
         }

         Pair<Ingredient, Integer> baseIngredient = null;
         if (jsonObject.has("base_ingredient")) {
            JsonObject baseIngredientJson = jsonObject.get("base_ingredient").getAsJsonObject();
            Ingredient ingredient = Ingredient.fromJson(baseIngredientJson.get("ingredient"));
            int requiredAmount = baseIngredientJson.get("required_amount").getAsInt();
            baseIngredient = Pair.of(ingredient, requiredAmount);
         }

         JsonObject resultJson = jsonObject.getAsJsonObject("result");
         ItemStack result = CraftingHelper.getItemStack(resultJson, true, true);
         return new WorkbenchCraftingRecipe(id, baseIngredient, ingredients, requiresPassiveSkill, result);
      }

      @Nullable
      public WorkbenchCraftingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
         boolean requiresPassiveSkill = buf.readBoolean();
         Map<Ingredient, Integer> ingredients = new HashMap<>();
         int ingredientsCount = buf.readInt();

         for (int i = 0; i < ingredientsCount; i++) {
            ingredients.put(Ingredient.fromNetwork(buf), buf.readInt());
         }

         Pair<Ingredient, Integer> baseIngredient = null;
         boolean hasBaseIngredient = buf.readBoolean();
         if (hasBaseIngredient) {
            baseIngredient = Pair.of(Ingredient.fromNetwork(buf), buf.readInt());
         }

         ItemStack result = buf.readItem();
         return new WorkbenchCraftingRecipe(id, baseIngredient, ingredients, requiresPassiveSkill, result);
      }

      public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull WorkbenchCraftingRecipe recipe) {
         buf.writeBoolean(recipe.requiresPassiveSkill());
         int ingredientsCount = recipe.getAdditionalIngredients().size();
         buf.writeInt(ingredientsCount);
         recipe.getAdditionalIngredients().forEach((ingredient, requiredAmount) -> {
            ingredient.toNetwork(buf);
            buf.writeInt(requiredAmount);
         });
         buf.writeBoolean(recipe.baseIngredient != null);
         if (recipe.baseIngredient != null) {
            ((Ingredient)recipe.baseIngredient.getLeft()).toNetwork(buf);
            buf.writeInt((Integer)recipe.baseIngredient.getRight());
         }

         buf.writeItem(recipe.result);
      }
   }
}
