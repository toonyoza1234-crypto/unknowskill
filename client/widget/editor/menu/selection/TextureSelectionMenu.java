package daripher.skilltree.client.widget.editor.menu.selection;

import daripher.skilltree.client.data.SkillTexturesData;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextureSelectionMenu extends EditorMenu {
   @NotNull
   private Consumer<ResourceLocation> responder = v -> {
   };
   private final TextureSelectionList selectionList;
   private final Runnable onInit;
   private final String texturesFolder;
   private int selectionListRows = 10;
   private int selectionListColumns = 10;

   public TextureSelectionMenu(
      SkillTreeEditor editor, @Nullable EditorMenu previousMenu, TextureSelectionList selectionList, String texturesFolder, Runnable onInit
   ) {
      super(editor, previousMenu);
      this.selectionList = selectionList;
      this.onInit = onInit;
      this.texturesFolder = texturesFolder;
   }

   @Override
   public void init() {
      this.clearWidgets();
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      this.editor.addLabel(0, 0, "Folder", ChatFormatting.GOLD);
      this.editor.increaseHeight(19);
      this.editor
         .addTextField(0, 0, 200, 14, this.texturesFolder)
         .setSoftFilter(SkillTexturesData::isTextureFolder)
         .setSuggestionProvider(SkillTexturesData::autocompleteFolderName)
         .setResponder(v -> {
            this.selectionList.setElementsList(SkillTexturesData.getTexturesInFolder(v));
            this.selectionList.setColumns(this.selectionListColumns);
            this.selectionList.setRows(this.selectionListRows);
            this.editor.setHeight(this.selectionList.getY() + this.selectionList.getHeight() + 10);
         });
      this.editor.increaseHeight(19);
      this.editor.addLabel(0, 0, "Search", ChatFormatting.GOLD);
      this.editor.increaseHeight(19);
      this.editor.addTextField(0, 0, 200, 14, "").setFocused().setResponder(this.selectionList::setSearchString);
      this.editor.increaseHeight(29);
      this.selectionList.setX(this.editor.getWidgetsX(0));
      this.selectionList.setY(this.editor.getWidgetsY(0));
      this.selectionList.setColumns(this.selectionListColumns);
      this.selectionList.setRows(this.selectionListRows);
      this.editor.increaseHeight(this.selectionList.getHeight() + 10);
      this.selectionList.setResponder(this.responder);
      this.addWidget(this.selectionList);
      this.onInit.run();
   }

   public TextureSelectionMenu setResponder(@NotNull Consumer<ResourceLocation> responder) {
      this.responder = responder;
      return this;
   }

   public TextureSelectionMenu setSelectionListGridSize(int rows, int columns) {
      this.selectionListRows = rows;
      this.selectionListColumns = columns;
      return this;
   }
}
