package daripher.skilltree.client.widget.editor.menu.selection;

import java.util.Collection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TextureSelectionList extends SelectionList<ResourceLocation> {
   private int textureWidth;
   private int textureHeight;

   public TextureSelectionList(
      int x, int y, int elementWidth, int elementHeight, int textureWidth, int textureHeight, Collection<ResourceLocation> elementsList
   ) {
      super(x, y, elementWidth, elementHeight, elementsList);
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
   }

   @Override
   protected void renderElement(@NotNull GuiGraphics graphics, int elementIndex, int x, int y) {
      ResourceLocation texture = this.getDisplayedElements().get(elementIndex);
      int textureX = x + (this.elementWidth - this.textureWidth) / 2;
      int textureY = y + (this.elementHeight - this.textureHeight) / 2;
      graphics.blit(texture, textureX, textureY, 0.0F, 0.0F, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight);
   }

   public TextureSelectionList setElementTextureSize(int width, int height) {
      this.textureWidth = width;
      this.textureHeight = height;
      return this;
   }
}
