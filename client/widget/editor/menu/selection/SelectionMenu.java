package daripher.skilltree.client.widget.editor.menu.selection;

import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectionMenu<T> extends EditorMenu {
   @NotNull
   private Consumer<T> responder = v -> {
   };
   private final SelectionList<T> selectionList;
   private final Runnable onInit;

   public SelectionMenu(SkillTreeEditor editor, @Nullable EditorMenu previousMenu, SelectionList<T> selectionList, Runnable onInit) {
      super(editor, previousMenu);
      this.selectionList = selectionList;
      this.onInit = onInit;
   }

   @Override
   public void init() {
      this.clearWidgets();
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      this.editor.addTextField(0, 0, 200, 14, "").setHint("Search").setFocused().setResponder(this.selectionList::setSearchString);
      this.editor.increaseHeight(19);
      this.selectionList.setX(this.editor.getWidgetsX(0));
      this.selectionList.setY(this.editor.getWidgetsY(0));
      this.editor.increaseHeight(this.selectionList.getHeight() + 10);
      this.selectionList.setResponder(this.responder);
      this.addWidget(this.selectionList);
      this.onInit.run();
   }

   public SelectionMenu<T> setResponder(@NotNull Consumer<T> responder) {
      this.responder = responder;
      return this;
   }
}
