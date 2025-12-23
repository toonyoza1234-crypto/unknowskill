package daripher.skilltree.client.widget.editor.menu;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.skill.PassiveSkill;
import net.minecraft.ChatFormatting;

public class SkillButtonEditor extends EditorMenu {
   public SkillButtonEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
      super(editor, previousMenu);
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
      if (selectedSkill != null) {
         if (this.editor.canEdit(PassiveSkill::getSkillSize)) {
            this.editor.addLabel(0, 0, "Size", ChatFormatting.GOLD);
            this.editor.increaseHeight(19);
            this.editor
               .addNumericTextField(0, 0, 40, 14, (double)selectedSkill.getSkillSize())
               .setNumericFilter(d -> d >= 2.0)
               .setNumericResponder(this::setSkillsSize);
            this.editor.increaseHeight(19);
         }

         if (this.editor.getSelectedSkills().size() == 1) {
            this.editor.increaseHeight(-38);
            this.editor.addLabel(65, 0, "Position", ChatFormatting.GOLD);
            this.editor.increaseHeight(19);
            this.editor
               .addNumericTextField(65, 0, 60, 14, (double)selectedSkill.getPositionX())
               .setNumericResponder(v -> this.setSkillPosition(v.floatValue(), selectedSkill.getPositionY()));
            this.editor
               .addNumericTextField(130, 0, 60, 14, (double)selectedSkill.getPositionY())
               .setNumericResponder(v -> this.setSkillPosition(selectedSkill.getPositionX(), v.floatValue()));
            this.editor.increaseHeight(19);
         }

         if (this.editor.canEdit(PassiveSkill::getTitle)) {
            this.editor.addLabel(0, 0, "Title", ChatFormatting.GOLD);
            this.editor.increaseHeight(19);
            this.editor.addTextField(0, 0, 200, 14, selectedSkill.getTitle()).setResponder(this::setSkillsTitle);
            this.editor.increaseHeight(19);
         }

         boolean canEditTitleColor = this.editor.canEdit(PassiveSkill::getTitleColor);
         if (canEditTitleColor) {
            this.editor.addLabel(0, 0, "Title Color", ChatFormatting.GOLD);
            this.editor.increaseHeight(19);
            this.editor
               .addTextField(0, 0, 80, 14, selectedSkill.getTitleColor())
               .setSoftFilter(v -> v.matches("^#?[a-fA-F0-9]{6}") || v.isEmpty())
               .setResponder(this::setSkillsTitleColor);
            this.editor.increaseHeight(19);
         }

         if (this.editor.canEdit(PassiveSkill::isStartingPoint)) {
            int widgetsX = 0;
            if (canEditTitleColor) {
               this.editor.increaseHeight(-38);
               widgetsX = 100;
            }

            this.editor.addLabel(widgetsX, 0, "Starting Point", ChatFormatting.GOLD);
            this.editor.increaseHeight(19);
            this.editor.addCheckBox(widgetsX, 0, selectedSkill.isStartingPoint()).setResponder(v -> {
               this.editor.getSelectedSkills().forEach(s -> s.setStartingPoint(v));
               this.editor.saveSelectedSkills();
            });
            this.editor.increaseHeight(19);
         }
      }
   }

   private void setSkillsSize(double size) {
      this.editor.getSelectedSkills().forEach(skill -> {
         skill.setButtonSize((int)size);
         this.editor.removeSkillButton(skill);
         this.editor.addSkillButton(skill);
      });
      this.editor.updateSkillConnections();
      this.editor.saveSelectedSkills();
   }

   private void setSkillPosition(float x, float y) {
      PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
      if (selectedSkill != null) {
         selectedSkill.setPosition(x, y);
         this.editor.removeSkillButton(selectedSkill);
         this.editor.addSkillButton(selectedSkill);
         this.editor.updateSkillConnections();
         this.editor.saveSelectedSkills();
      }
   }

   private void setSkillsTitle(String title) {
      this.editor.getSelectedSkills().forEach(skill -> skill.setTitle(title));
      this.editor.saveSelectedSkills();
   }

   private void setSkillsTitleColor(String color) {
      if (color.startsWith("#")) {
         color = color.substring(1);
      }

      String finalColor = color;
      this.editor.getSelectedSkills().forEach(skill -> skill.setTitleColor(finalColor));
      this.editor.saveSelectedSkills();
   }
}
