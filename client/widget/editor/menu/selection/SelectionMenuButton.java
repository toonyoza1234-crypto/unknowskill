package daripher.skilltree.client.widget.editor.menu.selection;

import daripher.skilltree.client.widget.Button;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class SelectionMenuButton<T> extends Button {
   protected SelectionList<T> selectionList;
   protected Runnable onMenuInit = () -> {
   };
   protected Consumer<T> responder = t -> {
   };
   protected int selectionListRows = 10;
   protected int selectionListColumns = 10;

   public SelectionMenuButton(SkillTreeEditor editor, int x, int y, int width, String message, Collection<T> values) {
      super(x, y, width, 14, Component.literal(message));
      this.selectionList = new TextSelectionList<>(0, 0, 190, 14, values).setRows(8);
      this.setPressFunc(b -> this.selectMenu(editor));
   }

   public SelectionMenuButton(SkillTreeEditor editor, int x, int y, int width, Collection<T> values) {
      this(editor, x, y, width, "", values);
   }

   public SelectionMenuButton<T> setResponder(Consumer<T> responder) {
      this.responder = responder;
      return this;
   }

   public SelectionMenuButton<T> setValue(T value) {
      this.selectionList.selectElement(value);
      return this;
   }

   public SelectionMenuButton<T> setElementNameGetter(Function<T, Component> nameGetter) {
      this.selectionList.setNameGetter(nameGetter);
      T value = this.selectionList.getSelectedElement();
      if (value != null) {
         this.setMessage(this.selectionList.getNameGetter().apply(value));
      }

      return this;
   }

   public SelectionMenuButton<T> setElementSize(int width, int height) {
      this.selectionList.setElementSize(width, height);
      return this;
   }

   public SelectionMenuButton<T> setSelectionListGridSize(int rows, int columns) {
      this.selectionListRows = rows;
      this.selectionListColumns = columns;
      return this;
   }

   public void setMenuInitFunc(Runnable onMenuInit) {
      this.onMenuInit = onMenuInit;
   }

   protected void selectMenu(SkillTreeEditor editor) {
      SelectionMenu<T> menu = new SelectionMenu<>(editor, editor.getSelectedMenu(), this.selectionList, this.onMenuInit).setResponder(this.responder);
      editor.selectMenu(menu);
   }
}
