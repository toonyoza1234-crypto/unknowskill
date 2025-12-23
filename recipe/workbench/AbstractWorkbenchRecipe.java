package daripher.skilltree.recipe.workbench;

import daripher.skilltree.init.PSTRecipeTypes;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.recipe.SkillRequiringRecipe;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.player.RecipeUnlockBonus;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractWorkbenchRecipe implements Recipe<WorkbenchContainer>, SkillRequiringRecipe {
   private final Map<Ingredient, Integer> additionalIngredients;
   private final ResourceLocation id;
   private final boolean requiresPassiveSkill;

   public AbstractWorkbenchRecipe(ResourceLocation id, Map<Ingredient, Integer> ingredients, boolean requiresPassiveSkill) {
      this.additionalIngredients = ingredients;
      this.requiresPassiveSkill = requiresPassiveSkill;
      this.id = id;
   }

   public boolean matches(@NotNull WorkbenchContainer container, @NotNull Level level) {
      if (!this.isValidBaseItem(container.getBaseItem())) {
         return false;
      } else {
         return !this.canBeUsedBy(container.getPlayer()) ? false : this.hasIngredients(container, this.additionalIngredients);
      }
   }

   protected String getDescriptionId() {
      ResourceLocation id = ForgeRegistries.RECIPE_SERIALIZERS.getKey(this.getSerializer());
      return "recipe.%s.%s".formatted(id.getNamespace(), id.getPath());
   }

   public boolean canBeUsedBy(@NotNull Player player) {
      return !this.requiresPassiveSkill || this.hasRecipeLearned(player);
   }

   public abstract boolean isValidBaseItem(ItemStack var1);

   public abstract Component getShortDescription();

   public List<Component> getFullDescription() {
      return List.of(this.getShortDescription());
   }

   @NotNull
   public abstract ItemStack getResult(WorkbenchContainer var1);

   public abstract int requiredBaseItemAmount();

   public Map<Ingredient, Integer> getAdditionalIngredients() {
      return this.additionalIngredients;
   }

   protected final boolean hasRecipeLearned(@NotNull Player player) {
      return SkillBonusHandler.getSkillBonuses(player, RecipeUnlockBonus.class).stream().map(RecipeUnlockBonus::getRecipeId).anyMatch(this.getId()::equals);
   }

   protected boolean hasIngredients(@NotNull WorkbenchContainer container, Map<Ingredient, Integer> ingredients) {
      return container.hasIngredients(ingredients);
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width == 7 && height == 1;
   }

   @NotNull
   public ResourceLocation getId() {
      return this.id;
   }

   @Deprecated
   @NotNull
   public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public RecipeType<?> getType() {
      return PSTRecipeTypes.WORKBENCH;
   }

   @Override
   public boolean requiresPassiveSkill() {
      return this.requiresPassiveSkill;
   }
}
