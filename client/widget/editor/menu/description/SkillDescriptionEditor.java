package daripher.skilltree.client.widget.editor.menu.description;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.skill.PassiveSkill;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SkillDescriptionEditor extends EditorMenu {
   public SkillDescriptionEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
      super(editor, previousMenu);
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      if (!this.editor.getSelectedSkills().isEmpty()) {
         PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
         if (selectedSkill != null) {
            if (this.canEditDescription()) {
               List<MutableComponent> description = selectedSkill.getDescription();
               this.editor.addConfirmationButton(110, 0, 90, 14, "Regenerate", "Confirm").setPressFunc(b -> this.regenerateSkillsDescription());
               this.editor.increaseHeight(29);
               if (description != null) {
                  for (int i = 0; i < description.size(); i++) {
                     int selectedLine = i;
                     String message = description.get(i).getString();
                     Font font = Minecraft.getInstance().font;
                     message = TooltipHelper.getTrimmedString(font, message, 190);
                     this.editor
                        .addButton(0, 0, 200, 14, message)
                        .setPressFunc(b -> this.editor.selectMenu(new SkillDescriptionLineEditor(this.editor, this, selectedLine)));
                     this.editor.increaseHeight(19);
                  }
               }

               this.editor.increaseHeight(10);
               this.editor.addButton(0, 0, 90, 14, "Add").setPressFunc(b -> this.addSelectedSkillsDescriptionLine());
               this.editor.addConfirmationButton(110, 0, 90, 14, "Clear", "Confirm").setPressFunc(b -> this.removeSelectedSkillsDescription());
               this.editor.increaseHeight(19);
            }
         }
      }
   }

   private boolean canEditDescription() {
      PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
      if (selectedSkill == null) {
         return false;
      } else if (this.editor.getSelectedSkills().size() < 2) {
         return true;
      } else {
         for (PassiveSkill otherSkill : this.editor.getSelectedSkills()) {
            List<MutableComponent> description = selectedSkill.getDescription();
            List<MutableComponent> otherDescription = otherSkill.getDescription();
            if (description != null || otherDescription != null) {
               if (description == null || otherDescription == null) {
                  return false;
               }

               if (description.size() != otherDescription.size()) {
                  return false;
               }

               for (int i = 0; i < description.size(); i++) {
                  if (!description.get(i).equals(otherDescription.get(i))) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private void regenerateSkillsDescription() {
      this.editor.getSelectedSkills().forEach(skill -> skill.setDescription(null));
      this.editor.saveSelectedSkills();
      this.editor.getSelectedSkills().forEach(skill -> {
         List<MutableComponent> description = new ArrayList<>();
         this.editor.getSkillButton(skill.getId()).addSkillBonusTooltip(description);
         skill.setDescription(description);
      });
      this.editor.rebuildWidgets();
   }

   private void addSelectedSkillsDescriptionLine() {
      this.editor.getSelectedSkills().forEach(skill -> {
         List<MutableComponent> description = skill.getDescription();
         if (description == null) {
            description = new ArrayList<>();
            skill.setDescription(description);
         }

         description.add(Component.empty().withStyle(TooltipHelper.getSkillBonusStyle(true)));
      });
      this.editor.saveSelectedSkills();
      this.editor.rebuildWidgets();
   }

   private void removeSelectedSkillsDescription() {
      this.editor.getSelectedSkills().forEach(skill -> skill.setDescription(null));
      this.editor.saveSelectedSkills();
      this.editor.rebuildWidgets();
   }
}
