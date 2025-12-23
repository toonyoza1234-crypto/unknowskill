package daripher.skilltree.client.widget.group;

import java.awt.geom.Rectangle2D.Double;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;

public class ScrollableZoomableWidgetGroup<T extends AbstractWidget> extends WidgetGroup<T> {
   protected float scrollX;
   protected float scrollY;
   protected int maxScrollX;
   protected int maxScrollY;
   private float zoom = 1.0F;

   public ScrollableZoomableWidgetGroup(int pX, int pY, int pWidth, int pHeight) {
      super(pX, pY, pWidth, pHeight);
   }

   @Override
   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
      graphics.pose().pushPose();
      graphics.pose().translate(this.scrollX, this.scrollY, 0.0F);
      this.renderBackground(graphics, mouseX, mouseY, partialTick);

      for (T widget : this.widgets) {
         graphics.pose().pushPose();
         double widgetCenterX = (double)((float)widget.getX() + (float)widget.getWidth() / 2.0F);
         double widgetCenterY = (double)((float)widget.getY() + (float)widget.getHeight() / 2.0F);
         graphics.pose().translate(widgetCenterX, widgetCenterY, 0.0);
         graphics.pose().scale(this.zoom, this.zoom, 1.0F);
         graphics.pose().translate(-widgetCenterX, -widgetCenterY, 0.0);
         widget.render(graphics, mouseX, mouseY, partialTick);
         graphics.pose().popPose();
      }

      graphics.pose().popPose();
      graphics.disableScissor();
   }

   protected void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (button != 2) {
         return false;
      } else {
         if (this.maxScrollX > 0) {
            this.scrollX += (float)dragX;
         }

         if (this.maxScrollY > 0) {
            this.scrollY += (float)dragY;
         }

         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (delta > 0.0 && this.zoom < 2.0F) {
         this.zoom += 0.05F;
         this.scrollX *= 1.05F;
         this.scrollY *= 1.05F;
      }

      if (delta < 0.0 && this.zoom > 0.25F) {
         this.zoom -= 0.05F;
         this.scrollX *= 0.95F;
         this.scrollY *= 0.95F;
      }

      this.rebuildFunc.run();
      return true;
   }

   @Nullable
   @Override
   public T getWidgetAt(double mouseX, double mouseY) {
      mouseX -= (double)this.scrollX;
      mouseY -= (double)this.scrollY;

      for (T widget : this.widgets) {
         Double widgetArea = this.getWidgetArea(widget);
         if (widgetArea.contains(mouseX, mouseY)) {
            return widget;
         }
      }

      return null;
   }

   @NotNull
   @Override
   protected Double getWidgetArea(T widget) {
      double width = (double)((float)widget.getWidth() * this.zoom);
      double height = (double)((float)widget.getHeight() * this.zoom);
      double x = (double)widget.getX() + (double)widget.getWidth() / 2.0 - width / 2.0;
      double y = (double)widget.getY() + (double)widget.getHeight() / 2.0 - height / 2.0;
      return new Double(x, y, width, height);
   }

   public void setMaxScrollX(int maxScrollX) {
      this.maxScrollX = maxScrollX;
   }

   public void setMaxScrollY(int maxScrollY) {
      this.maxScrollY = maxScrollY;
   }

   public int getMaxScrollX() {
      return this.maxScrollX;
   }

   public int getMaxScrollY() {
      return this.maxScrollY;
   }

   public float getScrollX() {
      return this.scrollX;
   }

   public float getScrollY() {
      return this.scrollY;
   }

   public float getZoom() {
      return this.zoom;
   }
}
