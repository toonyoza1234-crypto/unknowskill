package daripher.skilltree.inventory.slot;

import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.recipe.workbench.AbstractWorkbenchRecipe;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

public class WorkbenchResultSlot extends Slot {
   private final WorkbenchContainer workbenchContainer;
   private final Player player;
   private int removeCount;

   public WorkbenchResultSlot(Player player, WorkbenchContainer workbenchContainer, ResultContainer container, int slot, int x, int y) {
      super(container, slot, x, y);
      this.player = player;
      this.workbenchContainer = workbenchContainer;
   }

   public boolean mayPlace(@NotNull ItemStack itemStack) {
      return false;
   }

   @NotNull
   public ItemStack remove(int amount) {
      if (this.hasItem()) {
         this.removeCount = this.removeCount + Math.min(amount, this.getItem().getCount());
      }

      return super.remove(amount);
   }

   protected void onQuickCraft(@NotNull ItemStack itemStack, int amount) {
      this.removeCount += amount;
      this.checkTakeAchievements(itemStack);
      this.repeatQuickCraft();
   }

   private void repeatQuickCraft() {
      AbstractWorkbenchRecipe selectedRecipe = this.workbenchContainer.menu.getSelectedRecipe();
      if (selectedRecipe != null) {
         int additionalCrafts = -1;
         int requiredBaseItems = selectedRecipe.requiredBaseItemAmount();
         if (requiredBaseItems != 0) {
            additionalCrafts = this.workbenchContainer.getBaseItem().getCount() / requiredBaseItems;
         }

         List<Entry<Ingredient, Integer>> requiredIngredients = selectedRecipe.getAdditionalIngredients().entrySet().stream().toList();

         for (int i = 0; i < requiredIngredients.size(); i++) {
            int requiredAmount = requiredIngredients.get(i).getValue();
            int availableAmount = this.workbenchContainer.getItem(i + 1).getCount();
            int availableCrafts = availableAmount / requiredAmount;
            if (additionalCrafts == -1 || additionalCrafts > availableCrafts) {
               additionalCrafts = availableCrafts;
            }
         }

         for (int ix = 0; ix < additionalCrafts; ix++) {
            this.player.addItem(selectedRecipe.assemble(this.workbenchContainer, this.player.level().registryAccess()));
            this.consumeMaterials();
         }
      }
   }

   protected void onSwapCraft(int numItemsCrafted) {
      this.removeCount += numItemsCrafted;
   }

   protected void checkTakeAchievements(@NotNull ItemStack itemStack) {
      if (this.removeCount > 0) {
         itemStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
         ForgeEventFactory.firePlayerCraftingEvent(this.player, itemStack, this.workbenchContainer);
         this.consumeMaterials();
      }

      this.removeCount = 0;
   }

   public void onTake(@NotNull Player player, @NotNull ItemStack itemStack) {
      this.checkTakeAchievements(itemStack);
   }

   private void consumeMaterials() {
      AbstractWorkbenchRecipe selectedRecipe = this.workbenchContainer.menu.getSelectedRecipe();
      if (selectedRecipe != null) {
         if (!this.workbenchContainer.getItem(0).isEmpty()) {
            this.workbenchContainer.removeItem(0, selectedRecipe.requiredBaseItemAmount());
         }

         List<Entry<Ingredient, Integer>> requiredIngredients = selectedRecipe.getAdditionalIngredients().entrySet().stream().toList();

         for (int i = 0; i < requiredIngredients.size(); i++) {
            int requiredAmount = requiredIngredients.get(i).getValue();
            this.workbenchContainer.removeItem(i + 1, requiredAmount);
         }
      }
   }
}
