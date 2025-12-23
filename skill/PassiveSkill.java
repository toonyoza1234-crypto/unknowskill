package daripher.skilltree.skill;

import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.requirement.SkillRequirement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PassiveSkill {
   private final ResourceLocation id;
   private final List<SkillBonus<?>> bonuses = new ArrayList<>();
   @Nullable
   private List<SkillRequirement<?>> requirements;
   @Nullable
   private List<ResourceLocation> directConnections = new ArrayList<>();
   @Nullable
   private List<ResourceLocation> longConnections = new ArrayList<>();
   @Nullable
   private List<ResourceLocation> oneWayConnections = new ArrayList<>();
   @Nullable
   private List<String> tags = new ArrayList<>();
   private ResourceLocation backgroundTexture;
   private ResourceLocation iconTexture;
   private ResourceLocation borderTexture;
   @Nullable
   private String title;
   @Nullable
   private String titleColor;
   private float positionX;
   private float positionY;
   private int buttonSize;
   private boolean isStartingPoint;
   @Nullable
   private List<MutableComponent> description;

   public PassiveSkill(
      ResourceLocation id,
      int buttonSize,
      ResourceLocation backgroundTexture,
      ResourceLocation iconTexture,
      ResourceLocation borderTexture,
      boolean isStartingPoint
   ) {
      this.id = id;
      this.backgroundTexture = backgroundTexture;
      this.iconTexture = iconTexture;
      this.borderTexture = borderTexture;
      this.buttonSize = buttonSize;
      this.isStartingPoint = isStartingPoint;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public int getSkillSize() {
      return this.buttonSize;
   }

   public void setButtonSize(int buttonSize) {
      this.buttonSize = buttonSize;
   }

   public ResourceLocation getFrameTexture() {
      return this.backgroundTexture;
   }

   public void setBackgroundTexture(ResourceLocation texture) {
      this.backgroundTexture = texture;
   }

   public ResourceLocation getIconTexture() {
      return this.iconTexture;
   }

   public void setIconTexture(ResourceLocation texture) {
      this.iconTexture = texture;
   }

   public ResourceLocation getTooltipFrameTexture() {
      return this.borderTexture;
   }

   public void setBorderTexture(ResourceLocation texture) {
      this.borderTexture = texture;
   }

   public boolean isStartingPoint() {
      return this.isStartingPoint;
   }

   public void setStartingPoint(boolean isStartingPoint) {
      this.isStartingPoint = isStartingPoint;
   }

   public List<SkillBonus<?>> getBonuses() {
      return this.bonuses;
   }

   @Nonnull
   public List<SkillRequirement<?>> getRequirements() {
      return this.requirements == null ? (this.requirements = new ArrayList<>()) : this.requirements;
   }

   public void addSkillBonus(SkillBonus<?> bonus) {
      this.bonuses.add(bonus);
   }

   public void addSkillRequirement(SkillRequirement<?> requirement) {
      this.getRequirements().add(requirement);
   }

   public void connect(PassiveSkill otherSkill) {
      this.getDirectConnections().add(otherSkill.getId());
   }

   public void setPosition(float x, float y) {
      this.positionX = x;
      this.positionY = y;
   }

   public float getPositionX() {
      return this.positionX;
   }

   public float getPositionY() {
      return this.positionY;
   }

   @Nonnull
   public List<ResourceLocation> getDirectConnections() {
      return this.directConnections == null ? (this.directConnections = new ArrayList<>()) : this.directConnections;
   }

   @Nonnull
   public List<ResourceLocation> getLongConnections() {
      return this.longConnections == null ? (this.longConnections = new ArrayList<>()) : this.longConnections;
   }

   @Nonnull
   public List<ResourceLocation> getOneWayConnections() {
      return this.oneWayConnections == null ? (this.oneWayConnections = new ArrayList<>()) : this.oneWayConnections;
   }

   @Nonnull
   public List<String> getTags() {
      return this.tags == null ? (this.tags = new ArrayList<>()) : this.tags;
   }

   @Nonnull
   public String getTitle() {
      return this.title == null ? "" : this.title;
   }

   public void setTitle(@Nonnull String title) {
      this.title = title.isEmpty() ? null : title;
   }

   public void learn(ServerPlayer player, boolean firstTime) {
      this.getBonuses().forEach(b -> b.onSkillLearned(player, firstTime));
   }

   public void setTitleColor(@Nullable String color) {
      this.titleColor = color;
   }

   @Nonnull
   public String getTitleColor() {
      return this.titleColor == null ? "" : this.titleColor;
   }

   @Nullable
   public List<MutableComponent> getDescription() {
      return this.description;
   }

   public void setDescription(@Nullable List<MutableComponent> description) {
      this.description = description;
   }

   public void remove(ServerPlayer player) {
      this.getBonuses().forEach(b -> b.onSkillRemoved(player));
   }

   public boolean isInvalid() {
      return this.getId() == null
         || this.getBonuses() == null
         || this.getFrameTexture() == null
         || this.getIconTexture() == null
         || this.getTooltipFrameTexture() == null;
   }
}
