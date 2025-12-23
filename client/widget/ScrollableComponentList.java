package daripher.skilltree.client.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ScrollableComponentList extends AbstractWidget {
   private final int maxHeight;
   private List<Component> components = new ArrayList<>();
   private int maxLines;
   private int scroll;

   public ScrollableComponentList(int y, int maxHeight) {
      super(0, y, 0, 0, Component.empty());
      this.maxHeight = maxHeight;
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (!this.components.isEmpty()) {
         this.renderBackground(graphics);
         this.renderText(graphics);
         this.renderScrollBar(graphics);
      }
   }

   private void renderBackground(@NotNull GuiGraphics graphics) {
      graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -587202560);
   }

   private void renderText(@NotNull GuiGraphics graphics) {
      Font font = Minecraft.getInstance().font;

      for (int i = this.scroll; i < this.maxLines + this.scroll; i++) {
         Component component = this.components.get(i);
         int x = this.getX() + 5;
         int y = this.getY() + 5 + (i - this.scroll) * (9 + 3);
         graphics.drawString(font, component, x, y, 8092645);
      }
   }

   private void renderScrollBar(@NotNull GuiGraphics graphics) {
      if (this.components.size() > this.maxLines) {
         int scrollSize = this.height * this.maxLines / this.components.size();
         int maxScroll = this.components.size() - this.maxLines;
         int scrollShift = (int)((float)(this.height - scrollSize) / (float)maxScroll * (float)this.scroll);
         int x = this.getX() + this.width - 3;
         int y = this.getY() + scrollShift;
         graphics.fill(x, this.getY(), this.getX() + this.width, this.getY() + this.height, -584965598);
         graphics.fill(x, y, this.getX() + this.width, this.getY() + scrollShift + scrollSize, -578254712);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      int maxScroll = this.components.size() - this.maxLines;
      if (amount < 0.0 && this.scroll < maxScroll) {
         this.scroll++;
      }

      if (amount > 0.0 && this.scroll > 0) {
         this.scroll--;
      }

      return true;
   }

   public void setComponents(List<Component> components) {
      this.maxLines = components.size();
      this.components = components;
      this.width = 0;
      Font font = Minecraft.getInstance().font;

      for (Component stat : components) {
         int statWidth = font.width(stat);
         if (statWidth > this.width) {
            this.width = statWidth;
         }
      }

      this.width += 14;

      for (this.height = components.size() * (9 + 3) + 10; this.height > this.maxHeight; this.maxLines--) {
         this.height -= 9 + 3;
      }
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }
}
