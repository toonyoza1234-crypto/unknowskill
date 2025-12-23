package daripher.skilltree.client.widget;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfirmationButton extends Button {
   protected boolean confirming;
   private Component confirmationMessage;

   public ConfirmationButton(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
   }

   @NotNull
   public Component getMessage() {
      return this.confirming && this.confirmationMessage != null ? this.confirmationMessage : super.getMessage();
   }

   @Override
   public void onPress() {
      if (!this.confirming) {
         this.confirming = true;
      } else {
         this.pressFunc.onPress(this);
      }
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      boolean clicked = super.mouseClicked(pMouseX, pMouseY, pButton);
      if (!clicked) {
         this.confirming = false;
      }

      return clicked;
   }

   public void setConfirmationMessage(Component message) {
      this.confirmationMessage = message;
   }
}
