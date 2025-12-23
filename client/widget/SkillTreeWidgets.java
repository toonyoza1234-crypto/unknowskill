package daripher.skilltree.client.widget;

import com.google.common.collect.Streams;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.widget.group.WidgetGroup;
import daripher.skilltree.client.widget.skill.SkillButton;
import daripher.skilltree.client.widget.skill.SkillButtons;
import daripher.skilltree.client.widget.skill.SkillConnection;
import daripher.skilltree.config.ClientConfig;
import daripher.skilltree.config.ServerConfig;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.exp.ExpHelper;
import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.network.message.GainSkillPointMessage;
import daripher.skilltree.network.message.LearnSkillMessage;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.requirement.SkillRequirement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

public class SkillTreeWidgets extends WidgetGroup<AbstractWidget> {
   private final SkillButtons skills;
   private final PassiveSkillTree skillTree;
   private final List<ResourceLocation> learnedSkills = new ArrayList<>();
   public final List<ResourceLocation> newlyLearnedSkills = new ArrayList<>();
   private final List<SkillButton> startingPoints = new ArrayList<>();
   private Button buyButton;
   private Label pointsInfo;
   private ProgressBar progressBar;
   private ScrollableComponentList statsInfo;
   public int skillPoints;
   private boolean showStats;
   private boolean showProgressInNumbers;
   private String search = "";
   private final LocalPlayer player;

   public SkillTreeWidgets(LocalPlayer player, SkillButtons skills, PassiveSkillTree skillTree) {
      super(0, 0, 0, 0);
      this.skills = skills;
      this.skillTree = skillTree;
      this.player = player;
      this.readPlayerData(player);
   }

   public void init() {
      this.progressBar = new ProgressBar(this.width / 2 - 117, this.height - 17, b -> this.toggleProgressDisplayMode());
      this.progressBar.showProgressInNumbers = this.showProgressInNumbers;
      this.addWidget(this.progressBar);
      this.addTopWidgets();
      if (!ServerConfig.enable_exp_exchange) {
         this.progressBar.visible = false;
         this.buyButton.visible = false;
      }

      this.statsInfo = new ScrollableComponentList(48, this.height - 60);
      this.statsInfo.setComponents(this.getMergedSkillBonusesTooltips());
      this.addWidget(this.statsInfo);
      this.startingPoints.clear();
      this.skills.getWidgets().stream().filter(button -> button.skill.isStartingPoint()).forEach(this.startingPoints::add);
      this.highlightSkills();
      this.updateSearch();
   }

   @Override
   protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.updateBuyPointButton();
      Style pointsStyle = Style.EMPTY.withColor(16573030);
      Component pointsLeft = Component.literal(this.skillPoints + "").withStyle(pointsStyle);
      this.pointsInfo.setMessage(Component.translatable("widget.skill_points_left", new Object[]{pointsLeft}));
      this.statsInfo.setX(this.width - this.statsInfo.getWidth() - 10);
      this.statsInfo.visible = this.showStats;
      super.renderWidget(graphics, mouseX, mouseY, partialTick);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      AbstractWidget widget = this.getWidgetAt(mouseX, mouseY);
      if (widget != null) {
         widget.setFocused(true);
         return widget.mouseClicked(mouseX, mouseY, button);
      } else {
         SkillButton skillButton = this.skills.getWidgetAt(mouseX, mouseY);
         if (skillButton == null) {
            return false;
         } else if (button == 0) {
            this.playButtonSound();
            this.skillButtonPressed(skillButton);
            return true;
         } else if (button == 1) {
            ClientConfig.toggleFavoriteSkill(skillButton.skill);
            this.playButtonSound();
            return true;
         } else {
            return false;
         }
      }
   }

   private void updateSearch() {
      if (this.search.isEmpty()) {
         for (SkillButton button : this.skills.getWidgets()) {
            button.searched = false;
         }
      } else {
         label36:
         for (SkillButton button : this.skills.getWidgets()) {
            for (MutableComponent component : button.getSkillTooltip(this.skillTree)) {
               if (component.getString().toLowerCase().contains(this.search.toLowerCase())) {
                  button.searched = true;
                  continue label36;
               }
            }

            button.searched = false;
         }
      }
   }

   private void playButtonSound() {
      SoundManager soundManager = Minecraft.getInstance().getSoundManager();
      SimpleSoundInstance sound = SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
      soundManager.play(sound);
   }

   private void highlightSkills() {
      if (this.skillPoints != 0) {
         if (this.getLearnedSkillsOnTree().isEmpty() && this.newlyLearnedSkills.isEmpty()) {
            this.startingPoints.stream().filter(button -> this.canLearnSkill(button.skill)).forEach(SkillButton::setCanLearn);
         } else if (this.learnedSkills.size() + this.newlyLearnedSkills.size() < ServerConfig.max_skill_points) {
            this.skills.getSkillConnections().forEach(connection -> {
               SkillButton button1 = connection.getFirstButton();
               SkillButton button2 = connection.getSecondButton();
               if (button1.skillLearned != button2.skillLearned) {
                  if (connection.getType() != SkillConnection.Type.ONE_WAY && !button1.skillLearned && this.canLearnSkill(button1.skill)) {
                     button1.setCanLearn();
                     button1.setActive();
                  }

                  if (!button2.skillLearned && this.canLearnSkill(button2.skill)) {
                     button2.setCanLearn();
                     button2.setActive();
                  }
               }
            });
         }
      }
   }

   private List<ResourceLocation> getLearnedSkillsOnTree() {
      return this.learnedSkills.stream().filter(this.skillTree.getSkillIds()::contains).toList();
   }

   private void addTopWidgets() {
      Component buyButtonText = Component.translatable("widget.buy_skill_button");
      Component pointsInfoText = Component.translatable("widget.skill_points_left", new Object[]{100});
      Component confirmButtonText = Component.translatable("widget.confirm_button");
      Component cancelButtonText = Component.translatable("widget.cancel_button");
      Component showStatsButtonText = Component.translatable("widget.show_stats");
      Font font = Minecraft.getInstance().font;
      int buttonWidth = Math.max(font.width(buyButtonText), font.width(pointsInfoText));
      buttonWidth = Math.max(buttonWidth, font.width(confirmButtonText));
      buttonWidth = Math.max(buttonWidth, font.width(cancelButtonText));
      buttonWidth += 20;
      int buttonsY = 8;
      Button showStatsButton = new Button(this.width - buttonWidth - 8, buttonsY, buttonWidth, 14, showStatsButtonText);
      showStatsButton.setPressFunc(b -> this.showStats ^= true);
      this.addWidget(showStatsButton);
      TextField searchField = new TextField(8, buttonsY, buttonWidth, 14, this.search);
      ((TextField)this.addWidget(searchField)).setHint("Search...").setResponder(s -> {
         this.search = s;
         this.updateSearch();
      });
      this.buyButton = new Button(this.width / 2 - 8 - buttonWidth, buttonsY, buttonWidth, 14, buyButtonText);
      this.buyButton.setPressFunc(b -> this.buySkillPoint());
      this.addWidget(this.buyButton);
      this.pointsInfo = new Label(this.width / 2 + 8, buttonsY, buttonWidth, 14, Component.empty());
      if (!ServerConfig.enable_exp_exchange) {
         this.pointsInfo.setX(this.width / 2 - buttonWidth / 2);
      }

      this.addWidget(this.pointsInfo);
      buttonsY += 20;
      Button confirmButton = new Button(this.width / 2 - 8 - buttonWidth, buttonsY, buttonWidth, 14, confirmButtonText);
      confirmButton.setPressFunc(b -> this.confirmLearnSkills());
      this.addWidget(confirmButton);
      Button cancelButton = new Button(this.width / 2 + 8, buttonsY, buttonWidth, 14, cancelButtonText);
      cancelButton.setPressFunc(b -> this.cancelLearnSkills());
      this.addWidget(cancelButton);
      confirmButton.active = cancelButton.active = !this.newlyLearnedSkills.isEmpty();
   }

   private static void addToMergeList(SkillBonus<?> b, List<SkillBonus<?>> bonuses) {
      Optional<SkillBonus<?>> same = bonuses.stream().filter(b::canMerge).findAny();
      if (same.isPresent()) {
         bonuses.remove(same.get());
         bonuses.add(same.get().copy().merge(b));
      } else {
         bonuses.add(b);
      }
   }

   private boolean canLearnSkill(PassiveSkill skill) {
      if (!this.player.isCreative()) {
         for (SkillRequirement<?> requirement : skill.getRequirements()) {
            if (!requirement.test(this.player)) {
               return false;
            }
         }
      }

      Map<String, Integer> limitations = this.skillTree.getSkillLimitations();

      for (String tag : skill.getTags()) {
         int limit = limitations.getOrDefault(tag, 0);
         if (limit > 0 && this.getLearnedSkillsWithTag(tag) >= (long)limit) {
            return false;
         }
      }

      return true;
   }

   private long getLearnedSkillsWithTag(String tag) {
      return Streams.concat(new Stream[]{this.learnedSkills.stream(), this.newlyLearnedSkills.stream()})
         .map(SkillsReloader::getSkillById)
         .filter(Objects::nonNull)
         .filter(skill -> skill.getTags().contains(tag))
         .count();
   }

   private void confirmLearnSkills() {
      this.newlyLearnedSkills.forEach(id -> this.learnSkill(this.skills.getWidgetById(id).skill));
      this.newlyLearnedSkills.clear();
   }

   private void cancelLearnSkills() {
      this.skillPoints = this.skillPoints + this.newlyLearnedSkills.size();
      this.newlyLearnedSkills.clear();
      this.rebuildWidgets();
   }

   private void buySkillPoint() {
      int currentLevel = this.getCurrentLevel();
      if (this.canBuySkillPoint(currentLevel)) {
         int cost = ServerConfig.getSkillPointCost(currentLevel);
         NetworkDispatcher.network_channel.sendToServer(new GainSkillPointMessage());
         this.player.giveExperiencePoints(-cost);
      }
   }

   private boolean canBuySkillPoint(int currentLevel) {
      if (!ServerConfig.enable_exp_exchange) {
         return false;
      } else if (this.isMaxLevel(currentLevel)) {
         return false;
      } else {
         int cost = ServerConfig.getSkillPointCost(currentLevel);
         return ExpHelper.getPlayerExp(this.player) >= (long)cost;
      }
   }

   private boolean isMaxLevel(int currentLevel) {
      return currentLevel >= ServerConfig.max_skill_points;
   }

   private int getCurrentLevel() {
      IPlayerSkills capability = PlayerSkillsProvider.get(this.player);
      int learnedSkills = capability.getPlayerSkills().size();
      int skillPoints = capability.getSkillPoints();
      return learnedSkills + skillPoints;
   }

   protected void skillButtonPressed(SkillButton button) {
      PassiveSkill skill = button.skill;
      if (!this.newlyLearnedSkills.isEmpty()) {
         int lastLearned = this.newlyLearnedSkills.size() - 1;
         if (this.newlyLearnedSkills.get(lastLearned).equals(skill.getId())) {
            this.skillPoints++;
            this.newlyLearnedSkills.remove(lastLearned);
            this.rebuildWidgets();
            return;
         }
      }

      if (button.canLearn) {
         this.skillPoints--;
         this.newlyLearnedSkills.add(skill.getId());
         this.rebuildWidgets();
      }
   }

   protected void learnSkill(PassiveSkill skill) {
      this.learnedSkills.add(skill.getId());
      NetworkDispatcher.network_channel.sendToServer(new LearnSkillMessage(skill));
      this.rebuildWidgets();
   }

   protected void updateBuyPointButton() {
      int currentLevel = this.getCurrentLevel();
      this.buyButton.active = false;
      if (!this.isMaxLevel(currentLevel)) {
         int pointCost = ServerConfig.getSkillPointCost(currentLevel);
         this.buyButton.active = ExpHelper.getPlayerExp(this.player) >= (long)pointCost;
      }
   }

   private void toggleProgressDisplayMode() {
      this.progressBar.showProgressInNumbers ^= true;
      this.showProgressInNumbers ^= true;
   }

   private void readPlayerData(LocalPlayer player) {
      IPlayerSkills capability = PlayerSkillsProvider.get(player);
      List<PassiveSkill> skills = capability.getPlayerSkills();
      skills.stream().map(PassiveSkill::getId).forEach(this.learnedSkills::add);
      this.skillPoints = capability.getSkillPoints();
   }

   private List<Component> getMergedSkillBonusesTooltips() {
      List<SkillBonus<?>> bonuses = new ArrayList<>();
      this.learnedSkills
         .stream()
         .map(this.skills::getWidgetById)
         .filter(Objects::nonNull)
         .map(button -> button.skill)
         .map(PassiveSkill::getBonuses)
         .flatMap(Collection::stream)
         .forEach(b -> addToMergeList((SkillBonus<?>)b, bonuses));
      return bonuses.stream().sorted().map(SkillBonus::getTooltip).map(Component.class::cast).toList();
   }

   public void updateSkillPoints(int skillPoints) {
      this.skillPoints = skillPoints - this.newlyLearnedSkills.size();
   }

   public void addSkillButton(PassiveSkill skill, Supplier<Float> renderAnimation) {
      SkillButton button = this.skills.addSkillButton(skill, renderAnimation);
      if (this.learnedSkills.contains(skill.getId()) || this.newlyLearnedSkills.contains(skill.getId())) {
         button.skillLearned = true;
      }
   }
}
