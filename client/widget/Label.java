package daripher.skilltree.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class Label extends AbstractWidget {
   public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("skilltree:textures/screen/widgets.png");
   private boolean hasBackground;

   public Label(int x, int y, Component message) {
      super(x, y, 0, 14, message);
   }

   public Label(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
      this.setHasBackground(true);
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int m, int pMouseY, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      Font font = minecraft.font;
      if (this.hasBackground) {
         graphics.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), 0, 14, this.width / 2, this.height);
         graphics.blit(WIDGETS_TEXTURE, this.getX() + this.width / 2, this.getY(), 256 - this.width / 2, 14, this.width / 2, this.height);
         int textColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
         graphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
      } else {
         graphics.drawString(font, this.getMessage(), this.getX(), this.getY() + 3, this.getFGColor());
      }
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }

   public void setHasBackground(boolean hasBackground) {
      this.hasBackground = hasBackground;
   }
}
