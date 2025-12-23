package daripher.skilltree.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.SkillNodeEditor;
import daripher.skilltree.client.widget.skill.SkillButtons;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SkillTreeEditorScreen extends Screen implements StatsUpdateListener {
   private final PassiveSkillTree skillTree;
   private final SkillButtons skillButtons;
   private final SkillTreeEditor editorWidgets;
   private boolean shouldCloseOnEsc = true;
   private int prevMouseX;
   private int prevMouseY;
   private boolean statsUpdated;

   public SkillTreeEditorScreen(ResourceLocation skillTreeId) {
      super(Component.empty());
      this.minecraft = Minecraft.getInstance();
      this.skillTree = SkillTreeEditorData.getOrCreateEditorTree(skillTreeId);
      this.skillButtons = new SkillButtons(this.skillTree, () -> 0.0F);
      this.editorWidgets = new SkillTreeEditor(this.skillButtons);
   }

   public void init() {
      if (!this.statsUpdated) {
         ClientPacketListener connection = this.getMinecraft().getConnection();
         Objects.requireNonNull(connection);
         connection.send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
      }

      if (this.skillTree == null) {
         this.getMinecraft().setScreen(null);
      } else {
         this.clearWidgets();
         this.skillButtons.setWidth(this.width);
         this.skillButtons.setHeight(this.height);
         this.editorWidgets.setWidth(210);
         this.editorWidgets.setHeight(10);
         this.editorWidgets.setX(this.width - this.editorWidgets.getWidth());
         this.editorWidgets.init();
         this.editorWidgets.increaseHeight(5);
         this.editorWidgets.setRebuildFunc(this::rebuildWidgets);
         this.skillButtons.setRebuildFunc(this::rebuildWidgets);
         this.skillButtons.clearWidgets();
         this.editorWidgets.getSkills().forEach(this.editorWidgets::addSkillButton);
         this.skillButtons.updateSkillConnections();
         this.calculateMaxScroll();
         this.addRenderableWidget(this.skillButtons);
         this.addRenderableWidget(this.editorWidgets);
      }
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
      this.renderBackground(graphics);
      this.skillButtons.render(graphics, mouseX, mouseY, partialTick);
      this.renderOverlay(graphics);
      this.editorWidgets.render(graphics, mouseX, mouseY, partialTick);
      if (mouseX < this.editorWidgets.getX() || mouseY > this.editorWidgets.getHeight()) {
         float tooltipX = (float)mouseX + (float)(this.prevMouseX - mouseX) * partialTick;
         float tooltipY = (float)mouseY + (float)(this.prevMouseY - mouseY) * partialTick;
         this.skillButtons.renderTooltip(graphics, tooltipX, tooltipY);
      }

      this.prevMouseX = mouseX;
      this.prevMouseY = mouseY;
   }

   private void createBlankSkill() {
      ResourceLocation background = new ResourceLocation("skilltree", "textures/icons/background/lesser.png");
      ResourceLocation icon = new ResourceLocation("skilltree", "textures/icons/void.png");
      ResourceLocation border = new ResourceLocation("skilltree", "textures/tooltip/lesser.png");
      ResourceLocation skillId = SkillNodeEditor.createNewSkillId(this.skillTree.getId());
      PassiveSkill skill = new PassiveSkill(skillId, 16, background, icon, border, false);
      skill.setPosition(0.0F, 0.0F);
      SkillTreeEditorData.saveEditorSkill(skill);
      SkillTreeEditorData.loadEditorSkill(skill.getId());
      this.editorWidgets.getSkillTree().getSkillIds().add(skill.getId());
      SkillTreeEditorData.saveEditorSkillTree(this.editorWidgets.getSkillTree());
   }

   public boolean shouldCloseOnEsc() {
      if (!this.shouldCloseOnEsc) {
         this.shouldCloseOnEsc = true;
         return false;
      } else {
         return super.shouldCloseOnEsc();
      }
   }

   public void tick() {
      this.editorWidgets.onWidgetTick();
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
      poseStack.translate(this.skillButtons.getScrollX() / 3.0F, this.skillButtons.getScrollY() / 3.0F, 0.0F);
      int size = 2048;
      graphics.blit(texture, (this.width - size) / 2, (this.height - size) / 2, 0, 0.0F, 0.0F, size, size, size, size);
      poseStack.popPose();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.editorWidgets.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.editorWidgets.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.editorWidgets.mouseScrolled(mouseX, mouseY, amount) || this.skillButtons.mouseScrolled(mouseX, mouseY, amount);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      return this.editorWidgets.mouseDragged(mouseX, mouseY, button, dragX, dragY) | this.skillButtons.mouseDragged(mouseX, mouseY, button, dragX, dragY);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.editorWidgets.keyPressed(keyCode, scanCode, modifiers)) {
         if (keyCode == 256) {
            this.shouldCloseOnEsc = false;
         }

         return true;
      } else if (keyCode == 256 && this.shouldCloseOnEsc()) {
         this.onClose();
         return true;
      } else if (keyCode == 78 && Screen.hasControlDown()) {
         this.createBlankSkill();
         this.rebuildWidgets();
         return true;
      } else {
         return false;
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      return this.editorWidgets.charTyped(codePoint, modifiers);
   }

   public void onStatsUpdated() {
      this.statsUpdated = true;
      this.init();
   }
}
