package daripher.skilltree.client.widget.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import daripher.skilltree.client.screen.ScreenHelper;
import daripher.skilltree.skill.PassiveSkill;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SkillMirrorer extends AbstractWidget {
   private final SkillTreeEditor editor;
   private float mirrorCenterX;
   private float mirrorCenterY;
   private float mirrorAngle;
   private int mirrorSides = 2;

   public SkillMirrorer(SkillTreeEditor editor) {
      super(0, 0, 0, 0, Component.empty());
      this.editor = editor;
      this.active = false;
   }

   public void init() {
      this.editor.addLabel(19, 0, "Mirror", ChatFormatting.GOLD);
      this.editor.addCheckBox(0, 0, this.active).setResponder(v -> this.setActive(this.editor, v));
      this.editor.increaseHeight(19);
      if (this.active) {
         this.editor.addLabel(0, 0, "Sectors", ChatFormatting.GOLD);
         this.editor
            .addNumericTextField(160, 0, 40, 14, (double)this.mirrorSides)
            .setNumericFilter(v -> v > 1.0)
            .setNumericResponder(v -> this.mirrorSides = v.intValue());
         this.editor.increaseHeight(19);
         this.editor.addLabel(0, 0, "Angle", ChatFormatting.GOLD);
         this.editor.addNumericTextField(160, 0, 40, 14, (double)this.mirrorAngle).setNumericResponder(v -> this.mirrorAngle = v.floatValue());
         this.editor.increaseHeight(19);
         this.editor.addLabel(0, 0, "Center", ChatFormatting.GOLD);
         this.editor.addNumericTextField(160, 0, 40, 14, (double)this.mirrorCenterX).setNumericResponder(v -> this.mirrorCenterX = v.floatValue());
         this.editor.addNumericTextField(115, 0, 40, 14, (double)this.mirrorCenterY).setNumericResponder(v -> this.mirrorCenterY = v.floatValue());
         if (this.editor.getSelectedSkills().size() == 1) {
            this.editor.addButton(70, 0, 40, 14, "Set").setPressFunc(b -> this.setMirrorCenter(this.editor));
            this.editor.increaseHeight(19);
         }
      }
   }

   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (this.active) {
         graphics.pose().pushPose();
         int width = this.editor.getScreenWidth();
         int height = this.editor.getScreenHeight();
         float mirrorX = (float)width / 2.0F + this.mirrorCenterX * this.editor.getZoom() + this.editor.getScrollX();
         float mirrorY = (float)height / 2.0F + this.mirrorCenterY * this.editor.getZoom() + this.editor.getScrollY();
         graphics.pose().translate(mirrorX, mirrorY, 0.0F);
         graphics.pose().mulPose(Axis.ZP.rotationDegrees(this.mirrorAngle));

         for (int i = 0; i < this.mirrorSides; i++) {
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(360.0F / (float)this.mirrorSides));
            graphics.fill(-1, -1, 1, width * 2, 1439682511);
         }

         ScreenHelper.drawRectangle(graphics, -4, -4, 8, 8, 1439682511);
         graphics.pose().popPose();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
      }
   }

   private void setActive(SkillTreeEditor editor, boolean active) {
      this.active = active;
      editor.rebuildWidgets();
   }

   private void setMirrorCenter(SkillTreeEditor editor) {
      PassiveSkill selectedSkill = editor.getFirstSelectedSkill();
      if (selectedSkill != null) {
         this.mirrorCenterX = selectedSkill.getPositionX();
         this.mirrorCenterY = selectedSkill.getPositionY();
         editor.rebuildWidgets();
      }
   }

   @Nullable
   public PassiveSkill getMirroredSkill(PassiveSkill skill, int sector) {
      float skillX = skill.getPositionX();
      float skillY = skill.getPositionY();
      if (this.mirrorCenterX == skillX && this.mirrorCenterY == skillY) {
         return skill;
      } else {
         float originalAngle = (float)Math.toDegrees(Math.atan2((double)(skillY - this.mirrorCenterY), (double)(skillX - this.mirrorCenterX))) + 90.0F;
         float sectorSize = 360.0F / (float)this.mirrorSides;
         float angle = (float)Math.toRadians(
            this.mirrorSides == 2 ? (double)(-originalAngle + this.mirrorAngle * 2.0F) : (double)(originalAngle + sectorSize * (float)sector)
         );
         float distance = (float)Math.hypot((double)(skillX - this.mirrorCenterX), (double)(skillY - this.mirrorCenterY));
         float mirroredSkillX = this.mirrorCenterX + Mth.sin(angle) * distance;
         float mirroredSkillY = this.mirrorCenterY + Mth.cos((float)((double)angle + Math.PI)) * distance;
         return this.getSkillAtPosition(mirroredSkillX, mirroredSkillY);
      }
   }

   public void createSkills(float angle, float distance, SkillFactory skillFactory) {
      if (this.active) {
         float sectorSize = 360.0F / (float)this.mirrorSides;

         for (int i = 1; i < this.mirrorSides; i++) {
            angle = this.mirrorSides == 2 ? -angle - this.mirrorAngle * 2.0F : angle - sectorSize;
            float finalAngle = (float)Math.toRadians((double)angle);
            int sector = i;
            this.editor.getSelectedSkills().forEach(skill -> this.createSkill(distance, finalAngle, sector, skill, skillFactory));
         }
      }
   }

   private void createSkill(float distance, float angle, int sector, PassiveSkill skill, SkillFactory skillFactory) {
      skill = this.getMirroredSkill(skill, sector);
      if (skill != null) {
         float skillSize = (float)skill.getSkillSize() / 2.0F + 8.0F;
         float skillX = skill.getPositionX() + Mth.sin(angle) * (distance + skillSize);
         float skillY = skill.getPositionY() + Mth.cos(angle) * (distance + skillSize);
         skillFactory.accept(skillX, skillY, skill);
      }
   }

   @Nullable
   private PassiveSkill getSkillAtPosition(float x, float y) {
      for (PassiveSkill skill : this.editor.getSkills()) {
         double distance = Math.hypot((double)(x - skill.getPositionX()), (double)(y - skill.getPositionY()));
         if (distance < (double)skill.getSkillSize()) {
            return skill;
         }
      }

      return null;
   }

   protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
   }
}
