package daripher.skilltree.client.widget.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.config.ClientConfig;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.requirement.SkillRequirement;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class SkillButton extends Button {
   private static final Style DESCRIPTION_STYLE = Style.EMPTY.withColor(8092645);
   private static final Style ID_STYLE = Style.EMPTY.withColor(5526612);
   private final Supplier<Float> animationFunction;
   public final PassiveSkill skill;
   public float x;
   public float y;
   public boolean skillLearned;
   public boolean canLearn;
   public boolean searched;
   public boolean selected;

   public SkillButton(Supplier<Float> animationFunc, float x, float y, PassiveSkill skill) {
      super((int)x, (int)y, skill.getSkillSize(), skill.getSkillSize(), Component.empty(), b -> {
      }, Supplier::get);
      this.x = x;
      this.y = y;
      this.skill = skill;
      this.animationFunction = animationFunc;
      this.active = false;
   }

   public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      RenderSystem.enableBlend();
      graphics.pose().pushPose();
      graphics.pose().translate(this.x, this.y, 0.0F);
      this.renderFavoriteSkillHighlight(graphics);
      this.renderBackground(graphics);
      graphics.pose().pushPose();
      graphics.pose().translate((double)this.width / 2.0, (double)this.height / 2.0, 0.0);
      graphics.pose().scale(0.5F, 0.5F, 1.0F);
      if (this.width == 32) {
         graphics.pose().scale(0.75F, 0.75F, 1.0F);
      }

      graphics.pose().translate((double)(-this.width) / 2.0, (double)(-this.height) / 2.0, 0.0);
      this.renderIcon(graphics);
      graphics.pose().popPose();
      float animation = (Mth.sin(this.animationFunction.get() / 3.0F) + 1.0F) / 2.0F;
      float rb = this.searched ? 0.1F : 1.0F;
      if (this.canLearn || this.searched) {
         graphics.setColor(rb, 1.0F, rb, 1.0F - animation);
      }

      if (!this.skillLearned) {
         this.renderDarkening(graphics);
      }

      if (this.canLearn || this.searched) {
         graphics.setColor(rb, 1.0F, rb, animation);
      }

      if (this.skillLearned || this.canLearn || this.searched) {
         this.renderFrame(graphics);
      }

      if (this.canLearn || this.searched || this.selected) {
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      graphics.pose().popPose();
      RenderSystem.disableBlend();
   }

   private void renderFavoriteSkillHighlight(GuiGraphics graphics) {
      if (ClientConfig.favorite_skills.contains(this.skill.getId())) {
         ResourceLocation texture = new ResourceLocation("skilltree:textures/screen/favorite_skill.png");
         int color;
         if (ClientConfig.favorite_color_is_rainbow) {
            color = Color.getHSBColor(this.animationFunction.get() / 240.0F, 1.0F, 1.0F).getRGB();
         } else {
            color = ClientConfig.favorite_color;
         }

         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         graphics.setColor(r, g, b, 1.0F);
         int size = (int)((double)this.width * 1.4);
         graphics.pose().pushPose();
         graphics.pose().translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
         float animation = 1.0F + 0.3F * (Mth.sin(this.animationFunction.get() / 3.0F) + 1.0F) / 2.0F;
         graphics.pose().scale(animation, animation, 1.0F);
         graphics.pose().mulPose(Axis.ZP.rotationDegrees(this.animationFunction.get()));
         graphics.pose().translate((float)(-size) / 2.0F, (float)(-size) / 2.0F, 0.0F);
         graphics.blit(texture, 0, 0, size, size, 0.0F, 0.0F, 80, 80, 80, 80);
         graphics.pose().popPose();
         graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderFrame(GuiGraphics graphics) {
      ResourceLocation texture = this.skill.getFrameTexture();
      graphics.blit(texture, 0, 0, this.width, this.height, (float)(this.width * 2), 0.0F, this.width, this.height, this.width * 3, this.height);
   }

   private void renderDarkening(GuiGraphics graphics) {
      ResourceLocation texture = this.skill.getFrameTexture();
      graphics.blit(texture, 0, 0, this.width, this.height, (float)this.width, 0.0F, this.width, this.height, this.width * 3, this.height);
   }

   private void renderIcon(GuiGraphics graphics) {
      ResourceLocation texture = this.skill.getIconTexture();
      graphics.blit(texture, 0, 0, this.width, this.height, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
   }

   private void renderBackground(GuiGraphics graphics) {
      ResourceLocation texture = this.skill.getFrameTexture();
      graphics.blit(texture, 0, 0, this.width, this.height, 0.0F, 0.0F, this.width, this.height, this.width * 3, this.height);
   }

   public void setButtonSize(int size) {
      this.width = this.height = size;
   }

   public List<MutableComponent> getSkillTooltip(PassiveSkillTree skillTree) {
      ArrayList<MutableComponent> tooltip = new ArrayList<>();
      this.addTitleTooltip(tooltip);
      this.addLimitationsTooltip(skillTree, tooltip);
      List<MutableComponent> description = this.skill.getDescription();
      if (description != null) {
         tooltip.addAll(description);
      } else {
         this.addSkillBonusTooltip(tooltip);
      }

      this.addRequirementsTooltip(tooltip);
      this.addAdvancedTooltip(tooltip);
      return tooltip;
   }

   public void addRequirementsTooltip(ArrayList<MutableComponent> tooltip) {
      if (!this.skill.getRequirements().isEmpty()) {
         if (tooltip.size() > 1) {
            tooltip.add(Component.empty());
         }

         MutableComponent requirementsComponent = Component.translatable("skill.requirements");
         requirementsComponent = requirementsComponent.withStyle(TooltipHelper.getSkillBonusStyle(true));
         tooltip.add(requirementsComponent);
         this.skill.getRequirements().forEach(requirement -> this.addRequirementTooltip(tooltip, (SkillRequirement<?>)requirement));
      }
   }

   private void addRequirementTooltip(ArrayList<MutableComponent> tooltip, SkillRequirement<?> requirement) {
      MutableComponent requirementTooltip = requirement.getTooltip();
      Player localPlayer = Minecraft.getInstance().player;
      Style style = TooltipHelper.getSkillRequirementStyle(requirement.test(localPlayer));
      requirementTooltip = requirementTooltip.withStyle(style);
      tooltip.add(Component.literal("  ").append(requirementTooltip));
   }

   public void addSkillBonusTooltip(List<MutableComponent> tooltip) {
      this.addDescriptionTooltip(tooltip);
      this.addInfoTooltip(tooltip);
   }

   private void addInfoTooltip(List<MutableComponent> tooltip) {
      if (Screen.hasAltDown()) {
         List<MutableComponent> info = new ArrayList<>();

         for (SkillBonus<?> skillBonus : this.skill.getBonuses()) {
            skillBonus.gatherInfo(component -> {
               component = component.withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY});
               info.add(component);
            });
         }

         if (!info.isEmpty()) {
            tooltip.add(Component.empty());
            tooltip.addAll(info);
         }
      }
   }

   protected void addAdvancedTooltip(List<MutableComponent> tooltip) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.options.advancedItemTooltips) {
         this.addIdTooltip(tooltip);
      }
   }

   protected void addDescriptionTooltip(List<MutableComponent> tooltip) {
      this.skill.getBonuses().stream().map(SkillBonus::getTooltip).forEach(tooltip::add);
      String descriptionId = this.getSkillId() + ".description";
      String description = Component.translatable(descriptionId).getString();
      if (!description.equals(descriptionId)) {
         List<String> descriptionStrings = Arrays.asList(description.split("/n"));
         descriptionStrings.stream().<MutableComponent>map(Component::translatable).map(this::applyDescriptionStyle).forEach(tooltip::add);
      }
   }

   private void addLimitationsTooltip(PassiveSkillTree skillTree, ArrayList<MutableComponent> tooltips) {
      boolean addedLimitTooltip = false;

      for (String tag : this.skill.getTags()) {
         int limit = skillTree.getSkillLimitations().getOrDefault(tag, 0);
         if (limit > 0) {
            addedLimitTooltip = true;
            AtomicReference<MutableComponent> tagTooltip = new AtomicReference<>(Component.literal(tag));
            TooltipHelper.consumeTranslated("skill.tag.%s.name".formatted(tag), tagTooltip::set);
            tagTooltip.set(Component.literal(limit + " " + tagTooltip.get().getString()));
            tagTooltip.set(tagTooltip.get().withStyle(TooltipHelper.getSkillBonusSecondStyle(true)));
            MutableComponent tooltip = Component.translatable("skill.limitation", new Object[]{tagTooltip.get()});
            tooltip = tooltip.withStyle(TooltipHelper.getSkillBonusStyle(true));
            tooltips.add(tooltip);
         }
      }

      if (addedLimitTooltip) {
         tooltips.add(Component.empty());
      }
   }

   protected void addTitleTooltip(List<MutableComponent> tooltip) {
      tooltip.add(TooltipHelper.getSkillTitle(this.skill));
   }

   protected void addIdTooltip(List<MutableComponent> tooltip) {
      MutableComponent idComponent = Component.literal(this.skill.getId().toString()).withStyle(ID_STYLE);
      tooltip.add(idComponent);
   }

   protected MutableComponent applyDescriptionStyle(MutableComponent component) {
      return component.withStyle(DESCRIPTION_STYLE);
   }

   public void setCanLearn() {
      this.canLearn = true;
   }

   public void setActive() {
      this.active = true;
   }

   private String getSkillId() {
      return "skill." + this.skill.getId().getNamespace() + "." + this.skill.getId().getPath();
   }
}
