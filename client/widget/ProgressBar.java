package daripher.skilltree.client.widget;

import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.screen.ScreenHelper;
import daripher.skilltree.config.ServerConfig;
import daripher.skilltree.exp.ExpHelper;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ProgressBar extends net.minecraft.client.gui.components.Button {
   public boolean showProgressInNumbers;

   public ProgressBar(int x, int y, OnPress pressFunc) {
      super(x, y, 235, 19, Component.empty(), pressFunc, Supplier::get);
   }

   private static int getCurrentLevel() {
      LocalPlayer player = Minecraft.getInstance().player;
      Objects.requireNonNull(player);
      IPlayerSkills capability = PlayerSkillsProvider.get(player);
      int skills = capability.getPlayerSkills().size();
      int points = capability.getSkillPoints();
      return skills + points;
   }

   private static boolean isMaxLevel(int currentLevel) {
      return currentLevel >= ServerConfig.max_skill_points;
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics);
      this.renderCurrentLevel(graphics);
      this.renderNextLevel(graphics);
      this.renderProgress(graphics);
   }

   protected void renderBackground(GuiGraphics graphics) {
      float experienceProgress = this.getExperienceProgress();
      int filledBarWidth = (int)(experienceProgress * 183.0F);
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/progress_bars.png");
      graphics.blit(texture, this.getX() + 26, this.getY() + 7, 0, 0, 182, 5);
      if (filledBarWidth != 0) {
         graphics.blit(texture, this.getX() + 26, this.getY() + 7, 0, 5, filledBarWidth, 5);
      }
   }

   protected void renderProgress(GuiGraphics graphics) {
      if (this.showProgressInNumbers) {
         int cost = ServerConfig.getSkillPointCost(getCurrentLevel());
         LocalPlayer player = Minecraft.getInstance().player;
         Objects.requireNonNull(player);
         long exp = ExpHelper.getPlayerExp(player);
         String text = exp + "/" + cost;
         ScreenHelper.drawCenteredOutlinedText(graphics, text, this.getX() + this.width / 2, this.getTextY(), 16573030);
      } else {
         float experienceProgress = this.getExperienceProgress();
         String text = (int)(experienceProgress * 100.0F) + "%";
         ScreenHelper.drawCenteredOutlinedText(graphics, text, this.getX() + this.width / 2, this.getTextY(), 16573030);
      }
   }

   protected void renderNextLevel(GuiGraphics graphics) {
      int currentLevel = getCurrentLevel();
      if (isMaxLevel(currentLevel)) {
         currentLevel--;
      }

      int nextLevel = currentLevel + 1;
      ScreenHelper.drawCenteredOutlinedText(graphics, nextLevel + "", this.getX() + this.width - 17, this.getTextY(), 16573030);
   }

   protected void renderCurrentLevel(GuiGraphics graphics) {
      int currentLevel = getCurrentLevel();
      if (isMaxLevel(currentLevel)) {
         currentLevel--;
      }

      ScreenHelper.drawCenteredOutlinedText(graphics, currentLevel + "", this.getX() + 17, this.getTextY(), 16573030);
   }

   protected int getTextY() {
      return this.getY() + 5;
   }

   private float getExperienceProgress() {
      int level = getCurrentLevel();
      float progress = 1.0F;
      if (level < ServerConfig.max_skill_points) {
         int levelupCost = ServerConfig.getSkillPointCost(level);
         LocalPlayer player = Minecraft.getInstance().player;
         Objects.requireNonNull(player);
         progress = (float)ExpHelper.getPlayerExp(player) / (float)levelupCost;
         progress = Math.min(1.0F, progress);
      }

      return progress;
   }
}
