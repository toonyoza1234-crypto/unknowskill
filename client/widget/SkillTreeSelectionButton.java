package daripher.skilltree.client.widget;

import daripher.skilltree.client.screen.SkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SkillTreeSelectionButton extends Button {
   private final ResourceLocation skillTreeId;

   public SkillTreeSelectionButton(int x, int y, int width, int height, ResourceLocation skillTreeId) {
      super(x, y, width, height, Component.translatable(skillTreeId.toString()));
      this.setPressFunc(b -> onPress(skillTreeId));
      this.skillTreeId = skillTreeId;
   }

   private static void onPress(ResourceLocation skillTreeId) {
      getMinecraft().setScreen(new SkillTreeScreen(skillTreeId));
   }

   @Override
   protected void renderBackground(@NotNull GuiGraphics graphics) {
      String texturesFolder = "textures/icons/skill_tree/";
      ResourceLocation texture = this.skillTreeId.withPrefix(texturesFolder).withSuffix(".png");
      int v = this.getTextureVariant() * 19;
      graphics.blit(texture, this.getX(), this.getY(), 0.0F, (float)v, this.width, this.height, 19, 57);
   }

   @Override
   protected void renderText(@NotNull GuiGraphics graphics) {
   }

   private static Minecraft getMinecraft() {
      return Minecraft.getInstance();
   }
}
