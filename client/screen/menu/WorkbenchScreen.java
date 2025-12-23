package daripher.skilltree.client.screen.menu;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.inventory.menu.WorkbenchMenu;
import daripher.skilltree.recipe.workbench.AbstractWorkbenchRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
   private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("skilltree", "textures/gui/container/workbench.png");
   private static final ResourceLocation RECIPES_TEXTURE = new ResourceLocation("skilltree", "textures/gui/container/workbench_recipes.png");
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int SCROLLER_FULL_HEIGHT = 90;
   private static final int RECIPES_X = 8;
   private static final int RECIPES_Y = 24;
   private static final int RECIPE_WIDTH = 143;
   private static final int RECIPE_HEIGHT = 18;
   private final List<Pair<AbstractWorkbenchRecipe, Integer>> searchedRecipes = new ArrayList<>();
   private EditBox searchBox;
   private int amountScrolled;

   public WorkbenchScreen(WorkbenchMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      this.imageHeight = 242;
      menu.setRecipeListUpdateListener(this::refreshSearchResults);
   }

   protected void init() {
      super.init();
      this.clearWidgets();
      this.searchBox = new EditBox(this.font, this.leftPos + 36, this.topPos + 9, 102, 10, Component.empty());
      this.searchBox.setMaxLength(57);
      this.searchBox.setBordered(false);
      this.searchBox.setTextColor(16777215);
      this.addRenderableWidget(this.searchBox);
   }

   public void resize(@NotNull Minecraft minecraft, int width, int height) {
      String search = this.searchBox.getValue();
      this.init(minecraft, width, height);
      this.searchBox.setValue(search);
      if (!this.searchBox.getValue().isEmpty()) {
         this.refreshSearchResults();
      }
   }

   protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      this.renderBackground(guiGraphics);
      guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.renderScroll(guiGraphics);
      this.renderRecipes(guiGraphics, (double)mouseX, (double)mouseY);
      if (!this.searchBox.isFocused()) {
         Component searchHint = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC);
         guiGraphics.drawString(this.font, searchHint, this.searchBox.getX(), this.searchBox.getY(), 5592405, false);
      }
   }

   private void renderScroll(@NotNull GuiGraphics guiGraphics) {
      int scrollerIconIndex = this.isScrollBarActive() ? 2 : 1;
      int scrollerX = this.leftPos + 156;
      float scrollOffset = (float)this.amountScrolled / (float)this.getMaxScroll();
      int scrollerY = (int)((float)(this.topPos + 24) + 75.0F * scrollOffset);
      guiGraphics.blit(BACKGROUND_TEXTURE, scrollerX, scrollerY, -12 * scrollerIconIndex, 0, 12, 15);
   }

   private void renderRecipes(GuiGraphics guiGraphics, double mouseX, double mouseY) {
      int x = this.leftPos + 8;

      for (int i = 0; i < Math.min(5, this.searchedRecipes.size()); i++) {
         int recipeIndex = (Integer)this.getRecipeInSlot(i).getValue();
         int y = this.topPos + 24 + i * 18;
         int recipeTexture = this.getRecipeTexture(mouseX, mouseY, recipeIndex, i);
         int vOffset = recipeTexture * 18;
         guiGraphics.blit(RECIPES_TEXTURE, x, y, 0, vOffset, 143, 18);
         AbstractWorkbenchRecipe recipe = (AbstractWorkbenchRecipe)this.getRecipeInSlot(i).getKey();
         String tooltip = recipe.getShortDescription().getString();
         tooltip = TooltipHelper.getTrimmedString(this.font, tooltip, 139);
         guiGraphics.drawString(this.font, tooltip, x + 2, y + 5, 16777215);
      }
   }

   private int getRecipeTexture(double mouseX, double mouseY, int recipeIndex, int recipeSlot) {
      if (((WorkbenchMenu)this.menu).getSelectedRecipeIndex() == recipeIndex) {
         return 1;
      } else {
         return this.isMouseOverRecipe(recipeSlot, mouseX, mouseY) ? 2 : 0;
      }
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderGhostRecipe(guiGraphics);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   private void renderGhostRecipe(GuiGraphics guiGraphics) {
      AbstractWorkbenchRecipe selectedRecipe = ((WorkbenchMenu)this.menu).getSelectedRecipe();
      if (selectedRecipe != null) {
         Objects.requireNonNull(this.minecraft);
         if (selectedRecipe.requiredBaseItemAmount() == 0) {
            guiGraphics.fill(this.leftPos + 62, this.topPos + 120, this.leftPos + 96, this.topPos + 154, 822018048);
         }

         List<Entry<Ingredient, Integer>> requiredIngredients = selectedRecipe.getAdditionalIngredients().entrySet().stream().toList();

         for (int i = 0; i < 6; i++) {
            int itemX = this.leftPos + 8 + i % 3 * 18;
            int itemY = this.topPos + 120 + i / 3 * 18;
            if (i >= requiredIngredients.size()) {
               guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 822018048);
            } else {
               ItemStack existingIngredient = ((WorkbenchMenu)this.menu).getWorkbenchContainer().getItem(i + 1);
               int requiredAmount = requiredIngredients.get(i).getValue();
               if (!existingIngredient.isEmpty()) {
                  if (existingIngredient.getCount() < requiredAmount) {
                     guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 822018048);
                  }
               } else {
                  guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 822018048);
                  Ingredient ingredient = requiredIngredients.get(i).getKey();
                  ItemStack itemStack = ingredient.getItems()[0].copy();
                  itemStack.setCount(requiredAmount);
                  this.renderMissingItem(guiGraphics, itemX, itemY, itemStack);
               }
            }
         }

         if (((WorkbenchMenu)this.menu).getResultItem().isEmpty()) {
            ItemStack resultItem = selectedRecipe.getResult(((WorkbenchMenu)this.menu).getWorkbenchContainer());
            guiGraphics.fill(this.leftPos + 134, this.topPos + 120, this.leftPos + 168, this.topPos + 154, 822018048);
            if (!resultItem.isEmpty()) {
               this.renderMissingItem(guiGraphics, this.leftPos + 143, this.topPos + 129, resultItem);
            }
         }
      } else {
         for (int ix = 0; ix < 6; ix++) {
            int itemX = this.leftPos + 8 + ix % 3 * 18;
            int itemY = this.topPos + 120 + ix / 3 * 18;
            guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 822018048);
         }
      }
   }

   protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderTooltip(guiGraphics, mouseX, mouseY);
      this.renderRecipesTooltip(guiGraphics, mouseX, mouseY);
      this.renderGhostRecipeTooltip(guiGraphics, mouseX, mouseY);
   }

   private void renderGhostRecipeTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int selectedRecipeIndex = ((WorkbenchMenu)this.menu).getSelectedRecipeIndex();
      if (selectedRecipeIndex > -1) {
         List<AbstractWorkbenchRecipe> selectedRecipes = ((WorkbenchMenu)this.menu).getSelectedRecipes();
         if (!selectedRecipes.isEmpty()) {
            AbstractWorkbenchRecipe selectedRecipe = selectedRecipes.get(selectedRecipeIndex);
            AtomicInteger slotIndex = new AtomicInteger();
            selectedRecipe.getAdditionalIngredients().forEach((ingredient, requiredAmount) -> {
               if (((WorkbenchMenu)this.menu).getWorkbenchContainer().getItem(slotIndex.get() + 1).isEmpty()) {
                  int itemX = this.leftPos + 8 + slotIndex.get() % 3 * 18;
                  int itemY = this.topPos + 120 + slotIndex.get() / 3 * 18;
                  if (this.isMouseOverArea((double)mouseX, (double)mouseY, itemX, itemY, 16, 16)) {
                     ItemStack itemStack = ingredient.getItems()[0].copy();
                     itemStack.setCount(requiredAmount);
                     this.renderItemTooltip(guiGraphics, mouseX, mouseY, itemStack);
                  }

                  slotIndex.getAndIncrement();
               }
            });
            if (((WorkbenchMenu)this.menu).getResultItem().isEmpty()
               && this.isMouseOverArea((double)mouseX, (double)mouseY, this.leftPos + 134, this.topPos + 120, 34, 34)) {
               Objects.requireNonNull(this.minecraft);
               ItemStack resultItem = selectedRecipe.getResult(((WorkbenchMenu)this.menu).getWorkbenchContainer());
               this.renderItemTooltip(guiGraphics, mouseX, mouseY, resultItem);
            }
         }
      }
   }

   private void renderRecipesTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      for (int i = 0; i < Math.min(5, this.searchedRecipes.size()); i++) {
         if (this.isMouseOverRecipe(i, (double)mouseX, (double)mouseY)) {
            AbstractWorkbenchRecipe recipe = (AbstractWorkbenchRecipe)this.getRecipeInSlot(i).getKey();
            guiGraphics.renderComponentTooltip(this.font, recipe.getFullDescription(), mouseX, mouseY);
         }
      }
   }

   private void renderItemTooltip(@NotNull GuiGraphics guiGraphics, int x, int y, ItemStack itemStack) {
      List<Component> tooltip = this.getTooltipFromContainerItem(itemStack);
      Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
      guiGraphics.renderTooltip(this.font, tooltip, tooltipImage, itemStack, x, y);
   }

   private void renderMissingItem(GuiGraphics guiGraphics, int itemX, int itemY, ItemStack itemStack) {
      guiGraphics.renderFakeItem(itemStack, itemX, itemY);
      guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), itemX, itemY, itemX + 16, itemY + 16, 822083583);
      guiGraphics.renderItemDecorations(this.font, itemStack, itemX, itemY);
   }

   protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   protected void containerTick() {
      super.containerTick();
      this.searchBox.tick();
   }

   public boolean charTyped(char codePoint, int modifiers) {
      String search = this.searchBox.getValue();
      if (this.searchBox.charTyped(codePoint, modifiers)) {
         if (!Objects.equals(search, this.searchBox.getValue())) {
            this.refreshSearchResults();
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256 && ((WorkbenchMenu)this.menu).getSelectedRecipe() != null) {
         this.selectRecipe(-1);
         return true;
      } else {
         String search = this.searchBox.getValue();
         if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(search, this.searchBox.getValue())) {
               this.refreshSearchResults();
            }

            return true;
         } else {
            return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
         }
      }
   }

   private boolean isScrollBarActive() {
      return this.searchedRecipes.size() > 5;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
         this.searchBox.setFocused(true);
         return true;
      } else {
         this.searchBox.setFocused(false);
         Objects.requireNonNull(this.minecraft);
         LocalPlayer player = this.minecraft.player;

         for (int i = 0; i < Math.min(5, this.searchedRecipes.size()); i++) {
            int recipeIndex = (Integer)this.getRecipeInSlot(i).getValue();
            if (this.isMouseOverRecipe(i, mouseX, mouseY) && ((WorkbenchMenu)this.menu).clickMenuButton(player, recipeIndex)) {
               this.selectRecipe(recipeIndex);
               return true;
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.isScrollBarActive()) {
         this.amountScrolled = (int)Mth.clamp((double)this.amountScrolled - delta, 0.0, (double)this.getMaxScroll());
      }

      return super.mouseScrolled(mouseX, mouseY, delta);
   }

   private Pair<AbstractWorkbenchRecipe, Integer> getRecipeInSlot(int slot) {
      return this.searchedRecipes.get(slot + this.amountScrolled);
   }

   private int getMaxScroll() {
      return this.searchedRecipes.size() - 5;
   }

   private void refreshSearchResults() {
      List<AbstractWorkbenchRecipe> selectedRecipes = ((WorkbenchMenu)this.menu).getSelectedRecipes();
      this.searchedRecipes.clear();

      for (int i = 0; i < selectedRecipes.size(); i++) {
         AbstractWorkbenchRecipe recipe = selectedRecipes.get(i);
         String search = this.searchBox.getValue();
         if (search.isEmpty() || recipe.getShortDescription().toString().contains(search)) {
            this.searchedRecipes.add(Pair.of(recipe, i));
         }
      }
   }

   private void selectRecipe(int index) {
      Objects.requireNonNull(this.minecraft);
      SoundManager soundManager = this.minecraft.getSoundManager();
      soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      MultiPlayerGameMode gameMode = this.minecraft.gameMode;
      Objects.requireNonNull(gameMode);
      gameMode.handleInventoryButtonClick(((WorkbenchMenu)this.menu).containerId, index);
   }

   private boolean isMouseOverRecipe(int recipeIndex, double mouseX, double mouseY) {
      int recipeX = this.leftPos + 8;
      int recipeY = this.topPos + 24 + recipeIndex * 18;
      return mouseX >= (double)recipeX && mouseY >= (double)recipeY && mouseX < (double)(recipeX + 143) && mouseY < (double)(recipeY + 18);
   }

   private boolean isMouseOverArea(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= (double)x && mouseY >= (double)y && mouseX < (double)(x + width) && mouseY < (double)(y + height);
   }
}
