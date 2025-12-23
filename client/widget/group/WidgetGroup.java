package daripher.skilltree.client.widget.group;

import daripher.skilltree.client.widget.TickingWidget;
import java.awt.geom.Rectangle2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WidgetGroup<T extends AbstractWidget> extends AbstractWidget implements TickingWidget {
   protected final Set<T> widgets = new HashSet<>();
   protected Runnable rebuildFunc = () -> {
   };

   public WidgetGroup(int x, int y, int width, int height) {
      super(x, y, width, height, Component.empty());
   }

   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.widgetsCopy().forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 1.0F);
      graphics.pose().popPose();
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.keyPressed(keyCode, scanCode, modifiers)) {
            result = true;
         }
      }

      return result;
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.keyReleased(keyCode, scanCode, modifiers)) {
            result = true;
         }
      }

      return result;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.mouseClicked(mouseX, mouseY, button)) {
            result = true;
         }
      }

      return result;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            result = true;
         }
      }

      return result;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.mouseReleased(mouseX, mouseY, button)) {
            result = true;
         }
      }

      return result;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.mouseScrolled(mouseX, mouseY, delta)) {
            result = true;
         }
      }

      return result;
   }

   public boolean charTyped(char codePoint, int modifiers) {
      boolean result = false;

      for (T widget : this.widgetsCopy()) {
         if (widget.charTyped(codePoint, modifiers)) {
            result = true;
         }
      }

      return result;
   }

   public void mouseMoved(double mouseX, double mouseY) {
      this.widgetsCopy().forEach(widget -> widget.mouseMoved(mouseX, mouseY));
   }

   @Override
   public void onWidgetTick() {
      for (T t : this.widgetsCopy()) {
         if (t instanceof TickingWidget tickingWidget) {
            tickingWidget.onWidgetTick();
         }
      }
   }

   @NotNull
   public <W extends T> W addWidget(@NotNull W widget) {
      this.widgets.add((T)widget);
      return widget;
   }

   public Set<T> getWidgets() {
      return this.widgets;
   }

   public void clearWidgets() {
      this.widgets.clear();
   }

   public void setRebuildFunc(Runnable rebuildFunc) {
      this.rebuildFunc = rebuildFunc;
   }

   public void rebuildWidgets() {
      this.rebuildFunc.run();
   }

   protected HashSet<T> widgetsCopy() {
      return new HashSet<>(this.widgets);
   }

   public Float getArea() {
      return new Float((float)this.getX(), (float)this.getY(), (float)this.width, (float)this.height);
   }

   @Nullable
   public T getWidgetAt(double mouseX, double mouseY) {
      for (T widget : this.widgets) {
         if (widget.visible) {
            Double widgetArea = this.getWidgetArea(widget);
            if (widgetArea.contains(mouseX, mouseY)) {
               return widget;
            }
         }
      }

      return null;
   }

   @NotNull
   protected Double getWidgetArea(T widget) {
      double width = (double)widget.getWidth();
      double height = (double)widget.getHeight();
      double x = (double)widget.getX() + width / 2.0 - width / 2.0;
      double y = (double)widget.getY() + height / 2.0 - height / 2.0;
      return new Double(x, y, width, height);
   }
}
