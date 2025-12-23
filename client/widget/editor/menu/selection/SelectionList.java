package daripher.skilltree.client.widget.editor.menu.selection;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class SelectionList<T> extends AbstractButton {
   public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("skilltree:textures/screen/widgets.png");
   private Function<T, Component> nameGetter = t -> Component.literal(t.toString());
   private Consumer<T> responder = t -> {
   };
   private List<T> elementsList;
   private String search = "";
   private T selectedElement;
   protected int elementHeight;
   protected int elementWidth;
   private int rows = 1;
   private int columns = 1;
   private int maxScroll;
   private int scroll;

   public SelectionList(int x, int y, int elementWidth, int elementHeight, Collection<T> elementsList) {
      super(x, y, elementWidth, elementHeight, Component.empty());
      this.elementsList = new ArrayList<>(elementsList);
      this.elementWidth = elementWidth;
      this.elementHeight = elementHeight;
      this.setRows(Math.min(elementsList.size(), 10));
      this.setColumns(1);
   }

   public void onPress() {
      this.responder.accept(this.selectedElement);
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (this.visible) {
         RenderSystem.enableBlend();
         this.renderBackground(graphics, mouseX, mouseY);
         this.renderElements(graphics);
         this.renderScroll(graphics);
         RenderSystem.disableBlend();
      }
   }

   private void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      this.renderBackgroundLine(graphics, this.getX(), this.getY(), 42, this.width, 7);
      this.renderBackgroundLine(graphics, this.getX(), this.getY() + this.getHeight() - 7, 49, this.width, 7);
      int centerHeight = this.getHeight() - 14;

      for (int height = centerHeight; height > 0; height -= 14) {
         int centerLineY = this.getY() + 7 + centerHeight - height;
         int centerLineHeight = Math.min(14, height);
         this.renderBackgroundLine(graphics, this.getX(), centerLineY, 70, this.width, centerLineHeight);
      }

      this.renderElementHover(graphics, mouseX, mouseY);
   }

   private void renderElementHover(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      int slotX = (mouseX - this.getX() - 5) / this.elementWidth;
      int slotY = (mouseY - this.getY() - 5) / this.elementHeight;
      slotX = Math.min(this.columns - 1, Math.max(0, slotX));
      slotY = Math.min(this.rows - 1, Math.max(0, slotY));
      int x = this.getX() + 5 + this.elementWidth * slotX;
      int y = this.getY() + 5 + this.elementHeight * slotY;
      if (this.elementHeight > 14) {
         this.renderBackgroundLine(graphics, x, y, 84, this.elementWidth, 7);
         this.renderBackgroundLine(graphics, x, y + this.elementHeight - 7, 91, this.elementWidth, 7);
         this.renderBackgroundLine(graphics, x, y + 7, 98, this.elementWidth, this.elementHeight - 14);
      } else {
         this.renderBackgroundLine(graphics, x, y, 84, this.elementWidth, this.elementHeight);
      }
   }

   private void renderBackgroundLine(@NotNull GuiGraphics graphics, int x, int y, int textureOffset, int width, int height) {
      ResourceLocation texture = WIDGETS_TEXTURE;
      graphics.blit(texture, x, y, 0, textureOffset, width / 2, height);
      graphics.blit(texture, x + width / 2, y, -width / 2, textureOffset, width / 2, height);
   }

   private void renderElements(@NotNull GuiGraphics graphics) {
      List<T> displayedElements = this.getDisplayedElements();
      int elementIndex = 0;

      for (int row = 0; row < this.rows; row++) {
         for (int column = 0; column < this.columns && elementIndex + this.scroll * this.columns < displayedElements.size(); column++) {
            int x = this.getX() + 5 + column * this.elementWidth;
            int y = this.getY() + 5 + row * this.elementHeight;
            this.renderElement(graphics, elementIndex + this.scroll * this.columns, x, y);
            elementIndex++;
         }
      }
   }

   protected abstract void renderElement(@NotNull GuiGraphics var1, int var2, int var3, int var4);

   protected List<T> getDisplayedElements() {
      return !this.search.isEmpty() ? this.elementsList.stream().filter(this::shouldDisplay).toList() : this.elementsList;
   }

   private boolean shouldDisplay(T value) {
      return this.nameGetter.apply(value).getString().toLowerCase().contains(this.search);
   }

   private void renderScroll(GuiGraphics graphics) {
      if (this.maxScroll != 0) {
         int maxScrollSize = this.height - 8;
         int scrollSize = maxScrollSize / (this.maxScroll + 1);
         int x = this.getX() + this.width - 4;
         int y = this.getY() + 3 + (maxScrollSize - scrollSize) * this.scroll / Math.max(this.maxScroll, 1);
         graphics.fill(x, y, x + 1, y + scrollSize + 1, -5592406);
      }
   }

   public void onClick(double mouseX, double mouseY) {
      if (this.clicked(mouseX, mouseY)) {
         int hoveredElement = this.getHoveredElement((int)mouseX, (int)mouseY);
         List<T> displayedElements = this.getDisplayedElements();
         if (hoveredElement < displayedElements.size()) {
            this.selectedElement = displayedElements.get(hoveredElement);
            this.onPress();
         }
      }
   }

   private int getHoveredElement(int mouseX, int mouseY) {
      return (mouseX - this.getX() - 5) / this.elementWidth + ((mouseY - this.getY() - 5) / this.elementHeight + this.scroll) * this.columns;
   }

   public void mouseMoved(double mouseX, double mouseY) {
      super.mouseMoved(mouseX, mouseY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.isMouseOver(mouseX, mouseY)) {
         this.setScroll(this.scroll - Mth.sign(delta));
         return true;
      } else {
         return false;
      }
   }

   private void setScroll(int scroll) {
      this.scroll = Math.min(this.maxScroll, Math.max(0, scroll));
   }

   public SelectionList<T> setNameGetter(Function<T, Component> nameGetter) {
      this.nameGetter = nameGetter;
      this.sortValues();
      this.setScrollToSelection();
      return this;
   }

   private void sortValues() {
      this.getDisplayedElements().sort(Comparator.comparing(t -> this.nameGetter.apply((T)t).getString()));
   }

   public Function<T, Component> getNameGetter() {
      return this.nameGetter;
   }

   public SelectionList<T> setResponder(Consumer<T> responder) {
      this.responder = responder;
      return this;
   }

   public T getSelectedElement() {
      return this.selectedElement;
   }

   public SelectionList<T> selectElement(T element) {
      this.selectedElement = element;
      this.setScrollToSelection();
      return this;
   }

   public SelectionList<T> setRows(int rows) {
      rows = Math.max(1, Math.min(this.elementsList.size() / this.columns, rows));
      this.rows = rows;
      this.updateSize();
      this.updateMaxScroll();
      return this;
   }

   public SelectionList<T> setColumns(int columns) {
      this.columns = Math.max(1, columns);
      this.updateSize();
      this.updateMaxScroll();
      return this;
   }

   private void updateMaxScroll() {
      this.maxScroll = (int)Math.max(Math.ceil((double)((float)this.getDisplayedElements().size() / (float)this.columns)) - (double)this.rows, 0.0);
   }

   public void setScrollToSelection() {
      this.setScroll(this.getDisplayedElements().indexOf(this.selectedElement));
   }

   public String getElementName(T element) {
      return this.nameGetter.apply(element).getString();
   }

   public String getSearchString() {
      return this.search;
   }

   public void setSearchString(String search) {
      this.search = search.toLowerCase();
      this.setScrollToSelection();
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }

   public void setElementSize(int width, int height) {
      this.elementWidth = width;
      this.elementHeight = height;
      this.updateSize();
      this.updateMaxScroll();
   }

   private void updateSize() {
      this.setHeight(this.elementHeight * this.rows + 10);
      this.setWidth(this.elementWidth * this.columns + 10);
   }

   public void setElementsList(Collection<T> elements) {
      this.elementsList = new ArrayList<>(elements);
      this.elementsList.sort(Comparator.comparing(element -> this.getNameGetter().apply((T)element).getString()));
      this.updateMaxScroll();
      this.setScrollToSelection();
   }
}
