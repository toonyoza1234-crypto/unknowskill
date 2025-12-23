package daripher.skilltree.client.widget;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class Button extends net.minecraft.client.gui.components.Button {
   protected OnPress pressFunc = b -> {
   };

   public Button(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message, b -> {
      }, Supplier::get);
   }

   public void setPressFunc(OnPress pressFunc) {
      this.pressFunc = pressFunc;
   }

   public void onPress() {
      this.pressFunc.onPress(this);
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics);
      this.renderText(graphics);
   }

   protected void renderBackground(@NotNull GuiGraphics graphics) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/widgets.png");
      int v = this.getTextureVariant() * 14;
      graphics.blit(texture, this.getX(), this.getY(), 0, v, this.width / 2, this.height);
      graphics.blit(texture, this.getX() + this.width / 2, this.getY(), -this.width / 2, v, this.width / 2, this.height);
   }

   protected void renderText(@NotNull GuiGraphics graphics) {
      Minecraft minecraft = Minecraft.getInstance();
      Font font = minecraft.font;
      int textColor = this.getFGColor();
      textColor |= Mth.ceil(this.alpha * 255.0F) << 24;
      graphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
   }

   protected int getTextureVariant() {
      return !this.isActive() ? 0 : (this.isHoveredOrFocused() ? 2 : 1);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return false;
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.visible
         && mouseX >= (double)this.getX()
         && mouseY >= (double)this.getY()
         && mouseX < (double)(this.getX() + this.width)
         && mouseY < (double)(this.getY() + this.height);
   }
}
