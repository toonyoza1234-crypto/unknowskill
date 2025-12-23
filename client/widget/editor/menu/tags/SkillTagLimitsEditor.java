package daripher.skilltree.client.widget.editor.menu.tags;

import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.widget.NumericTextField;
import daripher.skilltree.client.widget.TextField;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class SkillTagLimitsEditor extends EditorMenu {
   private final List<Pair<TextField, NumericTextField>> widgetPairs = new ArrayList<>();

   public SkillTagLimitsEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
      super(editor, previousMenu);
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      PassiveSkillTree skillTree = this.editor.getSkillTree();
      Map<String, Integer> limitations = skillTree.getSkillLimitations();
      List<String> tags = limitations.keySet().stream().toList();
      Runnable saveFunc = () -> {
         limitations.clear();

         for (Pair<TextField, NumericTextField> pair : this.widgetPairs) {
            int limit = (int)((NumericTextField)pair.getValue()).getNumericValue();
            if (limit != 0) {
               String tag = ((TextField)pair.getKey()).getValue();
               limitations.put(tag, limit);
            }
         }

         SkillTreeEditorData.saveEditorSkillTree(skillTree);
      };

      for (int i = 0; i < limitations.size(); i++) {
         TextField tagEditor = this.editor.addTextField(0, 0, 155, 14, tags.get(i));
         NumericTextField limitEditor = this.editor.addNumericTextField(160, 0, 40, 14, (double)limitations.get(tags.get(i)).intValue());
         tagEditor.setResponder(v -> saveFunc.run());
         this.widgetPairs.add(Pair.of(tagEditor, limitEditor));
         limitEditor.setNumericFilter(d -> d >= 0.0).setNumericResponder(v -> {
            saveFunc.run();
            if (v == 0.0) {
               this.editor.rebuildWidgets();
            }
         });
         this.editor.increaseHeight(19);
      }
   }
}
