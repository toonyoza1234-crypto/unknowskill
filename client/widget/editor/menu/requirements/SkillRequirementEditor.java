package daripher.skilltree.client.widget.editor.menu.requirements;

import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.requirement.SkillRequirement;
import java.util.List;

public class SkillRequirementEditor extends EditorMenu {
   private final int selectedRequirement;

   public SkillRequirementEditor(SkillTreeEditor editor, EditorMenu previousMenu, int selectedBonus) {
      super(editor, previousMenu);
      this.selectedRequirement = selectedBonus;
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.addConfirmationButton(110, 0, 90, 14, "Remove", "Confirm").setPressFunc(b -> this.deleteSelectedSkillBonuses(this.editor));
      this.editor.increaseHeight(29);
      if (this.editor.canEditSkillRequirements()) {
         PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
         if (selectedSkill != null) {
            List<SkillRequirement<?>> requirements = selectedSkill.getRequirements();
            if (this.selectedRequirement >= requirements.size()) {
               this.editor.selectMenu(this.previousMenu);
            } else {
               SkillRequirement<?> requirement = selectedSkill.getRequirements().get(this.selectedRequirement);
               requirement.addEditorWidgets(this.editor, b -> this.setSkillRequirements(this.editor, b));
            }
         }
      }
   }

   private void setSkillRequirements(SkillTreeEditor editor, SkillRequirement<?> requirement) {
      editor.getSelectedSkills().forEach(s -> s.getRequirements().set(this.selectedRequirement, requirement.copy()));
      editor.saveSelectedSkills();
   }

   private void deleteSelectedSkillBonuses(SkillTreeEditor editor) {
      editor.getSelectedSkills().forEach(s -> this.removeRequirement(s, this.selectedRequirement));
      editor.selectMenu(this.previousMenu);
      editor.saveSelectedSkills();
      editor.rebuildWidgets();
   }

   private void removeRequirement(PassiveSkill skill, int index) {
      if (skill.getRequirements().size() > index) {
         skill.getRequirements().remove(index);
         SkillTreeEditorData.saveEditorSkill(skill);
      }
   }
}
