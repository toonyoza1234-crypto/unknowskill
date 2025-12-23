package daripher.skilltree.client.widget.editor.menu;

import daripher.skilltree.client.data.SkillTexturesData;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.skill.PassiveSkill;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class SkillTexturesEditor extends EditorMenu {
   public SkillTexturesEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
      super(editor, previousMenu);
   }

   @Override
   public void init() {
      this.editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> this.editor.selectMenu(this.previousMenu));
      this.editor.increaseHeight(29);
      PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
      if (selectedSkill != null) {
         this.addTextureEditorButton("Frame Texture", this::setFrameTextures, PassiveSkill::getFrameTexture, 3, 2, 95, 19, 48, 16);
         this.addTextureEditorButton("Tooltip Frame", this::setTooltipFrameTextures, PassiveSkill::getTooltipFrameTexture, 4, 1, 190, 19, 88, 16);
         this.addTextureEditorButton("Icon Texture", this::setIconTextures, PassiveSkill::getIconTexture, 10, 10, 19, 19, 16, 16);
      }
   }

   private void addTextureEditorButton(
      String label,
      Consumer<ResourceLocation> setTextureFunction,
      Function<PassiveSkill, ResourceLocation> textureProvider,
      int rows,
      int columns,
      int elementWidth,
      int elementHeight,
      int elementTextureWidth,
      int elementTextureHeight
   ) {
      if (this.editor.canEdit(textureProvider)) {
         PassiveSkill selectedSkill = this.editor.getFirstSelectedSkill();
         ResourceLocation texture = textureProvider.apply(selectedSkill);
         this.editor.addLabel(0, 0, label, ChatFormatting.GOLD);
         this.editor.increaseHeight(19);
         String textureFolder = SkillTexturesData.getTextureFolder(texture);
         this.editor
            .addTextureSelectionMenu(0, 0, 200, texture, textureFolder)
            .setElementTextureSize(elementTextureWidth, elementTextureHeight)
            .setSelectionListGridSize(rows, columns)
            .setElementNameGetter(TooltipHelper::getTextureName)
            .setElementSize(elementWidth, elementHeight)
            .setResponder(setTextureFunction);
         this.editor.increaseHeight(19);
      }
   }

   private void setFrameTextures(ResourceLocation value) {
      this.editor.getSelectedSkills().forEach(s -> s.setBackgroundTexture(value));
      this.editor.saveSelectedSkills();
   }

   private void setTooltipFrameTextures(ResourceLocation value) {
      this.editor.getSelectedSkills().forEach(s -> s.setBorderTexture(value));
      this.editor.saveSelectedSkills();
   }

   private void setIconTextures(ResourceLocation value) {
      this.editor.getSelectedSkills().forEach(s -> s.setIconTexture(value));
      this.editor.saveSelectedSkills();
   }
}
