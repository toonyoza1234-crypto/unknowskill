package daripher.skilltree.client.widget.editor;

import daripher.skilltree.client.screen.ScreenHelper;
import daripher.skilltree.client.widget.skill.SkillButton;
import daripher.skilltree.client.widget.skill.SkillButtons;
import daripher.skilltree.skill.PassiveSkill;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillSelector extends AbstractWidget {
   private static final int SELECTION_COLOR = -292164812;
   private final Set<PassiveSkill> selectedSkills = new LinkedHashSet<>();
   private final SkillButtons skillButtons;
   private final SkillTreeEditor editor;
   private int selectionStartX;
   private int selectionStartY;

   public SkillSelector(SkillTreeEditor editor, SkillButtons skillButtons) {
      super(0, 0, 0, 0, Component.empty());
      this.skillButtons = skillButtons;
      this.editor = editor;
      this.active = false;
   }

   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (this.active) {
         this.renderSelectionArea(graphics, mouseX, mouseY);
      }

      this.renderSelectedSkillsHighlight(graphics);
   }

   private void renderSelectedSkillsHighlight(@NotNull GuiGraphics graphics) {
      graphics.pose().pushPose();
      graphics.pose().translate(this.skillButtons.getScrollX(), this.skillButtons.getScrollY(), 0.0F);
      float zoom = this.skillButtons.getZoom();

      for (SkillButton widget : this.getSelectedButtons()) {
         this.renderSkillSelection(graphics, widget, zoom);
      }

      graphics.pose().popPose();
   }

   private void renderSkillSelection(@NotNull GuiGraphics graphics, SkillButton widget, float zoom) {
      graphics.pose().pushPose();
      double widgetCenterX = (double)((float)widget.getX() + (float)widget.getWidth() / 2.0F);
      double widgetCenterY = (double)((float)widget.getY() + (float)widget.getHeight() / 2.0F);
      graphics.pose().translate(widgetCenterX, widgetCenterY, 0.0);
      graphics.pose().scale(zoom, zoom, 1.0F);
      graphics.pose().translate(-widgetCenterX, -widgetCenterY, 0.0);
      int x = widget.getX() - 1;
      int y = widget.getY() - 1;
      int width = widget.getWidth() + 2;
      int height = widget.getHeight() + 2;
      ScreenHelper.drawRectangle(graphics, x, y, width, height, -292164812);
      graphics.pose().popPose();
   }

   private void renderSelectionArea(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      ScreenHelper.drawRectangle(graphics, this.selectionStartX, this.selectionStartY, mouseX - this.selectionStartX, mouseY - this.selectionStartY, -292164812);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else if (this.editor.getArea().contains(mouseX, mouseY)) {
         return false;
      } else if (Screen.hasControlDown()) {
         return false;
      } else {
         if (Screen.hasShiftDown()) {
            this.active = true;
            this.selectionStartX = (int)mouseX;
            this.selectionStartY = (int)mouseY;
         } else {
            if (!this.selectedSkills.isEmpty()) {
               this.clearSelection();
            }

            SkillButton clickedWidget = this.skillButtons.getWidgetAt(mouseX, mouseY);
            if (clickedWidget == null) {
               return false;
            }

            PassiveSkill clickedSkill = clickedWidget.skill;
            if (this.selectedSkills.contains(clickedSkill)) {
               this.selectedSkills.remove(clickedSkill);
            } else {
               this.selectedSkills.add(clickedSkill);
            }

            this.editor.rebuildWidgets();
         }

         return true;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.active) {
         this.addSelectedSkills(mouseX, mouseY);
         this.active = false;
         this.editor.rebuildWidgets();
         return true;
      } else {
         return false;
      }
   }

   private void addSelectedSkills(double mouseX, double mouseY) {
      Rectangle2D selectedArea = this.getSelectionArea(mouseX, mouseY);

      for (SkillButton skillButton : this.skillButtons.getWidgets()) {
         Rectangle2D skillArea = this.getSkillArea(skillButton);
         if (selectedArea.intersects(skillArea)) {
            this.selectedSkills.add(skillButton.skill);
         }
      }

      this.editor.rebuildWidgets();
   }

   @NotNull
   private Rectangle2D getSelectionArea(double mouseX, double mouseY) {
      double x = Math.min(mouseX, (double)this.selectionStartX) - (double)this.skillButtons.getScrollX();
      double y = Math.min(mouseY, (double)this.selectionStartY) - (double)this.skillButtons.getScrollY();
      double width = Math.abs(mouseX - (double)this.selectionStartX);
      double height = Math.abs(mouseY - (double)this.selectionStartY);
      return new Double(x, y, width, height);
   }

   @NotNull
   private Rectangle2D getSkillArea(SkillButton skill) {
      double skillSize = (double)((float)skill.skill.getSkillSize() * this.skillButtons.getZoom());
      double skillX = (double)skill.x + (double)skill.getWidth() / 2.0 - skillSize / 2.0;
      double skillY = (double)skill.y + (double)skill.getHeight() / 2.0 - skillSize / 2.0;
      return new Double(skillX, skillY, skillSize, skillSize);
   }

   public Set<PassiveSkill> getSelectedSkills() {
      return this.selectedSkills;
   }

   public void clearSelection() {
      this.selectedSkills.clear();
      this.editor.rebuildWidgets();
   }

   @Nullable
   public PassiveSkill getFirstSelectedSkill() {
      return this.selectedSkills.isEmpty() ? null : (PassiveSkill)this.selectedSkills.toArray()[0];
   }

   @NotNull
   private List<SkillButton> getSelectedButtons() {
      return this.selectedSkills.stream().map(PassiveSkill::getId).map(this.skillButtons::getWidgetById).toList();
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }
}
