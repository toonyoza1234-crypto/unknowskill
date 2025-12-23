package daripher.skilltree.client.screen;

import daripher.skilltree.client.widget.SkillTreeSelectionButton;
import daripher.skilltree.data.reloader.SkillTreesReloader;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SkillTreeSelectionScreen extends Screen {
   public static final int BUTTONS_SIZE = 19;
   public static final int BUTTONS_SPACING = 5;

   public SkillTreeSelectionScreen() {
      super(Component.empty());
   }

   protected void init() {
      this.clearWidgets();
      this.addSkillTreeButtons();
   }

   private void addSkillTreeButtons() {
      List<PassiveSkillTree> skillTrees = getNonEmptySkillTrees();
      int buttonCount = skillTrees.size();
      int buttonRowWidth = buttonCount * 19 - (buttonCount - 1) * 5;
      int x = this.width / 2 - buttonRowWidth / 2;
      int y = this.height / 2 - 9;

      for (PassiveSkillTree skillTree : skillTrees) {
         Button button = new SkillTreeSelectionButton(x, y, 19, 19, skillTree.getId());
         x += 24;
         this.addRenderableWidget(button);
      }
   }

   @NotNull
   private static List<PassiveSkillTree> getNonEmptySkillTrees() {
      return SkillTreesReloader.getSkillTrees().values().stream().filter(skillTree -> !skillTree.getSkillIds().isEmpty()).toList();
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTick);

      for (Renderable widget : this.renderables) {
         if (widget instanceof SkillTreeSelectionButton) {
            SkillTreeSelectionButton button = (SkillTreeSelectionButton)widget;
            if (button.isMouseOver((double)mouseX, (double)mouseY)) {
               guiGraphics.renderTooltip(this.font, button.getMessage(), mouseX, mouseY);
            }
         }
      }
   }

   public void renderBackground(GuiGraphics guiGraphics) {
      ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/skill_tree_background.png");
      int size = 2048;
      guiGraphics.blit(texture, (this.width - size) / 2, (this.height - size) / 2, 0, 0.0F, 0.0F, size, size, size, size);
   }
}
