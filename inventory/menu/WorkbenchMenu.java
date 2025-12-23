package daripher.skilltree.inventory.menu;

import daripher.skilltree.init.PSTBlocks;
import daripher.skilltree.init.PSTMenuTypes;
import daripher.skilltree.init.PSTRecipeTypes;
import daripher.skilltree.inventory.slot.WorkbenchBaseSlot;
import daripher.skilltree.inventory.slot.WorkbenchResultSlot;
import daripher.skilltree.inventory.slot.WorkbenchSlot;
import daripher.skilltree.recipe.workbench.AbstractWorkbenchRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchMenu extends AbstractContainerMenu {
   private static final int RESULT_SLOT = 0;
   private static final int CRAFT_SLOT_START = 1;
   private static final int CRAFT_SLOT_END = 8;
   private static final int INV_SLOT_START = 8;
   private static final int INV_SLOT_END = 35;
   private static final int HOTBAR_SLOT_START = 35;
   private static final int HOTBAR_SLOT_END = 44;
   private final WorkbenchContainer workbenchContainer;
   private final ResultContainer resultSlots;
   private final ContainerLevelAccess levelAccess;
   private final Player player;
   private final DataSlot selectedRecipeIndex;
   private List<AbstractWorkbenchRecipe> selectedRecipes = new ArrayList<>();
   @NotNull
   private ItemStack prevInput = ItemStack.EMPTY;
   private final Level level;
   @Nullable
   private Runnable recipeListUpdateListener;

   public WorkbenchMenu(int containerId, Inventory playerInventory) {
      this(containerId, playerInventory, ContainerLevelAccess.NULL);
   }

   public WorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess levelAccess) {
      super((MenuType)PSTMenuTypes.ARTISAN_WORKBENCH.get(), containerId);
      this.selectedRecipeIndex = DataSlot.standalone();
      this.workbenchContainer = new WorkbenchContainer(this);
      this.resultSlots = new ResultContainer();
      this.levelAccess = levelAccess;
      this.player = playerInventory.player;
      this.level = this.player.level();
      this.addSlot(new WorkbenchResultSlot(playerInventory.player, this.workbenchContainer, this.resultSlots, 0, 143, 129));
      this.addSlot(new WorkbenchBaseSlot(this.workbenchContainer, 0, 71, 129));

      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 3; j++) {
            this.addSlot(new WorkbenchSlot(this.workbenchContainer, j + i * 3 + 1, 8 + j * 18, 120 + i * 18, j + i * 3));
         }
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 160 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 218));
      }

      this.addDataSlot(this.selectedRecipeIndex);
      this.setupRecipeList();
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
      ItemStack movedStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(slotIndex);
      if (!slot.hasItem()) {
         return movedStack;
      } else {
         ItemStack clickedStack = slot.getItem();
         movedStack = clickedStack.copy();
         if (slotIndex == 0) {
            this.levelAccess.execute((level, blockPos) -> clickedStack.getItem().onCraftedBy(clickedStack, level, player));
            if (!this.moveItemStackTo(clickedStack, 8, 44, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(clickedStack, movedStack);
         } else if (slotIndex >= 8 && slotIndex < 44) {
            if (!this.moveItemStackTo(clickedStack, 1, 8, false)) {
               if (slotIndex < 35) {
                  if (!this.moveItemStackTo(clickedStack, 35, 44, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(clickedStack, 8, 35, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.moveItemStackTo(clickedStack, 8, 44, false)) {
            return ItemStack.EMPTY;
         }

         if (clickedStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (clickedStack.getCount() == movedStack.getCount()) {
            return ItemStack.EMPTY;
         } else {
            slot.onTake(player, clickedStack);
            if (slotIndex == 0) {
               player.drop(clickedStack, false);
            }

            return movedStack;
         }
      }
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      this.levelAccess.execute((level, blockPos) -> this.clearContainer(player, this.workbenchContainer));
   }

   public boolean stillValid(@NotNull Player player) {
      return stillValid(this.levelAccess, player, (Block)PSTBlocks.WORKBENCH.get());
   }

   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (id == -1) {
         this.selectedRecipeIndex.set(id);
         this.setupRecipeList();
      }

      if (this.isValidRecipeIndex(id)) {
         this.selectedRecipeIndex.set(id);
         AbstractWorkbenchRecipe selectedRecipe = this.getSelectedRecipe();
         if (selectedRecipe != null) {
            this.updateCraftingResult(selectedRecipe);
         }
      }

      return true;
   }

   private boolean isValidRecipeIndex(int recipeIndex) {
      return recipeIndex >= 0 && recipeIndex < this.selectedRecipes.size();
   }

   public void slotsChanged(@NotNull Container container) {
      this.updateSelectedRecipe();
   }

   private void updateSelectedRecipe() {
      ItemStack input = this.workbenchContainer.getBaseItem();
      AbstractWorkbenchRecipe selectedRecipe = this.getSelectedRecipe();
      if (selectedRecipe != null) {
         if (!selectedRecipe.isValidBaseItem(input)) {
            this.setupRecipeList();
         } else {
            this.updateCraftingResult(selectedRecipe);
         }
      } else {
         if (!ItemStack.isSameItemSameTags(input, this.prevInput)) {
            this.setupRecipeList();
            this.prevInput = input.copy();
         }

         this.broadcastChanges();
         if (this.recipeListUpdateListener != null) {
            this.recipeListUpdateListener.run();
         }
      }
   }

   public void setRecipeListUpdateListener(@Nullable Runnable recipeListUpdateListener) {
      this.recipeListUpdateListener = recipeListUpdateListener;
   }

   private void updateCraftingResult(AbstractWorkbenchRecipe selectedRecipe) {
      if (!selectedRecipe.matches(this.workbenchContainer, this.level)) {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
      } else if (!this.level.isClientSide) {
         ItemStack craftResult = selectedRecipe.assemble(this.workbenchContainer, this.level.registryAccess());
         this.resultSlots.setRecipeUsed(selectedRecipe);
         this.resultSlots.setItem(0, craftResult);
      }
   }

   private void setupRecipeList() {
      this.selectedRecipeIndex.set(-1);
      this.resultSlots.setItem(0, ItemStack.EMPTY);
      this.selectedRecipes = this.level
         .getRecipeManager()
         .getAllRecipesFor(PSTRecipeTypes.WORKBENCH)
         .stream()
         .filter(this::shouldDisplayRecipe)
         .sorted(Comparator.comparing(AbstractWorkbenchRecipe::getId))
         .toList();
   }

   private boolean shouldDisplayRecipe(AbstractWorkbenchRecipe recipe) {
      return recipe.requiresPassiveSkill() && !recipe.canBeUsedBy(this.player)
         ? false
         : this.workbenchContainer.getBaseItem().isEmpty() || recipe.isValidBaseItem(this.workbenchContainer.getBaseItem());
   }

   public Player getPlayer() {
      return this.player;
   }

   public List<AbstractWorkbenchRecipe> getSelectedRecipes() {
      return this.selectedRecipes;
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public WorkbenchContainer getWorkbenchContainer() {
      return this.workbenchContainer;
   }

   public ItemStack getResultItem() {
      return this.resultSlots.getItem(0);
   }

   @Nullable
   public AbstractWorkbenchRecipe getSelectedRecipe() {
      if (this.selectedRecipes.isEmpty()) {
         return null;
      } else {
         int index = this.selectedRecipeIndex.get();
         return index < this.selectedRecipes.size() && index >= 0 ? this.selectedRecipes.get(index) : null;
      }
   }
}
