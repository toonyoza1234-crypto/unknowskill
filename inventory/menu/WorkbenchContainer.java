package daripher.skilltree.inventory.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class WorkbenchContainer extends TransientCraftingContainer {
   public final WorkbenchMenu menu;

   public WorkbenchContainer(WorkbenchMenu menu) {
      super(menu, 7, 1);
      this.menu = menu;
   }

   public Player getPlayer() {
      return this.menu.getPlayer();
   }

   public ItemStack getBaseItem() {
      return this.getItem(0);
   }

   public boolean hasIngredients(Map<Ingredient, Integer> ingredients) {
      Map<Ingredient, Integer> remaining = new HashMap<>(ingredients);

      for (int i = 1; i < this.getContainerSize(); i++) {
         ItemStack item = this.getItem(i);
         if (!item.isEmpty()) {
            for (Entry<Ingredient, Integer> entry : new HashMap<>(remaining).entrySet()) {
               Ingredient ingredient = entry.getKey();
               int needed = entry.getValue();
               if (needed > 0 && ingredient.test(item)) {
                  int available = item.getCount();
                  if (available >= needed) {
                     remaining.put(ingredient, 0);
                  } else {
                     remaining.put(ingredient, needed - available);
                  }
               }
            }

            if (remaining.values().stream().allMatch(count -> count <= 0)) {
               return true;
            }
         }
      }

      return remaining.values().stream().allMatch(count -> count <= 0);
   }
}
