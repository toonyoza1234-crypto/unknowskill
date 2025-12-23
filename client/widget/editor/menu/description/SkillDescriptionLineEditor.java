package daripher.skilltree.client.widget.editor.menu.description;

import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.skill.PassiveSkill;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class SkillDescriptionLineEditor extends EditorMenu {
   private final int selectedLine;

   public SkillDescriptionLineEditor(SkillTreeEditor editor, EditorMenu previousMenu, int selectedLine) {
      super(editor, previousMenu);
      this.selectedLine = selectedLine;
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      if (!this.editor.getSelectedSkills().isEmpty()) {
         PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
         if (selectedSkill != null) {
            if (this.canEditDescription()) {
               List<MutableComponent> description = selectedSkill.getDescription();
               this.editor.addConfirmationButton(110, 0, 90, 14, "Remove", "Confirm").setPressFunc(b -> this.removeDescriptionLine());
               this.editor.increaseHeight(29);
               if (description != null && this.selectedLine <= description.size()) {
                  MutableComponent component = description.get(this.selectedLine);
                  this.editor.addTextArea(0, 0, 200, 70, component.getString()).setResponder(this::setDescription);
                  this.editor.increaseHeight(75);
                  this.editor.addLabel(0, 0, "Color", ChatFormatting.GOLD);
                  Style originalStyle = component.getStyle();
                  TextColor textColor = originalStyle.getColor();
                  if (textColor == null) {
                     textColor = TextColor.fromRgb(16777215);
                  }

                  String color = Integer.toHexString(textColor.getValue());
                  this.editor.addTextField(120, 0, 80, 14, color).setSoftFilter(SkillDescriptionLineEditor::isColorString).setResponder(v -> {
                     if (isColorString(v)) {
                        int rgb = Integer.parseInt(v, 16);
                        this.setDescriptionStyle(s -> s.withColor(rgb));
                     }
                  });
                  this.editor.increaseHeight(19);
                  this.editor.addLabel(0, 0, "Bold", ChatFormatting.GOLD);
                  this.editor.addCheckBox(186, 0, originalStyle.isBold()).setResponder(v -> {
                     this.setDescriptionStyle(s -> s.withBold(v));
                     this.editor.rebuildWidgets();
                  });
                  this.editor.increaseHeight(19);
                  this.editor.addLabel(0, 0, "Italic", ChatFormatting.GOLD);
                  this.editor.addCheckBox(186, 0, originalStyle.isItalic()).setResponder(v -> {
                     this.setDescriptionStyle(s -> s.withItalic(v));
                     this.editor.rebuildWidgets();
                  });
                  this.editor.increaseHeight(19);
                  this.editor.addLabel(0, 0, "Underline", ChatFormatting.GOLD);
                  this.editor.addCheckBox(186, 0, originalStyle.isUnderlined()).setResponder(v -> {
                     this.setDescriptionStyle(s -> s.withUnderlined(v));
                     this.editor.rebuildWidgets();
                  });
                  this.editor.increaseHeight(19);
                  this.editor.addLabel(0, 0, "Strikethrough", ChatFormatting.GOLD);
                  this.editor.addCheckBox(186, 0, originalStyle.isStrikethrough()).setResponder(v -> {
                     this.setDescriptionStyle(s -> s.withStrikethrough(v));
                     this.editor.rebuildWidgets();
                  });
                  this.editor.increaseHeight(19);
                  this.editor.addLabel(0, 0, "Obfuscated", ChatFormatting.GOLD);
                  this.editor.addCheckBox(186, 0, originalStyle.isObfuscated()).setResponder(v -> {
                     this.setDescriptionStyle(s -> s.withObfuscated(v));
                     this.editor.rebuildWidgets();
                  });
                  this.editor.increaseHeight(19);
               } else {
                  this.editor.selectMenu(this.previousMenu);
               }
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

   private void removeDescriptionLine() {
      this.editor.getSelectedSkills().forEach(skill -> {
         List<MutableComponent> description = skill.getDescription();
         Objects.requireNonNull(description);
         description.remove(this.selectedLine);
      });
      this.editor.saveSelectedSkills();
      this.editor.selectMenu(this.previousMenu);
      this.editor.rebuildWidgets();
   }

   private void setDescription(String line) {
      this.editor.getSelectedSkills().forEach(skill -> {
         List<MutableComponent> description = skill.getDescription();
         Objects.requireNonNull(description);
         MutableComponent component = description.get(this.selectedLine);
         Style style = component.getStyle();
         description.set(this.selectedLine, Component.literal(line).withStyle(style));
      });
      this.editor.saveSelectedSkills();
   }

   private void setDescriptionStyle(Function<Style, Style> styleFunc) {
      this.editor.getSelectedSkills().forEach(skill -> {
         List<MutableComponent> description = skill.getDescription();
         Objects.requireNonNull(description);
         MutableComponent component = description.get(this.selectedLine);
         Style style = styleFunc.apply(component.getStyle());
         description.set(this.selectedLine, component.withStyle(style));
         SkillTreeEditorData.saveEditorSkill(skill);
         SkillTreeEditorData.loadEditorSkill(skill.getId());
      });
   }

   private static boolean isColorString(String v) {
      return v.matches("^[a-fA-F0-9]{6}");
   }
}
