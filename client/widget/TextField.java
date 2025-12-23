package daripher.skilltree.client.widget;

import daripher.skilltree.mixin.EditBoxAccessor;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextField extends EditBox implements TickingWidget {
   public static final int INVALID_TEXT_COLOR = 14155776;
   private static final int HINT_COLOR = 5723991;
   private Predicate<String> softFilter = Objects::nonNull;
   private Function<String, String> suggestionProvider = s -> null;
   private String hint = null;

   public TextField(int x, int y, int width, int height, String defaultText) {
      super(Minecraft.getInstance().font, x, y, width, height, Component.empty());
      this.setMaxLength(80);
      this.setValue(defaultText);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.canConsumeInput() && keyCode == 256) {
         this.setFocused(false);
         return true;
      } else {
         EditBoxAccessor accessor = (EditBoxAccessor)this;
         if (keyCode == 258 && accessor.getSuggestion() != null) {
            this.setValue(this.getValue() + accessor.getSuggestion());
            this.setSuggestion(null);
            return true;
         } else {
            boolean result = super.keyPressed(keyCode, scanCode, modifiers);
            this.setSuggestion(this.suggestionProvider.apply(this.getValue()));
            return result;
         }
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      boolean result = super.charTyped(codePoint, modifiers);
      this.setSuggestion(this.suggestionProvider.apply(this.getValue()));
      return result;
   }

   public void setResponder(@NotNull Consumer<String> responder) {
      super.setResponder(s -> {
         if (this.isValueValid()) {
            responder.accept(s);
         }
      });
   }

   public TextField setSuggestionProvider(Function<String, String> suggestionProvider) {
      this.suggestionProvider = suggestionProvider;
      return this;
   }

   public TextField setSoftFilter(Predicate<String> filter) {
      this.softFilter = filter;
      return this;
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      EditBoxAccessor accessor = (EditBoxAccessor)this;
      if (this.isVisible()) {
         ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/widgets.png");
         int v = this.isHoveredOrFocused() ? 42 : 56;
         graphics.blit(texture, this.getX(), this.getY(), 0, v, this.width / 2, this.height);
         graphics.blit(texture, this.getX() + this.width / 2, this.getY(), -this.width / 2, v, this.width / 2, this.height);
         int textColor = this.getTextColor();
         int cursorVisiblePosition = this.getCursorPosition() - accessor.getDisplayPos();
         int highlightWidth = accessor.getHighlightPos() - accessor.getDisplayPos();
         Minecraft minecraft = Minecraft.getInstance();
         Font font = minecraft.font;
         String visibleText = font.plainSubstrByWidth(this.getValue().substring(accessor.getDisplayPos()), this.getInnerWidth());
         boolean isTextSplitByCursor = cursorVisiblePosition >= 0 && cursorVisiblePosition <= visibleText.length();
         boolean isCursorVisible = this.isFocused() && accessor.getFrame() / 6 % 2 == 0 && isTextSplitByCursor;
         if (visibleText.isEmpty() && this.hint != null && !this.isFocused()) {
            visibleText = this.hint;
         }

         int textX = this.getX() + 5;
         int textStartX = textX;
         int textY = this.getY() + 3;
         if (highlightWidth > visibleText.length()) {
            highlightWidth = visibleText.length();
         }

         if (!visibleText.isEmpty()) {
            String s1 = isTextSplitByCursor ? visibleText.substring(0, cursorVisiblePosition) : visibleText;
            textX = graphics.drawString(font, accessor.getFormatter().apply(s1, accessor.getDisplayPos()), textX, textY, textColor, true);
         }

         boolean isCursorSurrounded = this.getCursorPosition() < this.getValue().length() || this.getValue().length() >= accessor.getMaxLength();
         int cursorX = textX;
         if (!isTextSplitByCursor) {
            cursorX = cursorVisiblePosition > 0 ? this.getX() + this.width : this.getX();
         } else if (isCursorSurrounded) {
            cursorX = textX - 1;
            textX--;
         }

         if (!visibleText.isEmpty() && isTextSplitByCursor && cursorVisiblePosition < visibleText.length()) {
            graphics.drawString(
               font, accessor.getFormatter().apply(visibleText.substring(cursorVisiblePosition), this.getCursorPosition()), textX, textY, textColor, true
            );
         }

         if (!isCursorSurrounded && accessor.getSuggestion() != null) {
            graphics.drawString(font, accessor.getSuggestion(), cursorX - 1, textY, -8355712, true);
         }

         if (isCursorVisible) {
            if (isCursorSurrounded) {
               graphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, -3092272);
            } else {
               graphics.drawString(font, "_", cursorX, textY, textColor, true);
            }
         }

         if (highlightWidth != cursorVisiblePosition) {
            int highlightEndX = textStartX + font.width(visibleText.substring(0, highlightWidth));
            accessor.invokeRenderHighlight(graphics, cursorX, textY - 1, highlightEndX - 1, textY + 9);
         }
      }
   }

   public boolean isValueValid() {
      return this.softFilter.test(this.getValue());
   }

   public TextField setHint(@Nullable String hint) {
      this.hint = hint;
      return this;
   }

   private int getTextColor() {
      return this.getValue().isEmpty() ? 5723991 : (this.isValueValid() ? 14737632 : 14155776);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.setFocused(this.clicked(mouseX, mouseY));
      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void onWidgetTick() {
      this.tick();
   }

   public TextField setFocused() {
      this.setFocused(true);
      return this;
   }
}
