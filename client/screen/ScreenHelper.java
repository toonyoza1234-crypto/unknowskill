package daripher.skilltree.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.skill.SkillButton;
import daripher.skilltree.client.widget.skill.SkillConnection;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScreenHelper {
   public static void drawCenteredOutlinedText(GuiGraphics graphics, String text, int x, int y, int color) {
      Font font = Minecraft.getInstance().font;
      x -= font.width(text) / 2;
      graphics.drawString(font, text, x + 1, y, 0, false);
      graphics.drawString(font, text, x - 1, y, 0, false);
      graphics.drawString(font, text, x, y + 1, 0, false);
      graphics.drawString(font, text, x, y - 1, 0, false);
      graphics.drawString(font, text, x, y, color, false);
   }

   public static void drawRectangle(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y + 1, x + 1, y + height - 1, color);
      graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
   }

   public static float getAngleBetweenButtons(Button button1, Button button2) {
      float x1 = (float)button1.getX() + (float)button1.getWidth() / 2.0F;
      float y1 = (float)button1.getY() + (float)button1.getHeight() / 2.0F;
      float x2 = (float)button2.getX() + (float)button2.getWidth() / 2.0F;
      float y2 = (float)button2.getY() + (float)button2.getHeight() / 2.0F;
      return (float)Mth.atan2((double)(y2 - y1), (double)(x2 - x1));
   }

   public static float getDistanceBetweenButtons(Button button1, Button button2) {
      float x1 = (float)button1.getX() + (float)button1.getWidth() / 2.0F;
      float y1 = (float)button1.getY() + (float)button1.getHeight() / 2.0F;
      float x2 = (float)button2.getX() + (float)button2.getWidth() / 2.0F;
      float y2 = (float)button2.getY() + (float)button2.getHeight() / 2.0F;
      return Mth.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
   }

   public static void renderSkillTooltip(PassiveSkillTree skillTree, SkillButton button, GuiGraphics graphics, float x, float y, int width, int height) {
      Font font = Minecraft.getInstance().font;
      int maxWidth = width - 10;
      List<MutableComponent> tooltip = new ArrayList<>();

      for (MutableComponent component : button.getSkillTooltip(skillTree)) {
         if (font.width(component) > maxWidth) {
            tooltip.addAll(TooltipHelper.split(component, font, maxWidth));
         } else {
            tooltip.add(component);
         }
      }

      if (!tooltip.isEmpty()) {
         int tooltipWidth = 0;
         int tooltipHeight = tooltip.size() == 1 ? 8 : 10;

         for (MutableComponent componentx : tooltip) {
            int k = font.width(componentx);
            if (k > tooltipWidth) {
               tooltipWidth = k;
            }

            tooltipHeight += 9 + 2;
         }

         tooltipWidth += 42;
         float tooltipX = x + 12.0F;
         float tooltipY = y - 12.0F;
         if (tooltipX + (float)tooltipWidth > (float)width) {
            tooltipX -= (float)(28 + tooltipWidth);
         }

         if (tooltipY + (float)tooltipHeight + 6.0F > (float)height) {
            tooltipY = (float)(height - tooltipHeight - 6);
         }

         if (tooltipX < 5.0F) {
            tooltipX = 5.0F;
         }

         if (tooltipY < 5.0F) {
            tooltipY = 5.0F;
         }

         graphics.pose().pushPose();
         graphics.pose().translate(tooltipX, tooltipY, 10.0F);
         graphics.fill(1, 4, tooltipWidth - 1, tooltipHeight + 4, -587202560);
         int textX = 5;
         int textY = 2;
         ResourceLocation texture = button.skill.getTooltipFrameTexture();
         graphics.blit(texture, -4, -4, 0.0F, 0.0F, 21, 20, 110, 20);
         graphics.blit(texture, tooltipWidth + 4 - 21, -4, -21.0F, 0.0F, 21, 20, 110, 20);
         int centerWidth = tooltipWidth + 8 - 42;
         int centerX = 17;

         while (centerWidth > 0) {
            int partWidth = Math.min(centerWidth, 68);
            graphics.blit(texture, centerX, -4, 21.0F, 0.0F, partWidth, 20, 110, 20);
            centerX += partWidth;
            centerWidth -= partWidth;
         }

         MutableComponent title = tooltip.remove(0);
         graphics.drawCenteredString(font, title, tooltipWidth / 2, textY, 16777215);
         textY += 19;

         for (MutableComponent componentx : tooltip) {
            graphics.drawString(font, componentx, textX, textY, 16777215);
            textY += 9 + 2;
         }

         graphics.pose().popPose();
      }
   }

   public static void renderGatewayConnection(GuiGraphics graphics, SkillConnection connection, boolean highlighted, float zoom, float animation) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/long_connection.png");
      graphics.pose().pushPose();
      SkillButton button1 = connection.getFirstButton();
      SkillButton button2 = connection.getSecondButton();
      double connectionX = (double)(button1.x + (float)button1.getWidth() / 2.0F);
      double connectionY = (double)(button1.y + (float)button1.getHeight() / 2.0F);
      graphics.pose().translate(connectionX, connectionY, 0.0);
      float rotation = getAngleBetweenButtons(button1, button2);
      graphics.pose().mulPose(Axis.ZP.rotation(rotation));
      int length = (int)(getDistanceBetweenButtons(button1, button2) / zoom);
      graphics.pose().scale(zoom, zoom, 1.0F);
      graphics.blit(texture, 0, -8, length, 6, -animation, highlighted ? 0.0F : 6.0F, length, 6, 30, 12);
      graphics.blit(texture, 0, 2, length, 6, animation, highlighted ? 0.0F : 6.0F, length, 6, -30, 12);
      graphics.pose().popPose();
   }

   public static void renderOneWayConnection(GuiGraphics graphics, SkillConnection connection, boolean highlighted, float zoom, float animation) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/one_way_connection.png");
      graphics.pose().pushPose();
      SkillButton button1 = connection.getFirstButton();
      SkillButton button2 = connection.getSecondButton();
      double connectionX = (double)(button1.x + (float)button1.getWidth() / 2.0F);
      double connectionY = (double)(button1.y + (float)button1.getHeight() / 2.0F);
      graphics.pose().translate(connectionX, connectionY, 0.0);
      float rotation = getAngleBetweenButtons(button1, button2);
      graphics.pose().mulPose(Axis.ZP.rotation(rotation));
      int length = (int)(getDistanceBetweenButtons(button1, button2) / zoom);
      graphics.pose().scale(zoom, zoom, 1.0F);
      graphics.blit(texture, 0, -3, length, 6, -animation, highlighted ? 0.0F : 6.0F, length, 6, 30, 12);
      graphics.pose().popPose();
   }

   public static void renderConnection(GuiGraphics graphics, SkillConnection connection, float zoom, float animation) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/direct_connection.png");
      graphics.pose().pushPose();
      SkillButton button1 = connection.getFirstButton();
      SkillButton button2 = connection.getSecondButton();
      double connectionX = (double)(button1.x + (float)button1.getWidth() / 2.0F);
      double connectionY = (double)(button1.y + (float)button1.getHeight() / 2.0F);
      graphics.pose().translate(connectionX, connectionY, 0.0);
      float rotation = getAngleBetweenButtons(button1, button2);
      graphics.pose().mulPose(Axis.ZP.rotation(rotation));
      int length = (int)getDistanceBetweenButtons(button1, button2);
      boolean highlighted = button1.skillLearned && button2.skillLearned;
      graphics.pose().scale(1.0F, zoom, 1.0F);
      graphics.blit(texture, 0, -3, length, 6, 0.0F, highlighted ? 0.0F : 6.0F, length, 6, 50, 12);
      boolean shouldAnimate = button1.skillLearned && button2.canLearn || button2.skillLearned && button1.canLearn;
      if (!highlighted && shouldAnimate) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (Mth.sin(animation / 3.0F) + 1.0F) / 2.0F);
         graphics.blit(texture, 0, -3, length, 6, 0.0F, 0.0F, length, 6, 50, 12);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      graphics.pose().popPose();
   }
}
