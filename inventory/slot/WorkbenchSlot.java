package daripher.skilltree.inventory.slot;

import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.recipe.workbench.AbstractWorkbenchRecipe;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class WorkbenchSlot extends Slot {
   private final WorkbenchContainer container;
   private final int ingredientIndex;

   public WorkbenchSlot(WorkbenchContainer container, int index, int x, int y, int ingredientIndex) {
      super(container, index, x, y);
      this.container = container;
      this.ingredientIndex = ingredientIndex;
   }

   public boolean mayPlace(@NotNull ItemStack itemStack) {
      AbstractWorkbenchRecipe selectedRecipe = this.container.menu.getSelectedRecipe();
      if (selectedRecipe == null) {
         return false;
      } else {
         List<Entry<Ingredient, Integer>> requiredIngredients = selectedRecipe.getAdditionalIngredients().entrySet().stream().toList();
         if (this.ingredientIndex >= requiredIngredients.size()) {
            return false;
         } else {
            Ingredient requiredIngredient = requiredIngredients.get(this.ingredientIndex).getKey();
            return requiredIngredient.test(itemStack);
         }
      }
   }
}
