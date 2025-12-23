package daripher.skilltree.client.widget.editor;

import daripher.skilltree.skill.PassiveSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SkillDragger extends AbstractWidget {
   private final SkillTreeEditor editor;

   public SkillDragger(SkillTreeEditor editor) {
      super(0, 0, 0, 0, Component.empty());
      this.editor = editor;
   }

   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
   }

   public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
      if (mouseButton == 0 && Screen.hasControlDown() && !this.editor.getSelectedSkills().isEmpty()) {
         this.dragSelectedSkills((float)dragX / this.editor.getZoom(), (float)dragY / this.editor.getZoom());
         return true;
      } else {
         return true;
      }
   }

   private void dragSelectedSkills(float x, float y) {
      this.editor.getSelectedSkills().forEach(skill -> this.dragSkill(x, y, skill));
      this.editor.updateSkillConnections();
      this.editor.saveSelectedSkills();
   }

   private void dragSkill(float x, float y, PassiveSkill skill) {
      skill.setPosition(skill.getPositionX() + x, skill.getPositionY() + y);
      this.editor.getSkillButtons().removeIf(button -> button.skill == skill);
      this.editor.addSkillButton(skill);
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }
}
