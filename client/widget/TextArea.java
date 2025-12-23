package daripher.skilltree.client.widget;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TextArea extends MultiLineEditBox implements TickingWidget {
   public TextArea(int x, int y, int width, int height, String defaultValue) {
      super(Minecraft.getInstance().font, x, y, width, height, Component.empty(), Component.empty());
      this.setValue(defaultValue);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.isFocused() && super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return this.isFocused() && super.mouseScrolled(mouseX, mouseY, delta);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.setFocused(this.clicked(mouseX, mouseY));
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public TextArea setResponder(@NotNull Consumer<String> responder) {
      super.setValueListener(responder);
      return this;
   }

   @Override
   public void onWidgetTick() {
      this.tick();
   }
}
