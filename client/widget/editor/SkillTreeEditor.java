package daripher.skilltree.client.widget.editor;

import daripher.skilltree.attribute.AttributesHelper;
import daripher.skilltree.client.data.SkillTexturesData;
import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.Button;
import daripher.skilltree.client.widget.CheckBox;
import daripher.skilltree.client.widget.ConfirmationButton;
import daripher.skilltree.client.widget.Label;
import daripher.skilltree.client.widget.NumericTextField;
import daripher.skilltree.client.widget.TextArea;
import daripher.skilltree.client.widget.TextField;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.client.widget.editor.menu.MainEditorMenu;
import daripher.skilltree.client.widget.editor.menu.selection.SelectionList;
import daripher.skilltree.client.widget.editor.menu.selection.SelectionMenuButton;
import daripher.skilltree.client.widget.editor.menu.selection.TextSelectionList;
import daripher.skilltree.client.widget.editor.menu.selection.TextureSelectionMenuButton;
import daripher.skilltree.client.widget.group.WidgetGroup;
import daripher.skilltree.client.widget.skill.SkillButton;
import daripher.skilltree.client.widget.skill.SkillButtons;
import daripher.skilltree.init.PSTDamageConditions;
import daripher.skilltree.init.PSTEnchantmentConditions;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.init.PSTFloatFunctions;
import daripher.skilltree.init.PSTItemConditions;
import daripher.skilltree.init.PSTLivingConditions;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.init.PSTSkillRequirements;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.function.FloatFunction;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.damage.DamageCondition;
import daripher.skilltree.skill.bonus.predicate.enchantment.EnchantmentCondition;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.requirement.SkillRequirement;
import daripher.skilltree.skill.requirement.StatRequirement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillTreeEditor extends WidgetGroup<AbstractWidget> {
   private final SkillButtons skillButtons;
   private final SkillSelector skillSelector;
   private final SkillMirrorer skillMirrorer;
   private final SkillDragger skillDragger;
   @NotNull
   private EditorMenu selectedMenu = new MainEditorMenu(this);

   public SkillTreeEditor(SkillButtons skillButtons) {
      super(0, 0, 0, 0);
      this.skillButtons = skillButtons;
      this.skillSelector = new SkillSelector(this, skillButtons);
      this.skillMirrorer = new SkillMirrorer(this);
      this.skillDragger = new SkillDragger(this);
   }

   public void init() {
      this.clearWidgets();
      this.addWidget(this.selectedMenu);
      this.addWidget(this.skillSelector);
      this.addWidget(this.skillDragger);
      this.addWidget(this.skillMirrorer);
      this.selectedMenu.init();
   }

   public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.skillMirrorer.render(graphics, mouseX, mouseY, partialTick);
      if (!this.skillSelector.getSelectedSkills().isEmpty()) {
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -587202560);
      }

      super.render(graphics, mouseX, mouseY, partialTick);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 256) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else if (this.selectedMenu.previousMenu != null) {
         this.selectMenu(this.selectedMenu.previousMenu);
         return true;
      } else if (!this.skillSelector.getSelectedSkills().isEmpty()) {
         this.skillSelector.clearSelection();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void selectMenu(EditorMenu menu) {
      if (menu != null) {
         this.selectedMenu = menu;
         this.rebuildWidgets();
      }
   }

   public Button addButton(int x, int y, int width, int height, String message) {
      return this.addButton(x, y, width, height, Component.literal(message));
   }

   public Button addButton(int x, int y, int width, int height, Component message) {
      return this.addWidget(new Button(this.getWidgetsX(x), this.getWidgetsY(y), width, height, message));
   }

   public ConfirmationButton addConfirmationButton(int x, int y, int width, int height, String message, String confirmationMessage) {
      ConfirmationButton button = new ConfirmationButton(this.getWidgetsX(x), this.getWidgetsY(y), width, height, Component.literal(message));
      button.setConfirmationMessage(Component.literal(confirmationMessage));
      return this.addWidget(button);
   }

   public TextField addTextField(int x, int y, int width, int height, String defaultValue) {
      return this.addWidget(new TextField(this.getWidgetsX(x), this.getWidgetsY(y), width, height, defaultValue));
   }

   public NumericTextField addNumericTextField(int x, int y, int width, int height, double defaultValue) {
      return this.addWidget(new NumericTextField(this.getWidgetsX(x), this.getWidgetsY(y), width, height, defaultValue));
   }

   public TextArea addTextArea(int x, int y, int width, int height, String defaultValue) {
      return this.addWidget(new TextArea(this.getWidgetsX(x), this.getWidgetsY(y), width, height, defaultValue));
   }

   public Label addLabel(int x, int y, String text, ChatFormatting... styles) {
      MutableComponent message = Component.literal(text);

      for (ChatFormatting style : styles) {
         message.withStyle(style);
      }

      return this.addWidget(new Label(this.getWidgetsX(x), this.getWidgetsY(y), message));
   }

   public CheckBox addCheckBox(int x, int y, boolean value) {
      return this.addWidget(new CheckBox(this.getWidgetsX(x), this.getWidgetsY(y), value));
   }

   public TextureSelectionMenuButton addTextureSelectionMenu(int x, int y, int width, ResourceLocation currentValue, String folder) {
      Collection<ResourceLocation> values = SkillTexturesData.getTexturesInFolder(folder);
      x = this.getWidgetsX(x);
      y = this.getWidgetsY(y);
      String message = currentValue.toString();
      TextureSelectionMenuButton button = (TextureSelectionMenuButton)new TextureSelectionMenuButton(this, x, y, width, message, folder, values)
         .setValue(currentValue)
         .setElementNameGetter(TooltipHelper::getTextureName);
      return this.addWidget(button);
   }

   public SelectionMenuButton<SkillBonus> addSelectionMenu(int x, int y, int width, SkillBonus defaultValue) {
      Collection<SkillBonus> values = PSTSkillBonuses.bonusList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(b -> Component.literal(PSTSkillBonuses.getName(b)));
   }

   public SelectionMenuButton<SkillRequirement> addSelectionMenu(int x, int y, int width, SkillRequirement defaultValue) {
      Collection<SkillRequirement> values = PSTSkillRequirements.requirementList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(b -> Component.literal(PSTSkillRequirements.getName(b)));
   }

   public SelectionMenuButton<StatRequirement> addSelectionMenu(int x, int y, int width, StatRequirement defaultValue) {
      Collection<StatRequirement> values = this.getDefaultRequirementInstances();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(r -> Component.literal(r.getStatTypeId().getPath()));
   }

   private Collection<StatRequirement> getDefaultRequirementInstances() {
      return ForgeRegistries.STAT_TYPES.getValues().stream().map(SkillTreeEditor::createDefaultRequirement).filter(Objects::nonNull).toList();
   }

   @Nullable
   private static StatRequirement createDefaultRequirement(StatType<?> statType) {
      ResourceLocation statId = ForgeRegistries.STAT_TYPES.getKey(statType);
      Registry<Object> statRegistry = statType.getRegistry();
      Object stat = statRegistry.byId(0);
      return stat == null ? null : new StatRequirement(statId, statRegistry.getKey(stat), 1);
   }

   public SelectionMenuButton<FloatFunction> addSelectionMenu(int x, int y, int width, FloatFunction defaultValue) {
      Collection<FloatFunction> values = PSTFloatFunctions.providerList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(p -> Component.literal(PSTFloatFunctions.getName(p)));
   }

   public SelectionMenuButton<Attribute> addSelectionMenu(int x, int y, int width, Attribute defaultValue) {
      Collection<Attribute> values = AttributesHelper.attributeList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(a -> Component.literal(AttributesHelper.getName(a)));
   }

   public SelectionMenuButton<LivingEntityPredicate> addSelectionMenu(int x, int y, int width, LivingEntityPredicate defaultValue) {
      Collection<LivingEntityPredicate> values = PSTLivingConditions.conditionsList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(c -> Component.literal(PSTLivingConditions.getName(c)));
   }

   public SelectionMenuButton<LivingMultiplier> addSelectionMenu(int x, int y, int width, LivingMultiplier defaultValue) {
      Collection<LivingMultiplier> values = PSTLivingMultipliers.multiplierList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(m -> Component.literal(PSTLivingMultipliers.getName(m)));
   }

   public SelectionMenuButton<ItemStackPredicate> addSelectionMenu(int x, int y, int width, ItemStackPredicate defaultValue) {
      Collection<ItemStackPredicate> values = PSTItemConditions.conditionsList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(c -> Component.literal(PSTItemConditions.getName(c)));
   }

   public SelectionMenuButton<MobEffect> addSelectionMenu(int x, int y, int width, MobEffect defaultValue) {
      Collection<MobEffect> values = ForgeRegistries.MOB_EFFECTS.getValues();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(e -> Component.literal(e.getDescriptionId()));
   }

   public SelectionMenuButton<DamageCondition> addSelectionMenu(int x, int y, int width, DamageCondition defaultValue) {
      List<DamageCondition> values = PSTDamageConditions.conditionsList();
      return this.addSelectionMenu(x, y, width, values)
         .setValue(defaultValue)
         .setElementNameGetter(c -> Component.translatable(PSTDamageConditions.getName(c)));
   }

   public SelectionMenuButton<SkillEventListener> addSelectionMenu(int x, int y, int width, SkillEventListener defaultValue) {
      List<SkillEventListener> values = PSTEventListeners.eventsList();
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue).setElementNameGetter(e -> Component.translatable(PSTEventListeners.getName(e)));
   }

   public SelectionMenuButton<EnchantmentCondition> addSelectionMenu(int x, int y, int width, EnchantmentCondition defaultValue) {
      List<EnchantmentCondition> values = PSTEnchantmentConditions.conditionsList();
      return this.addSelectionMenu(x, y, width, values)
         .setValue(defaultValue)
         .setElementNameGetter(c -> Component.translatable(PSTEnchantmentConditions.getName(c)));
   }

   public <T extends Enum<T>> SelectionMenuButton<T> addSelectionMenu(int x, int y, int width, T defaultValue) {
      List<T> values = getEnumValues(defaultValue);
      return this.addSelectionMenu(x, y, width, values).setValue(defaultValue);
   }

   public <T> SelectionMenuButton<T> addSelectionMenu(int x, int y, int width, Collection<T> values) {
      return this.addWidget(new SelectionMenuButton<>(this, this.getWidgetsX(x), this.getWidgetsY(y), width, values));
   }

   public <T> SelectionList<T> addSelection(int x, int y, int width, T defaultValue, Collection<T> values, int maxDisplayed) {
      SelectionList<T> widget = new TextSelectionList<>(this.getWidgetsX(x), this.getWidgetsY(y), width, 14, values)
         .setRows(maxDisplayed)
         .selectElement(defaultValue);
      return this.addWidget(widget);
   }

   public SelectionList<Operation> addOperationSelection(int x, int y, int width, Operation defaultValue) {
      List<Operation> values = List.of(Operation.values());
      return this.addSelection(x, y, width, defaultValue, values, 1).setNameGetter(TooltipHelper::getOperationName);
   }

   public <T extends Enum<T>> SelectionList<T> addSelection(int x, int y, int width, int maxDisplayed, T defaultValue) {
      List<T> values = getEnumValues(defaultValue);
      return this.addSelection(x, y, width, defaultValue, values, maxDisplayed);
   }

   @NotNull
   private static <T extends Enum<T>> List<T> getEnumValues(T defaultValue) {
      Class<T> enumType = (Class<T>)defaultValue.getClass();
      return List.of(enumType.getEnumConstants());
   }

   public void addMirrorerWidgets() {
      this.skillMirrorer.init();
   }

   public Set<PassiveSkill> getSelectedSkills() {
      return this.skillSelector.getSelectedSkills();
   }

   @Nullable
   public PassiveSkill getFirstSelectedSkill() {
      return this.skillSelector.getFirstSelectedSkill();
   }

   public SkillMirrorer getSkillMirrorer() {
      return this.skillMirrorer;
   }

   public void saveSelectedSkills() {
      this.skillSelector.getSelectedSkills().forEach(SkillTreeEditorData::saveEditorSkill);
   }

   public int getWidgetsY(int y) {
      return this.getHeight() + y;
   }

   public int getWidgetsX(int x) {
      return this.getX() + 5 + x;
   }

   public float getScrollX() {
      return this.skillButtons.getScrollX();
   }

   public float getScrollY() {
      return this.skillButtons.getScrollY();
   }

   public float getZoom() {
      return this.skillButtons.getZoom();
   }

   public void increaseHeight(int delta) {
      this.setHeight(this.getHeight() + delta);
   }

   public PassiveSkillTree getSkillTree() {
      return this.skillButtons.getSkillTree();
   }

   public List<PassiveSkill> getSkills() {
      return this.getSkillTree().getSkillIds().stream().map(SkillTreeEditorData::getEditorSkill).toList();
   }

   public Collection<SkillButton> getSkillButtons() {
      return this.skillButtons.getWidgets();
   }

   public void addSkillButton(PassiveSkill skill) {
      SkillButton button = this.skillButtons.addSkillButton(skill, () -> 0.0F);
      button.skillLearned = true;
   }

   public void updateSkillConnections() {
      this.skillButtons.updateSkillConnections();
   }

   @Override
   public void rebuildWidgets() {
      super.rebuildWidgets();
      this.updateSkillConnections();
   }

   public boolean canEdit(Function<PassiveSkill, ?> function) {
      return this.getSelectedSkills().stream().map(function).distinct().count() <= 1L;
   }

   public void removeSkillButton(PassiveSkill skill) {
      this.skillButtons.getWidgets().removeIf(button -> button.skill == skill);
   }

   public SkillButton getSkillButton(ResourceLocation skillId) {
      return this.skillButtons.getWidgetById(skillId);
   }

   public int getScreenWidth() {
      return this.skillButtons.getWidth();
   }

   public int getScreenHeight() {
      return this.skillButtons.getHeight();
   }

   @NotNull
   public EditorMenu getSelectedMenu() {
      return this.selectedMenu;
   }

   public boolean canEditSkillBonuses() {
      PassiveSkill selectedSkill = this.getFirstSelectedSkill();
      if (selectedSkill == null) {
         return false;
      } else {
         for (PassiveSkill otherSkill : this.getSelectedSkills()) {
            if (otherSkill != selectedSkill) {
               List<SkillBonus<?>> bonuses = otherSkill.getBonuses();
               List<SkillBonus<?>> otherBonuses = selectedSkill.getBonuses();
               if (bonuses.size() != otherBonuses.size()) {
                  return false;
               }

               for (int i = 0; i < bonuses.size(); i++) {
                  if (!bonuses.get(i).sameBonus(otherBonuses.get(i))) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean canEditSkillRequirements() {
      PassiveSkill selectedSkill = this.getFirstSelectedSkill();
      if (selectedSkill == null) {
         return false;
      } else {
         for (PassiveSkill otherSkill : this.getSelectedSkills()) {
            if (otherSkill != selectedSkill) {
               List<SkillRequirement<?>> requirements = otherSkill.getRequirements();
               List<SkillRequirement<?>> otherRequirements = selectedSkill.getRequirements();
               if (requirements.size() != otherRequirements.size()) {
                  return false;
               }

               for (int i = 0; i < requirements.size(); i++) {
                  if (!requirements.get(i).equals(otherRequirements.get(i))) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }
}
