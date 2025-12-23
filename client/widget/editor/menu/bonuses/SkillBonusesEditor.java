package daripher.skilltree.client.widget.editor.menu.bonuses;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.List;
import net.minecraft.network.chat.Component;

public class SkillBonusesEditor extends EditorMenu {
   public SkillBonusesEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
      super(editor, previousMenu);
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      if (this.editor.canEditSkillBonuses()) {
         SkillBonus<?> defaultBonus = ((SkillBonus.Serializer)PSTSkillBonuses.ATTRIBUTE.get()).createDefaultInstance();
         this.editor
            .addSelectionMenu(110, -29, 90, defaultBonus)
            .setResponder(skillBonus -> this.addSkillBonus(this.editor, skillBonus))
            .setMessage(Component.literal("Add"));
         PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
         if (selectedSkill != null) {
            List<SkillBonus<?>> bonuses = selectedSkill.getBonuses();

            for (int i = 0; i < bonuses.size(); i++) {
               int bonusIndex = i;
               SkillBonus<?> bonus = bonuses.get(i);
               String message = bonus.getTooltip().getString();
               message = TooltipHelper.getTrimmedString(message, 190);
               this.editor.addButton(0, 0, 200, 14, message).setPressFunc(b -> this.editor.selectMenu(new SkillBonusEditor(this.editor, this, bonusIndex)));
               this.editor.increaseHeight(19);
            }
         }
      }
   }

   private void addSkillBonus(SkillTreeEditor editor, SkillBonus<?> skillBonus) {
      editor.getSelectedSkills().forEach(s -> s.getBonuses().add(skillBonus.copy()));
      editor.saveSelectedSkills();
      editor.selectMenu(editor.getSelectedMenu().previousMenu);
   }
}
