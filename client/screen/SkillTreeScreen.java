package daripher.skilltree.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import daripher.skilltree.client.widget.SkillTreeWidgets;
import daripher.skilltree.client.widget.skill.SkillButtons;
import daripher.skilltree.config.ClientConfig;
import daripher.skilltree.data.reloader.SkillTreesReloader;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SkillTreeScreen extends Screen implements StatsUpdateListener {
   public static final int BACKGROUND_SIZE = 2048;
   private final PassiveSkillTree skillTree;
   private final SkillButtons skillButtons;
   private final SkillTreeWidgets skillTreeWidgets;
   public float renderAnimation;
   private int prevMouseX;
   private int prevMouseY;
   private boolean statsUpdated;

   public SkillTreeScreen(ResourceLocation skillTreeId) {
      super(Component.empty());
      this.skillTree = SkillTreesReloader.getSkillTreeById(skillTreeId);
      this.minecraft = Minecraft.getInstance();
      this.skillButtons = new SkillButtons(this.skillTree, () -> this.renderAnimation);
      this.skillTreeWidgets = new SkillTreeWidgets(this.getLocalPlayer(), this.skillButtons, this.skillTree);
      this.skillButtons.setRebuildFunc(this::rebuildWidgets);
      this.skillTreeWidgets.setRebuildFunc(this::rebuildWidgets);
   }

   public void init() {
      if (!this.statsUpdated) {
         ClientPacketListener connection = this.getMinecraft().getConnection();
         Objects.requireNonNull(connection);
         connection.send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
      }

      this.clearWidgets();
      this.skillTreeWidgets.clearWidgets();
      this.skillTreeWidgets.setWidth(this.width);
      this.skillTreeWidgets.setHeight(this.height);
      this.skillButtons.setWidth(this.width);
      this.skillButtons.setHeight(this.height);
      this.skillButtons.clearWidgets();
      this.addSkillButtons();
      this.skillTreeWidgets.init();
      this.calculateMaxScroll();
      this.addRenderableWidget(this.skillTreeWidgets);
      this.addRenderableWidget(this.skillButtons);
   }

   private void addSkillButtons() {
      Stream<PassiveSkill> passiveSkills = this.skillTree.getSkillIds().stream().map(SkillsReloader::getSkillById).filter(Objects::nonNull);
      passiveSkills.forEach(skill -> this.skillTreeWidgets.addSkillButton(skill, () -> this.renderAnimation));
      this.skillButtons.updateSkillConnections();
   }

   protected void rebuildWidgets() {
      this.getMinecraft().tell(() -> super.rebuildWidgets());
   }

   private void calculateMaxScroll() {
      this.skillButtons.setMaxScrollX(Math.min(0, this.width / 2 - 350));
      this.skillButtons.setMaxScrollY(Math.min(0, this.height / 2 - 350));
      this.skillButtons.getWidgets().forEach(button -> {
         float skillX = button.skill.getPositionX();
         float skillY = button.skill.getPositionY();
         int maxScrollX = (int)Math.max((float)this.skillButtons.getMaxScrollX(), Mth.abs(skillX));
         int maxScrollY = (int)Math.max((float)this.skillButtons.getMaxScrollY(), Mth.abs(skillY));
         this.skillButtons.setMaxScrollX(maxScrollX);
         this.skillButtons.setMaxScrollY(maxScrollY);
      });
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderAnimation += partialTick;
      this.renderBackground(graphics);
      this.skillButtons.render(graphics, mouseX, mouseY, partialTick);
      this.renderOverlay(graphics);
      this.skillTreeWidgets.render(graphics, mouseX, mouseY, partialTick);
      float tooltipX = (float)mouseX + (float)(this.prevMouseX - mouseX) * partialTick;
      float tooltipY = (float)mouseY + (float)(this.prevMouseY - mouseY) * partialTick;
      this.skillButtons.renderTooltip(graphics, tooltipX, tooltipY);
      this.prevMouseX = mouseX;
      this.prevMouseY = mouseY;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.skillTreeWidgets.mouseClicked(mouseX, mouseY, button) ? true : this.skillButtons.mouseClicked(mouseX, mouseY, button);
   }

   public void tick() {
      this.skillTreeWidgets.onWidgetTick();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.skillTreeWidgets.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 256) {
         if (SkillTreesReloader.getSkillTrees().size() == 1) {
            this.onClose();
         } else {
            this.getMinecraft().setScreen(new SkillTreeSelectionScreen());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.skillTreeWidgets.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char character, int keyCode) {
      return this.skillTreeWidgets.charTyped(character, keyCode);
   }

   private void renderOverlay(GuiGraphics graphics) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/skill_tree_overlay.png");
      RenderSystem.enableBlend();
      graphics.blit(texture, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      RenderSystem.disableBlend();
   }

   public void renderBackground(GuiGraphics graphics) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/skill_tree_background.png");
      PoseStack poseStack = graphics.pose();
      poseStack.pushPose();
      float x = this.skillButtons.getScrollX();
      float y = this.skillButtons.getScrollY();
      if (ClientConfig.skill_tree_background_parallax) {
         x /= 3.0F;
         y /= 3.0F;
      }

      poseStack.translate(x, y, 0.0F);
      int size = 2048;
      graphics.blit(texture, (this.width - size) / 2, (this.height - size) / 2, 0, 0.0F, 0.0F, size, size, size, size);
      poseStack.popPose();
   }

   public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragAmountX, double dragAmountY) {
      return this.skillButtons.mouseDragged(mouseX, mouseY, mouseButton, dragAmountX, dragAmountY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.skillButtons.mouseScrolled(mouseX, mouseY, amount);
   }

   @Nonnull
   private LocalPlayer getLocalPlayer() {
      return Objects.requireNonNull(this.getMinecraft().player);
   }

   public void updateSkillPoints(int skillPoints) {
      this.skillTreeWidgets.updateSkillPoints(skillPoints);
   }

   public void onStatsUpdated() {
      this.statsUpdated = true;
      this.init();
   }
}
